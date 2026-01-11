# Backend Project Standards & Guidelines

> **Versiyon:** 2.1  
> **Son Güncelleme:** 11.01.2025  
> **Durum:** Aktif / Zorunlu

Bu doküman, backend projeleri için mimari kararları, kodlama standartlarını ve operasyonel kuralları **Tek Doğruluk Kaynağı (SSOT)** olarak tanımlar.

---

## İçindekiler

1. [Teknoloji Yığını](#1-teknoloji-yığını)
2. [Mimari: Modüler Monolith](#2-mimari-modüler-monolith)
3. [Modüller Arası İletişim](#3-modüller-arası-i̇letişim)
4. [Transaction Yönetimi](#4-transaction-yönetimi)
5. [Security (Authentication & Authorization)](#5-security-authentication--authorization)
6. [Authentication Stratejisi](#6-authentication-stratejisi)
7. [Kodlama & Lombok](#7-kodlama--lombok)
8. [API ve Controller Standartları](#8-api-ve-controller-standartları)
9. [Exception Handling](#9-exception-handling)
10. [Logging & Observability](#10-logging--observability)
11. [JPA ve Veri Standartları](#11-jpa-ve-veri-standartları)
12. [Git & Commit Standartları](#12-git--commit-standartları)
13. [AI / Asistan Kullanımı](#13-ai--asistan-kullanımı)
14. [Anti-Pattern Referans Tabloları](#14-anti-pattern-referans-tabloları)
15. [Değiştirilemez Kurallar](#15-değiştirilemez-kurallar-özet)

---

## 1. Teknoloji Yığını

| Teknoloji | Tercih |
|-----------|--------|
| Language | Java 21 (LTS) |
| Framework | Spring Boot 3.x |
| Database | PostgreSQL |
| Migration | Flyway (Zorunlu) |
| Mapping | MapStruct |
| Docs | SpringDoc OpenAPI |
| Test | Testcontainers + JUnit 5 |

---

## 2. Mimari: Modüler Monolith

### Temel Kurallar

- Her iş alanı (Domain) ayrı bir maven/gradle modülü veya ana paket olmalıdır.
- **Encapsulation:** Sınıflar varsayılan olarak package-private olmalıdır.
- **Public API:** Sadece modülün dışa açılması gereken Service Interface'leri ve DTO'ları public olabilir. Implementation sınıfları (ServiceImpl) kesinlikle dışarı açılmaz.

### Yasaklar

- **Cross-Repository Access:** Modül A, Modül B'nin Repository'sini inject edemez. Veri erişimi Modül B'nin Servisi üzerinden yapılmalıdır.
- **Entity Paylaşımı:** Modüller birbirine Entity dönemez, sadece DTO dönebilir.

---

## 3. Modüller Arası İletişim

### Senkron İletişim (Method Call)

- **Kullanım:** Bir modülün cevabına anlık ihtiyaç duyulduğunda (örn: Sipariş oluştururken Stok kontrolü).
- **Yöntem:** Service Interface Injection.

### Asenkron İletişim (Events)

- **Kullanım:** "Side effect" işlerde (örn: Kayıt olunca email at, Fatura kesilince istatistik güncelle). Modüllerin birbirinden gevşek bağlı (loosely coupled) kalmasını sağlar.
- **Araç:** Spring `ApplicationEventPublisher` (varsayılan).
- **İleride:** Sistem büyüdüğünde RabbitMQ/Kafka'ya geçiş kolaylığı için event payload'ları POJO olmalıdır.

---

## 4. Transaction Yönetimi

- **Sınır:** Transaction sınırı Service katmanında başlar (`@Transactional`). Controller'da transaction başlatılmaz.
- **Read-Only:** Veri okuma operasyonlarında performans için `@Transactional(readOnly = true)` zorunludur.
- **Propagation:** Varsayılan (`REQUIRED`) genelde yeterlidir. Ancak, "Loglama" veya "Notification" gibi ana işi bozmaması gereken yan işlemlerde `REQUIRES_NEW` kullanılarak ana transaction'dan bağımsızlaştırılmalıdır.

---

## 5. Security (Authentication & Authorization)

### Temel Kurallar

- **Token (Web):** LocalStorage yasak. `HttpOnly`, `Secure` Cookie zorunlu.
- **Token (Mobil):** Response body'de token dönülür, client secure storage kullanır.
- **Yetki:** Controller'da `if (user == null)` kontrolü yasak. `@PreAuthorize` veya Security Filter Chain kullanılmalı.
- **Secrets:** Repository'de secret tutulamaz. Env variable veya Vault kullanılır.

### Swagger Global Auth Konfigürasyonu

Swagger'da auth gerektiren endpoint'ler için **endpoint başına manuel** `@SecurityRequirement` eklenmez. Global konfigürasyon kullanılır:

```java
// ❌ YANLIŞ - Her endpoint'e manuel ekleme
@Operation(security = @SecurityRequirement(name = "bearer"))
public UserResponse getUser() { }

// ✅ DOĞRU - Global konfigürasyon
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("API Documentation")
                .version("1.0"))
            .addSecurityItem(new SecurityRequirement().addList("bearer"))
            .components(new Components()
                .addSecuritySchemes("bearer", new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("JWT token giriniz")));
    }
}
```

**Not:** Public endpoint'ler (login, register, health) için `@SecurityRequirements` (boş) annotation ile auth bypass edilebilir:

```java
@PostMapping("/login")
@SecurityRequirements  // Auth gerektirmez
public AuthResponse login(@RequestBody LoginRequest request) { }
```

---

## 6. Authentication Stratejisi

### Genel Bakış

**Strateji:** Access Token + Refresh Token

| Token | Ömür | Amaç |
| --- | --- | --- |
| Access Token | 15 dakika | API istekleri için kısa ömürlü yetkilendirme |
| Refresh Token | 7 gün | Access token yenileme |

### Web vs Mobil Farkı

| Platform | Token Gönderimi | Token Dönüşü | Client Storage |
| --- | --- | --- | --- |
| **Web** | Cookie (otomatik) | Set-Cookie header | Browser (httpOnly) |
| **Mobil** | Authorization header | Response body | Secure Storage |

**Neden farklı?**
- Web'de httpOnly cookie ile XSS koruması sağlanır (JS token'a erişemez)
- Mobilde browser yok, cookie mekanizması çalışmaz → Secure Storage kullanılır

### Token Akışı

```
1. LOGIN
   Client                          Backend
     │ POST /auth/login              │
     │ {email, password}             │
     │ Header: X-Platform: web/mobile│
     │─────────────────────────────►│
     │                               │ Validate credentials
     │                               │ Generate tokens
     │                               │
     │ [Web: Set-Cookie headers]     │
     │ [Mobile: JSON body tokens]    │
     │◄─────────────────────────────│

2. API İSTEĞİ
   [Web]
     │ GET /api/journals             │
     │ Cookie: accessToken=xxx       │ (browser otomatik gönderir)
     │─────────────────────────────►│

   [Mobile]
     │ GET /api/journals             │
     │ Authorization: Bearer xxx     │ (client manuel ekler)
     │─────────────────────────────►│

3. TOKEN REFRESH
     │ POST /auth/refresh            │
     │ [Web: Cookie ile]             │
     │ [Mobile: Body'de refresh]     │
     │─────────────────────────────►│
     │                               │
     │ [Web: Yeni Set-Cookie]        │
     │ [Mobile: Yeni tokens body'de] │
     │◄─────────────────────────────│

4. LOGOUT
     │ POST /auth/logout             │
     │─────────────────────────────►│
     │                               │
     │ [Web: Clear cookies]          │
     │ [Mobile: 200 OK]              │
     │◄─────────────────────────────│
```

### Authentication Endpoints

#### POST /auth/login

```java
@PostMapping("/login")
@SecurityRequirements  // Public endpoint
public ResponseEntity<?> login(
        @RequestBody @Valid LoginRequest request,
        @RequestHeader(value = "X-Platform", defaultValue = "web") String platform) {
    
    AuthResult result = authService.login(request);
    
    if ("mobile".equalsIgnoreCase(platform)) {
        // Mobil: Token'ları body'de dön
        return ResponseEntity.ok(LoginResponse.builder()
            .accessToken(result.accessToken())
            .refreshToken(result.refreshToken())
            .user(result.user())
            .build());
    }
    
    // Web: Cookie olarak set et
    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, buildAccessCookie(result.accessToken()).toString())
        .header(HttpHeaders.SET_COOKIE, buildRefreshCookie(result.refreshToken()).toString())
        .body(LoginResponse.builder()
            .user(result.user())
            .build());  // Token body'de YOK
}
```

#### POST /auth/refresh

```java
@PostMapping("/refresh")
@SecurityRequirements
public ResponseEntity<?> refresh(
        @CookieValue(name = "refreshToken", required = false) String cookieRefreshToken,
        @RequestBody(required = false) RefreshRequest bodyRequest,
        @RequestHeader(value = "X-Platform", defaultValue = "web") String platform) {
    
    // Token'ı al (cookie veya body'den)
    String refreshToken = "mobile".equalsIgnoreCase(platform) 
        ? bodyRequest.refreshToken() 
        : cookieRefreshToken;
    
    if (refreshToken == null) {
        throw new UnauthorizedException("Refresh token bulunamadı");
    }
    
    String newAccessToken = authService.refresh(refreshToken);
    
    if ("mobile".equalsIgnoreCase(platform)) {
        return ResponseEntity.ok(RefreshResponse.builder()
            .accessToken(newAccessToken)
            .build());
    }
    
    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, buildAccessCookie(newAccessToken).toString())
        .build();
}
```

#### POST /auth/logout

```java
@PostMapping("/logout")
public ResponseEntity<Void> logout(
        @RequestHeader(value = "X-Platform", defaultValue = "web") String platform) {
    
    // Opsiyonel: Refresh token'ı blacklist'e ekle
    // authService.invalidateRefreshToken(refreshToken);
    
    if ("mobile".equalsIgnoreCase(platform)) {
        return ResponseEntity.ok().build();
    }
    
    // Web: Cookie'leri temizle
    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, clearAccessCookie().toString())
        .header(HttpHeaders.SET_COOKIE, clearRefreshCookie().toString())
        .build();
}
```

#### GET /auth/me

```java
@GetMapping("/me")
public UserResponse getCurrentUser(@AuthenticationPrincipal UserPrincipal principal) {
    return userService.getById(principal.getId());
}
```

### Cookie Configuration

```java
@Component
public class CookieUtils {

    @Value("${app.cookie.secure:true}")
    private boolean secure;
    
    @Value("${app.cookie.same-site:Strict}")
    private String sameSite;

    public ResponseCookie buildAccessCookie(String token) {
        return ResponseCookie.from("accessToken", token)
            .httpOnly(true)              // JS erişemez (XSS koruması)
            .secure(secure)              // HTTPS only (prod'da true)
            .sameSite(sameSite)          // CSRF koruması
            .path("/")                   // Tüm API'ler için geçerli
            .maxAge(Duration.ofMinutes(15))
            .build();
    }

    public ResponseCookie buildRefreshCookie(String token) {
        return ResponseCookie.from("refreshToken", token)
            .httpOnly(true)
            .secure(secure)
            .sameSite(sameSite)
            .path("/auth")               // Sadece /auth/* endpoint'leri için
            .maxAge(Duration.ofDays(7))
            .build();
    }

    public ResponseCookie clearAccessCookie() {
        return ResponseCookie.from("accessToken", "")
            .httpOnly(true)
            .secure(secure)
            .sameSite(sameSite)
            .path("/")
            .maxAge(0)                   // Hemen expire et
            .build();
    }

    public ResponseCookie clearRefreshCookie() {
        return ResponseCookie.from("refreshToken", "")
            .httpOnly(true)
            .secure(secure)
            .sameSite(sameSite)
            .path("/auth")
            .maxAge(0)
            .build();
    }
}
```

### Cookie Özellikleri

| Özellik | Değer | Açıklama |
| --- | --- | --- |
| `HttpOnly` | `true` | JavaScript erişemez → XSS koruması |
| `Secure` | `true` (prod) | Sadece HTTPS üzerinden gönderilir |
| `SameSite` | `Strict` | Cross-site request'lerde gönderilmez → CSRF koruması |
| `Path` | Access: `/`, Refresh: `/auth` | Yüzey alanını küçültür |
| `Max-Age` | Access: 15dk, Refresh: 7gün | Token ömrü |

### JWT Token Service

```java
@Service
@RequiredArgsConstructor
public class JwtTokenService {

    @Value("${app.jwt.secret}")
    private String secret;
    
    @Value("${app.jwt.access-expiration-ms:900000}")  // 15 dakika
    private long accessExpirationMs;
    
    @Value("${app.jwt.refresh-expiration-ms:604800000}")  // 7 gün
    private long refreshExpirationMs;

    public String generateAccessToken(UserPrincipal user) {
        return Jwts.builder()
            .setSubject(user.getId().toString())
            .claim("email", user.getEmail())
            .claim("roles", user.getRoles())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + accessExpirationMs))
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .compact();
    }

    public String generateRefreshToken(UserPrincipal user) {
        return Jwts.builder()
            .setSubject(user.getId().toString())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + refreshExpirationMs))
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody();
        return Long.parseLong(claims.getSubject());
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
```

### Security Filter (Cookie + Header desteği)

```java
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        
        String token = extractToken(request);
        
        if (token != null && jwtTokenService.validateToken(token)) {
            Long userId = jwtTokenService.getUserIdFromToken(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(userId.toString());
            
            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
                );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        
        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        // 1. Önce Cookie'den dene (Web)
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("accessToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        
        // 2. Authorization header'dan dene (Mobile)
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        
        return null;
    }
}
```

### İleride: Refresh Token Rotation

Güvenlik gereksinimleri artarsa (finans, sağlık, enterprise) rotation eklenebilir:

```java
// Refresh token DB'de tutulur
@Entity
public class RefreshToken {
    @Id
    private String tokenHash;
    private Long userId;
    private boolean used;        // Rotation için
    private Instant expiresAt;
}

// Her refresh'te:
// 1. Mevcut token "used" olarak işaretlenir
// 2. Yeni refresh token oluşturulur
// 3. Kullanılmış token tekrar denenirse → Tüm token'lar invalidate (şüpheli aktivite)
```

---

## 7. Kodlama & Lombok

- **Injection:** Constructor Injection zorunlu (`@RequiredArgsConstructor` önerilir). Field Injection (`@Autowired`) yasak.
- **Entity:** Entity sınıflarında `@Data`, `@AllArgsConstructor` yasak. Manuel constructor veya Builder ile tutarlılık korunmalı.
- **Validation:** İş kuralları Domain içinde, format kontrolleri (`@NotNull` vb.) DTO üzerinde olmalıdır.

### MapStruct Kuralları

MapStruct interface'lerinde `componentModel = "spring"` kullanımı zorunludur. Mapper'lar her zaman Dependency Injection ile çağrılmalıdır.

```java
// ❌ YASAK - Factory pattern
UserMapper mapper = Mappers.getMapper(UserMapper.class);

// ✅ DOĞRU - Spring managed
@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toResponse(User entity);
    User toEntity(CreateUserRequest request);
}

// Service'te kullanım
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserMapper userMapper;  // DI ile inject
}
```

---

## 8. API ve Controller Standartları

### 8.1 Entity Sızıntısı (Leakage) 🛑

- Entity nesneleri **asla** Controller'dan dışarı dönemez.
- Entity nesneleri **asla** Controller'a parametre olarak giremez.
- Request ve Response her zaman DTO olmalıdır.

### 8.2 Controller Return Type Standardı

#### Varsayılan: POJO + @ResponseStatus

Controller metotları varsayılan olarak iş sonucunu temsil eden nesneyi (DTO) dönmeli, HTTP durum kodları deklaratif olarak (`@ResponseStatus`) yönetilmelidir.

```java
// ✅ STANDART

// CREATE İşlemi: 201 Created ve DTO döner
@PostMapping
@ResponseStatus(HttpStatus.CREATED)
public UserResponse create(@RequestBody @Valid CreateUserRequest request) {
    return userService.create(request);
}

// READ İşlemi: 200 OK (Varsayılan) ve DTO döner
@GetMapping("/{id}")
public UserResponse getById(@PathVariable Long id) {
    return userService.getById(id);
}

// DELETE İşlemi: 204 No Content ve boş döner
@DeleteMapping("/{id}")
@ResponseStatus(HttpStatus.NO_CONTENT)
public void delete(@PathVariable Long id) {
    userService.delete(id);
}
```

**Avantajları:**
- **Temiz İmza:** Metodun ne döndüğü (`UserResponse`) bir bakışta anlaşılır
- **Kolay Test:** Unit testlerde `response.getBody()` gibi sarmalayıcıları açmaya gerek kalmaz
- **Separation of Concerns:** HTTP detayları business logic'ten ayrıştırılır
- **OpenAPI/Swagger:** Dokümantasyon araçları dönüş tipini hatasız analiz eder

#### İstisna: ResponseEntity (Sadece Özel Gereksinimler)

`ResponseEntity` wrapper'ı **yalnızca** aşağıdaki teknik gereksinimler olduğunda kullanılabilir:

| Durum | Örnek |
|-------|-------|
| File Download/Upload | `Content-Disposition` header eklemek için |
| Cache Kontrolü | `ETag`, `Cache-Control` headerlarını yönetmek için |
| Cookie / Auth | Response'a özel `Set-Cookie` veya `Authorization` header eklemek için |
| Public API | Eğer 3. parti entegrasyon için `Location` header zorunluysa |

```java
// ✅ İSTİSNA ÖRNEĞİ - Dosya İndirme
@GetMapping("/export")
public ResponseEntity<Resource> export() {
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report.csv")
        .body(resource);
}
```

#### Yasaklar

```java
// ❌ YASAK - Tiplendirilmemiş (Wildcard) ResponseEntity
// Swagger dokümantasyonunu bozar ve Type Safety'i yok eder.
public ResponseEntity<?> getUser() { }
public ResponseEntity<Object> getUser() { }

// ❌ YASAK - Gereksiz Wrapper Kullanımı
// Header manipülasyonu yoksa ResponseEntity gereksizdir.
public ResponseEntity<UserResponse> getUser() {
    return ResponseEntity.ok(userService.get()); 
}
```

> **💡 Sık Sorulan Soru:** "Kayıt bulunamazsa 404'ü nasıl döneceğiz?"
>
> **Cevap:** Service katmanında `ResourceNotFoundException` fırlatılır, `GlobalExceptionHandler` bunu yakalar ve 404'e çevirir. Controller içinde `if (user == null) return ResponseEntity.notFound().build();` yazmaya gerek yoktur.

### 8.3 Pagination

Manuel `page`/`size` parametresi yasak. Spring Data `Pageable` arayüzü kullanılmalı:

```java
// ❌ YANLIŞ
@GetMapping
public List<UserResponse> list(@RequestParam int page, @RequestParam int size) {
    return userService.list(page, size);
}

// ✅ DOĞRU
@GetMapping
public Page<UserResponse> list(@ParameterObject Pageable pageable) {
    return userService.list(pageable);
}
```

### 8.4 API Docs

Public endpointler OpenAPI (Swagger) ile dokümante edilmelidir.

---

## 9. Exception Handling

Hata yönetimi merkezi ve standart olmalıdır.

### Global Exception Handler

- Tüm projede tek bir `@RestControllerAdvice` sınıfı olmalıdır.
- İstemciye **asla** Stacktrace dönülmez.

### Exception Hiyerarşisi

| Exception | Açıklama | HTTP Status |
|-----------|----------|-------------|
| `ValidationException` | Validation hataları (form, input) | 400 |
| `UnauthorizedException` | Kimlik doğrulama hatası | 401 |
| `ForbiddenException` | Yetki hatası | 403 |
| `ResourceNotFoundException` | Veri bulunamadı | 404 |
| `ConflictException` | İş kuralı çakışması (duplicate email vb.) | 409 |
| `BusinessException` | İş kuralı hataları (Stok yetersiz, Bakiye yok) | 422 |
| `TechnicalException` | Sistem hataları (DB down). Kullanıcıya genel mesaj, loga detay. | 500 |

### Error Response Format (RFC 7807)

```json
{
  "type": "business-error",
  "title": "Yetersiz Bakiye",
  "status": 422,
  "detail": "İşlem için 50 TL eksik.",
  "instance": "/api/v1/plans/payment",
  "errors": {
    "amount": ["Minimum 100 TL olmalıdır"]
  }
}
```

---

## 10. Logging & Observability

### Log Seviyeleri

| Seviye | Kullanım |
|--------|----------|
| `ERROR` | Müdahale gereken teknik hatalar (DB bağlantı hatası, NullPointer) |
| `WARN` | Beklenen iş hataları veya validasyon sorunları (Login başarısız, yetersiz bakiye). Stacktrace basılmaz. |
| `INFO` | Genel akış (Uygulama başladı, Job bitti, Kritik işlem tamamlandı) |
| `DEBUG` | Geliştirme detayları (Payload içeriği, SQL parametresi). Prod'da kapalıdır. |

### Kurallar

- **Correlation ID:** Her request sisteme girdiğinde bir `traceId` atanmalı ve MDC'ye (Mapped Diagnostic Context) eklenmelidir. Loglarda bu ID aranabilir olmalıdır.
- **PII (Hassas Veri):** TC Kimlik, Şifre, Kredi Kartı, Token loglanamaz. Maskelenmelidir.

---

## 11. JPA ve Veri Standartları

- **Enum:** `@Enumerated(EnumType.STRING)` zorunlu. Ordinal yasak.
- **Date:** `java.util.Date` yasak. `Instant` veya `LocalDateTime` zorunlu.
- **Flyway:** Şema değişiklikleri versiyonlu SQL dosyalarıyla yönetilir. Hibernate `ddl-auto` → `validate` olmalıdır.

---

## 12. Git & Commit Standartları

**Conventional Commits** zorunludur.

**Format:** `<tip>(<kapsam>): <açıklama>`

| Tip | Örnek |
|-----|-------|
| `feat` | `feat(auth): add login` |
| `fix` | `fix(payment): resolve currency bug` |
| `refactor` | `refactor(core): cleanup util classes` |
| `docs` | `docs(readme): add setup instructions` |
| `chore` | `chore(deps): upgrade spring-boot` |
| `test` | `test(order): add integration tests` |

---

## 13. AI / Asistan Kullanımı

AI araçlarına (Copilot, Cursor, ChatGPT) prompt girilirken şu ifade eklenmelidir:

> "Bu kodu yazarken **BE-STANDARDS.md v2.1** dosyasındaki kurallara (özellikle Authentication Stratejisi, Exception Handling, Transaction, Controller Return Type ve Entity Sızıntısı maddelerine) %100 uy."

---

## 14. Anti-Pattern Referans Tabloları

Bu bölüm, sık yapılan hataları ve doğru yöntemleri hızlı referans için özetler.

### 14.1 Controller & API Katmanı

| ❌ Yanlış | ✅ Doğru | Açıklama |
|-----------|----------|----------|
| Manuel `page`, `size` parametresi | `Pageable` inject etmek | Spring Data'nın gücünü kullan |
| `if (principal == null)` kontrolü | `@PreAuthorize` annotation | Security Filter Chain'e bırak |
| Entity döndürmek | DTO döndürmek | Entity sızıntısını önle |
| `ResponseEntity<Object>` | Tipli `ResponseEntity<UserResponse>` | Type safety |
| `ResponseEntity<?>` | POJO + `@ResponseStatus` | Swagger uyumluluğu |
| Her endpoint'te try-catch | `@RestControllerAdvice` | Merkezi hata yönetimi |
| Endpoint başı Swagger auth | Global `SecurityScheme` config | Tek yerden yönet |
| `ResponseEntity.ok(dto)` (gereksiz) | Direkt DTO dön | Header yoksa wrapper gereksiz |

### 14.2 Service Katmanı

| ❌ Yanlış | ✅ Doğru | Açıklama |
|-----------|----------|----------|
| `findById().get()` | `findById().orElseThrow()` | NoSuchElement yerine anlamlı hata |
| Null döndürmek | `Optional` veya Exception | Null güvensizliğini önle |
| Service'te `HttpServletRequest` | DTO ile veri almak | Katman bağımsızlığı |
| Başka modülün Repository'sini inject | O modülün Service'ini kullan | Modül sınırlarına saygı |
| Read işleminde `@Transactional` | `@Transactional(readOnly = true)` | Performans optimizasyonu |
| Controller'da iş mantığı | Service'te iş mantığı | Separation of concerns |
| `Mappers.getMapper(...)` | `@Mapper(componentModel = "spring")` + DI | Spring context entegrasyonu |

### 14.3 Repository & JPA Katmanı

| ❌ Yanlış | ✅ Doğru | Açıklama |
|-----------|----------|----------|
| `@Enumerated` (default Ordinal) | `@Enumerated(EnumType.STRING)` | Enum sırası değişirse veri bozulur |
| `java.util.Date` | `Instant` veya `LocalDateTime` | Modern Java Time API |
| Entity'de `@Data` | Manuel getter + Builder | Equals/HashCode sorunları |
| `cascade = CascadeType.ALL` | İhtiyaca göre spesifik cascade | Beklenmeyen silmeleri önle |
| N+1 query (lazy loading döngüsü) | `@EntityGraph` veya `JOIN FETCH` | Performans |
| `ddl-auto=update` | `ddl-auto=validate` + Flyway | Kontrollü şema yönetimi |

### 14.4 Security & Authentication

| ❌ Yanlış | ✅ Doğru | Açıklama |
|-----------|----------|----------|
| Token'ı response body'de web'e dönmek | `Set-Cookie` header | XSS koruması |
| `SameSite=None` | `SameSite=Strict` | CSRF koruması |
| Tek cookie path `/` (refresh için) | Access `/`, Refresh `/auth` | Yüzey alanını küçült |
| Token süresiz | Access 15dk, Refresh 7gün | Güvenlik |
| Token LocalStorage'da (web) | HttpOnly Cookie | XSS koruması |
| Secret'ları yml'e yazmak | Environment variable / Vault | Git güvenliği |
| Endpoint başı CORS config | Global CORS configuration | Tutarlılık |
| Hardcoded role string'leri | Enum veya constant | Typo hatalarını önle |
| `if (user == null)` | `@PreAuthorize("hasRole('USER')")` | Deklaratif güvenlik |
| JWT secret'ı kısa/basit | Min 256-bit, Base64 encoded | Kaba kuvvet koruması |

### 14.5 Logging

| ❌ Yanlış | ✅ Doğru | Açıklama |
|-----------|----------|----------|
| `e.printStackTrace()` | `log.error("msg", e)` | Structured logging |
| PII loglamak (TC, şifre, token) | Maskeleme veya exclude | KVKK/GDPR uyumu |
| Her yerde DEBUG log | Seviyeye uygun loglama | Prod'da gürültü önleme |
| Exception'da sadece message | Correlation ID ile loglama | Trace edilebilirlik |

### 14.6 Test

| ❌ Yanlış | ✅ Doğru | Açıklama |
|-----------|----------|----------|
| H2 ile test | Testcontainers + PostgreSQL | Gerçek DB davranışı |
| Test metodunda belirsiz isim | `should_returnUser_when_validId` | Okunabilirlik |
| Mock overuse | Integration test dengesi | Gerçek senaryoları yakala |
| Test olmadan PR açmak | Minimum coverage sağlamak | Kalite güvencesi |

### 14.7 Naming Conventions

| ❌ Yanlış | ✅ Doğru | Açıklama |
|-----------|----------|----------|
| Generic isimler: `data`, `info`, `item` | Domain isimleri: `userData`, `orderInfo` | İsimden anlam çıkmalı |
| Boolean: `flag`, `status` | Soru kalıpları: `isActive`, `hasPermission` | Evet/Hayır okunabilirliği |
| Magic Numbers: `if (status == 1)` | Constants/Enums: `UserStatus.ACTIVE` | Sihirli sayılar yasak |
| Kısaltmalar: `usr`, `cust`, `doc` | Tam kelimeler: `user`, `customer` | Okunabilirlik |
| Negatif isimlendirme: `isNotActive` | Pozitif: `isActive` | Çift negasyon karmaşası |

### 14.8 Performance Anti-Patterns

| ❌ Yanlış | ✅ Doğru | Açıklama |
|-----------|----------|----------|
| `repo.findAll().stream().filter()` | `repo.findByAgeGreaterThan(18)` | DB'de filtrele |
| Döngü içinde sorgu | `repo.findAllById(ids)` | Toplu sorgu |
| Metot içinde `new ObjectMapper()` | Constructor Injection (Singleton) | Ağır nesne tekrar yaratma |
| `forEach` içinde `repo.save()` | `repo.saveAll(users)` | Batch operation |
| Döngüde String birleştirme | `StringBuilder` | Bellek verimliliği |

### 14.9 Java Best Practices

| ❌ Yanlış | ✅ Doğru | Açıklama |
|-----------|----------|----------|
| For loop ile filtreleme | Stream API | Okunabilirlik |
| `if (list != null && !list.isEmpty())` | Boş liste dön, asla null | Null safety |
| `Optional.get()` | `orElseThrow()` veya `orElse()` | NPE riski |
| Tarih karşılaştırma: `toString().equals()` | `isBefore()`, `isAfter()` | Time API |

---

## 15. Değiştirilemez Kurallar (Özet)

Aşağıdaki kuralların ihlali Code Review aşamasında **reddedilme** sebebidir:

| Kural | Durum |
|-------|-------|
| Field Injection (`@Autowired`) | ❌ Yasak |
| Controller'da `if (user == null)` | ❌ Yasak |
| Token'lar LocalStorage'da (web) | ❌ Yasak |
| Token'ı body'de web'e dönmek | ❌ Yasak |
| Enum Ordinal mapping | ❌ Yasak |
| Manuel pagination (`page`, `size`) | ❌ Yasak |
| Secret'lar Git'te | ❌ Yasak |
| Flyway yerine `ddl-auto=update` | ❌ Yasak |
| H2 veritabanı (test dahil) | ❌ Yasak |
| Entity'de `@Data` | ❌ Yasak |
| Entity Controller'a dönmek | ❌ Yasak |
| Stacktrace istemciye dönmek | ❌ Yasak |
| `ResponseEntity<?>` veya `ResponseEntity<Object>` | ❌ Yasak |
| Gereksiz `ResponseEntity` wrapper | ❌ Yasak |
| Endpoint başı Swagger auth tanımı | ❌ Yasak |
| `findById().get()` kullanımı | ❌ Yasak |
| `e.printStackTrace()` kullanımı | ❌ Yasak |
| `Mappers.getMapper(...)` kullanımı | ❌ Yasak |
| `repo.findAll()` + bellekte filtreleme | ❌ Yasak |
| Döngü içinde tekil DB sorgusu | ❌ Yasak |
| `forEach` içinde `repo.save()` | ❌ Yasak |
| Magic number/string kullanımı | ❌ Yasak |
| Token/PII loglama | ❌ Yasak |

---

> **Doküman Sonu**  
> Sorular veya öneriler için: Teknik Lider ile iletişime geçin.

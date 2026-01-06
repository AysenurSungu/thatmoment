# Backend Project Standards & Guidelines

> **Versiyon:** 2.0  
> **Son Güncelleme:** 06.01.2026  
> **Durum:** Aktif / Zorunlu

Bu doküman, backend projeleri için mimari kararları, kodlama standartlarını ve operasyonel kuralları **Tek Doğruluk Kaynağı (SSOT)** olarak tanımlar.

---

## İçindekiler

1. [Teknoloji Yığını](#1-teknoloji-yığını)
2. [Mimari: Modüler Monolith](#2-mimari-modüler-monolith)
3. [Modüller Arası İletişim](#3-modüller-arası-i̇letişim)
4. [Transaction Yönetimi](#4-transaction-yönetimi)
5. [Security](#5-security-authentication--authorization)
6. [Kodlama & Lombok](#6-kodlama--lombok)
7. [API ve Controller Standartları](#7-api-ve-controller-standartları)
8. [Exception Handling](#8-exception-handling)
9. [Logging & Observability](#9-logging--observability)
10. [JPA ve Veri Standartları](#10-jpa-ve-veri-standartları)
11. [Git & Commit Standartları](#11-git--commit-standartları)
12. [AI / Asistan Kullanımı](#12-ai--asistan-kullanımı)
13. [Anti-Pattern Referans Tabloları](#13-anti-pattern-referans-tabloları)
  - 13.1 Controller & API Katmanı
  - 13.2 Service Katmanı
  - 13.3 Repository & JPA Katmanı
  - 13.4 Security
  - 13.5 Logging
  - 13.6 Test
  - 13.7 Naming Conventions
  - 13.8 Performance Anti-Patterns
  - 13.9 Java Best Practices
14. [Değiştirilemez Kurallar](#14-değiştirilemez-kurallar-özet)

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

- **Token:** LocalStorage yasak. `HttpOnly`, `Secure` Cookie zorunlu.
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

## 6. Kodlama & Lombok

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

## 7. API ve Controller Standartları

### 7.1 Entity Sızıntısı (Leakage) 🛑

- Entity nesneleri **asla** Controller'dan dışarı dönemez.
- Entity nesneleri **asla** Controller'a parametre olarak giremez.
- Request ve Response her zaman DTO olmalıdır.

### 7.2 Controller Return Type Standardı

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

### 7.3 Pagination

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

### 7.4 API Docs

Public endpointler OpenAPI (Swagger) ile dokümante edilmelidir.

---

## 8. Exception Handling

Hata yönetimi merkezi ve standart olmalıdır.

### Global Exception Handler

- Tüm projede tek bir `@RestControllerAdvice` sınıfı olmalıdır.
- İstemciye **asla** Stacktrace dönülmez.

### Exception Hiyerarşisi

| Exception | Açıklama | HTTP Status |
|-----------|----------|-------------|
| `BusinessException` | İş kuralı hataları (Stok yetersiz, Bakiye yok) | 400 / 422 |
| `ResourceNotFoundException` | Veri bulunamadı | 404 |
| `TechnicalException` | Sistem hataları (DB down, NullPointer). Kullanıcıya genel mesaj, loga detay basılır. | 500 |

### Error Response Format (RFC 7807)

```json
{
  "type": "business-error",
  "title": "Yetersiz Bakiye",
  "status": 422,
  "detail": "İşlem için 50 TL eksik.",
  "instance": "/api/v1/plans/payment"
}
```

---

## 9. Logging & Observability

### Log Seviyeleri

| Seviye | Kullanım |
|--------|----------|
| `ERROR` | Müdahale gereken teknik hatalar (DB bağlantı hatası, NullPointer) |
| `WARN` | Beklenen iş hataları veya validasyon sorunları (Login başarısız, yetersiz bakiye). Stacktrace basılmaz. |
| `INFO` | Genel akış (Uygulama başladı, Job bitti, Kritik işlem tamamlandı) |
| `DEBUG` | Geliştirme detayları (Payload içeriği, SQL parametresi). Prod'da kapalıdır. |

### Kurallar

- **Correlation ID:** Her request sisteme girdiğinde bir `traceId` atanmalı ve MDC'ye (Mapped Diagnostic Context) eklenmelidir. Loglarda bu ID aranabilir olmalıdır.
- **PII (Hassas Veri):** TC Kimlik, Şifre, Kredi Kartı loglanamaz. Maskelenmelidir.

---

## 10. JPA ve Veri Standartları

- **Enum:** `@Enumerated(EnumType.STRING)` zorunlu. Ordinal yasak.
- **Date:** `java.util.Date` yasak. `Instant` veya `LocalDateTime` zorunlu.
- **Flyway:** Şema değişiklikleri versiyonlu SQL dosyalarıyla yönetilir. Hibernate `ddl-auto` → `validate` olmalıdır.

---

## 11. Git & Commit Standartları

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

## 12. AI / Asistan Kullanımı

AI araçlarına (Copilot, Cursor, ChatGPT) prompt girilirken şu ifade eklenmelidir:

> "Bu kodu yazarken **BACKEND-STANDARDS.md v2.0** dosyasındaki kurallara (özellikle Exception Handling, Transaction, Controller Return Type ve Entity Sızıntısı maddelerine) %100 uy."

---

## 13. Anti-Pattern Referans Tabloları

Bu bölüm, sık yapılan hataları ve doğru yöntemleri hızlı referans için özetler.

### 13.1 Controller & API Katmanı

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

### 13.2 Service Katmanı

| ❌ Yanlış | ✅ Doğru | Açıklama |
|-----------|----------|----------|
| `findById().get()` | `findById().orElseThrow()` | NoSuchElement yerine anlamlı hata |
| Null döndürmek | `Optional` veya Exception | Null güvensizliğini önle |
| Service'te `HttpServletRequest` | DTO ile veri almak | Katman bağımsızlığı |
| Başka modülün Repository'sini inject | O modülün Service'ini kullan | Modül sınırlarına saygı |
| Read işleminde `@Transactional` | `@Transactional(readOnly = true)` | Performans optimizasyonu |
| Controller'da iş mantığı | Service'te iş mantığı | Separation of concerns |
| `Mappers.getMapper(...)` | `@Mapper(componentModel = "spring")` + DI | Spring context entegrasyonu |

### 13.3 Repository & JPA Katmanı

| ❌ Yanlış | ✅ Doğru | Açıklama |
|-----------|----------|----------|
| `@Enumerated` (default Ordinal) | `@Enumerated(EnumType.STRING)` | Enum sırası değişirse veri bozulur |
| `java.util.Date` | `Instant` veya `LocalDateTime` | Modern Java Time API |
| Entity'de `@Data` | Manuel getter + Builder | Equals/HashCode sorunları |
| `cascade = CascadeType.ALL` | İhtiyaca göre spesifik cascade | Beklenmeyen silmeleri önle |
| N+1 query (lazy loading döngüsü) | `@EntityGraph` veya `JOIN FETCH` | Performans |
| `ddl-auto=update` | `ddl-auto=validate` + Flyway | Kontrollü şema yönetimi |

### 13.4 Security

| ❌ Yanlış | ✅ Doğru | Açıklama |
|-----------|----------|----------|
| Token'ı LocalStorage'da tutmak | HttpOnly Secure Cookie | XSS koruması |
| Secret'ları yml'e yazmak | Environment variable / Vault | Git güvenliği |
| Endpoint başı CORS config | Global CORS configuration | Tutarlılık |
| Hardcoded role string'leri | Enum veya constant | Typo hatalarını önle |
| `if (user == null)` | `@PreAuthorize("hasRole('USER')")` | Deklaratif güvenlik |

### 13.5 Logging

| ❌ Yanlış | ✅ Doğru | Açıklama |
|-----------|----------|----------|
| `e.printStackTrace()` | `log.error("msg", e)` | Structured logging |
| PII loglamak (TC, şifre) | Maskeleme veya exclude | KVKK/GDPR uyumu |
| Her yerde DEBUG log | Seviyeye uygun loglama | Prod'da gürültü önleme |
| Exception'da sadece message | Correlation ID ile loglama | Trace edilebilirlik |

### 13.6 Test

| ❌ Yanlış | ✅ Doğru | Açıklama |
|-----------|----------|----------|
| H2 ile test | Testcontainers + PostgreSQL | Gerçek DB davranışı |
| Test metodunda belirsiz isim | `should_returnUser_when_validId` | Okunabilirlik |
| Mock overuse | Integration test dengesi | Gerçek senaryoları yakala |
| Test olmadan PR açmak | Minimum coverage sağlamak | Kalite güvencesi |

### 13.7 Naming Conventions

| ❌ Yanlış (Anti-Pattern) | ✅ Doğru (Best Practice) | Açıklama |
|--------------------------|--------------------------|----------|
| Generic isimler: `data`, `info`, `item`, `list`, `tmp` | Domain isimleri: `userData`, `orderInfo`, `cartItem`, `userList` | Değişkenin ne taşıdığı isminden anlaşılmalı |
| Boolean isimlendirme: `flag`, `status` (boolean ise), `check` | Soru kalıpları: `isActive`, `hasPermission`, `isDeleted` | Boolean değişkenler "Evet/Hayır" sorusu gibi okunmalı |
| Magic Numbers: `if (status == 1)`, `if (type.equals("A"))` | Constants/Enums: `if (status == UserStatus.ACTIVE)`, `if (type.equals(Type.ADMIN))` | "1" ne demek? Kodun içinde sihirli sayılar olmamalı |
| Kısaltmalar: `usr`, `cust`, `doc`, `mng` | Tam kelimeler: `user`, `customer`, `document`, `manager` | Kısaltma yapmak modern IDE'lerde gereksizdir, okumayı zorlaştırır |
| Negatif isimlendirme: `isNotActive`, `disableValidation` | Pozitif isimlendirme: `isActive`, `enableValidation` | Negatif mantık (`!isNotActive`) beyin jimnastiği yaptırır, hataya açıktır |

### 13.8 Performance Anti-Patterns

| ❌ Yanlış (Anti-Pattern) | ✅ Doğru (Best Practice) | Açıklama |
|--------------------------|--------------------------|----------|
| Bellekte filtreleme: `repo.findAll().stream().filter(u -> u.age > 18)` | DB'de filtreleme: `repo.findByAgeGreaterThan(18)` | 1 milyon kayıt varsa `findAll()` uygulamayı OOM (Out of Memory) yapar |
| Döngü içinde sorgu: `for (id : ids) { repo.findById(id); }` | Toplu sorgu: `repo.findAllById(ids)` | 100 id için 100 sorgu atmak yerine 1 sorgu (IN clause) atılmalı |
| Ağır nesne oluşturma: `ObjectMapper mapper = new ObjectMapper();` (metot içinde) | Statik/Bean kullanımı: `private final ObjectMapper mapper;` (Constructor Injection) | ObjectMapper gibi ağır nesneler her istekte yeniden yaratılmamalı (Singleton olmalı) |
| Stream içinde side-effect: `users.stream().forEach(u -> repo.save(u));` | Batch operation: `repo.saveAll(users);` | `forEach` içinde DB çağrısı yapmak (N+1) performans katilidir |
| String birleştirme döngüde: `String s = ""; for... s = s + val;` | StringBuilder: `StringBuilder sb... sb.append(val);` | Döngü içinde String toplamak belleği şişirir (String pool) |

### 13.9 Java Best Practices

| ❌ Yanlış (Anti-Pattern) | ✅ Doğru (Best Practice) | Açıklama |
|--------------------------|--------------------------|----------|
| For loop ile filtreleme: `List<User> actives = new ArrayList<>(); for(User u : users) { if(u.isActive()) actives.add(u); }` | Stream API: `users.stream().filter(User::isActive).toList();` | Kodun okunabilirliğini artırır ve niyeti (intent) belli eder |
| Gereksiz null check: `if (list != null && !list.isEmpty())` | Empty collection: Listeler asla null dönmemeli, boş liste dönmeli | `CollectionUtils.isEmpty(list)` kullanmak veya mimariyi null dönmeyecek şekilde kurmak |
| `Optional.get()` kullanımı: `userOp.get().getName()` | Safe unwrap: `userOp.map(User::getName).orElse("Default")` | `get()` kullanmak NullPointerException riskini geri getirir |
| Tarih karşılaştırma: `date1.toString().equals(date2.toString())` | Time API: `date1.isBefore(date2)` | Tarihleri string'e çevirerek karşılaştırmak saat dilimi hatalarına yol açar |

---

## 14. Değiştirilemez Kurallar (Özet)

Aşağıdaki kuralların ihlali Code Review aşamasında **reddedilme** sebebidir:

| Kural | Durum |
|-------|-------|
| Field Injection (`@Autowired`) | ❌ Yasak |
| Controller'da `if (user == null)` | ❌ Yasak |
| Token'lar LocalStorage'da | ❌ Yasak |
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

---

> **Doküman Sonu**  
> Sorular veya öneriler için: Teknik Lider ile iletişime geçin.

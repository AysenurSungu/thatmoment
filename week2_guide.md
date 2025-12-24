# Hafta 2: Auth Devamı (Verification + Login)

## 📋 Bu Hafta Ne Yapacaksın?

Hafta 1'de register endpoint'i ve email verification kaydını oluşturdun. Bu hafta:
- Email doğrulama (kod girişi)
- Kod tekrar gönderme
- Passwordless login (email + kod)
- Device session yönetimi

---

## 🟢 Task 1.3: Email Verification

### 📊 Etkilenen Tablolar

```
Verify işleminde:

1. auth.email_verifications → UPDATE (attempt_count veya verified_at)
2. auth.users → UPDATE (is_verified, verified_at)

┌─────────────────────────────────────────────────────────────────┐
│                   auth.email_verifications                       │
├─────────────────────────────────────────────────────────────────┤
│ attempt_count      │ Yanlış kod girilince +1                    │
│ verified_at        │ Doğru kod girilince NOW()                  │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                        auth.users                                │
├─────────────────────────────────────────────────────────────────┤
│ is_verified        │ true olacak                                │
│ verified_at        │ NOW() olacak                               │
└─────────────────────────────────────────────────────────────────┘
```

### 🔄 Data Flow

```
Client                    Controller              Service                    Repository
   │                          │                      │                           │
   │ POST /verify-email       │                      │                           │
   │ {email, code}            │                      │                           │
   │ ────────────────────────>│                      │                           │
   │                          │  verifyEmail(req)    │                           │
   │                          │ ──────────────────── >│                           │
   │                          │                      │                           │
   │                          │                      │  1. User bul              │
   │                          │                      │ ─────────────────────────>│
   │                          │                      │     findByEmail()         │
   │                          │                      │ <─────────────────────────│
   │                          │                      │                           │
   │                          │                      │  2. Aktif verification bul│
   │                          │                      │ ─────────────────────────>│
   │                          │                      │     findActiveVerification│
   │                          │                      │ <─────────────────────────│
   │                          │                      │                           │
   │                          │                      │  3. Kod kontrol           │
   │                          │                      │     - Expired mı?         │
   │                          │                      │     - Max attempt mı?     │
   │                          │                      │     - Kod eşleşiyor mu?   │
   │                          │                      │                           │
   │                          │                      │  4a. Yanlış kod:          │
   │                          │                      │      attempt_count++      │
   │                          │                      │ ─────────────────────────>│
   │                          │                      │                           │
   │                          │                      │  4b. Doğru kod:           │
   │                          │                      │      verification.verify()│
   │                          │                      │      user.markAsVerified()│
   │                          │                      │ ─────────────────────────>│
   │                          │                      │                           │
   │  200 OK                  │ <────────────────────│                           │
   │  {message}               │                      │                           │
   │ <────────────────────────│                      │                           │
```

### 📁 Oluşturacağın/Güncelleyeceğin Dosyalar

```
src/main/java/com/thatmoment/auth/
├── api/
│   └── AuthController.java              ← Güncelle
├── dto/
│   └── request/
│       └── VerifyEmailRequest.java      ← Yeni
└── service/
    └── AuthService.java                 ← Güncelle
```

### 💻 VerifyEmailRequest.java

```java
package com.thatmoment.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class VerifyEmailRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Verification code is required")
    @Pattern(regexp = "^[0-9]{6}$", message = "Code must be 6 digits")
    private String code;
}
```

### 💻 AuthService.java - verifyEmail metodu

```java
@Transactional
public void verifyEmail(VerifyEmailRequest request) {
    String email = request.getEmail().toLowerCase().trim();
    String code = request.getCode();
    
    // 1. User'ı bul
    User user = userRepository.findByEmailAndDeletedAtIsNull(email)
        .orElseThrow(() -> new NotFoundException("User not found"));
    
    // 2. Zaten verified mı?
    if (user.getIsVerified()) {
        throw new BadRequestException("Email already verified");
    }
    
    // 3. Aktif verification bul
    EmailVerification verification = emailVerificationRepository
        .findActiveVerification(user.getId(), VerificationPurpose.EMAIL_VERIFY, Instant.now())
        .orElseThrow(() -> new BadRequestException("No active verification code. Please request a new one."));
    
    // 4. Kod kontrolü
    if (!verification.matches(code)) {
        // Yanlış kod - attempt sayısını artır
        verification.incrementAttempt();
        emailVerificationRepository.save(verification);
        
        int remainingAttempts = verification.getMaxAttempts() - verification.getAttemptCount();
        
        if (remainingAttempts <= 0) {
            throw new BadRequestException("Too many failed attempts. Please request a new code.");
        }
        
        throw new BadRequestException(
            String.format("Invalid code. %d attempts remaining.", remainingAttempts)
        );
    }
    
    // 5. Başarılı doğrulama
    verification.markAsVerified();
    emailVerificationRepository.save(verification);
    
    user.markAsVerified();
    userRepository.save(user);
    
    log.info("Email verified for user: {}", user.getId());
}
```

### 💻 AuthController.java - Güncelleme

```java
@PostMapping("/verify-email")
public ResponseEntity<MessageResponse> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
    authService.verifyEmail(request);
    return ResponseEntity.ok(new MessageResponse("Email verified successfully"));
}
```

### 💻 MessageResponse.java (Ortak DTO)

```java
package com.thatmoment.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MessageResponse {
    private String message;
}
```

### 📊 Veritabanında Ne Oluyor?

```sql
-- Doğru kod girildiğinde:

-- 1. email_verifications güncelle
UPDATE auth.email_verifications 
SET verified_at = NOW(), updated_at = NOW()
WHERE id = '661f9511-...';

-- 2. users güncelle
UPDATE auth.users 
SET is_verified = true, verified_at = NOW(), updated_at = NOW()
WHERE id = '550e8400-...';
```

```sql
-- Yanlış kod girildiğinde:

UPDATE auth.email_verifications 
SET attempt_count = attempt_count + 1, updated_at = NOW()
WHERE id = '661f9511-...';
```

### ⚠️ Edge Cases ve Exception'lar

| Durum | Exception | HTTP Status |
|-------|-----------|-------------|
| User bulunamadı | NotFoundException | 404 |
| Email zaten verified | BadRequestException | 400 |
| Aktif kod yok (expire olmuş veya max attempt) | BadRequestException | 400 |
| Yanlış kod | BadRequestException | 400 |
| Max attempt aşıldı | BadRequestException | 400 |

---

## 🟢 Task 1.4: Resend Verification Code

### 📊 Etkilenen Tablolar

```
Resend işleminde:

1. auth.email_verifications → UPDATE (eski kodları expire et)
2. auth.email_verifications → INSERT (yeni kod)

Rate limiting için:
3. Redis → SET/GET (rate limit counter)
```

### 🔄 Business Logic

```
1. User bul
2. Zaten verified mı? → Hata
3. Rate limit kontrolü (1 dakikada 1 istek) → Redis
4. Eski aktif kodları invalidate et
5. Yeni kod oluştur ve kaydet
6. Email gönder (TODO)
```

### 📁 Dosyalar

```
src/main/java/com/thatmoment/auth/
├── dto/
│   └── request/
│       └── ResendCodeRequest.java       ← Yeni
└── service/
    ├── AuthService.java                 ← Güncelle
    └── RateLimitService.java            ← Yeni (basit versiyon)
```

### 💻 ResendCodeRequest.java

```java
package com.thatmoment.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResendCodeRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
}
```

### 💻 RateLimitService.java (Basit Redis tabanlı)

```java
package com.thatmoment.auth.service;

import com.thatmoment.common.exception.exceptions.TooManyRequestsException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final StringRedisTemplate redisTemplate;

    /**
     * Rate limit kontrolü yap
     * 
     * @param key Unique key (örn: "resend_code:user@example.com")
     * @param limit İzin verilen istek sayısı
     * @param window Zaman penceresi
     * @throws TooManyRequestsException Limit aşıldığında
     */
    public void checkRateLimit(String key, int limit, Duration window) {
        String redisKey = "rate_limit:" + key;
        
        Long currentCount = redisTemplate.opsForValue().increment(redisKey);
        
        if (currentCount == 1) {
            // İlk istek, TTL ayarla
            redisTemplate.expire(redisKey, window);
        }
        
        if (currentCount > limit) {
            Long ttl = redisTemplate.getExpire(redisKey);
            throw new TooManyRequestsException(
                "Too many requests. Please try again later.",
                ttl != null ? ttl.intValue() : (int) window.getSeconds()
            );
        }
    }

    /**
     * Kalan süreyi al (saniye)
     */
    public Long getRemainingTime(String key) {
        return redisTemplate.getExpire("rate_limit:" + key);
    }
}
```

### 💻 TooManyRequestsException.java

```java
package com.thatmoment.common.exception.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class TooManyRequestsException extends ApiException {
    
    private final int retryAfterSeconds;

    public TooManyRequestsException(String message, int retryAfterSeconds) {
        super(message, HttpStatus.TOO_MANY_REQUESTS, "RATE_LIMIT_EXCEEDED");
        this.retryAfterSeconds = retryAfterSeconds;
    }
}
```

### 💻 AuthService.java - resendVerificationCode metodu

```java
private static final String RESEND_CODE_RATE_LIMIT_KEY = "resend_code:";
private static final int RESEND_CODE_LIMIT = 1;
private static final Duration RESEND_CODE_WINDOW = Duration.ofMinutes(1);

@Transactional
public void resendVerificationCode(ResendCodeRequest request) {
    String email = request.getEmail().toLowerCase().trim();
    
    // 1. Rate limit kontrolü
    rateLimitService.checkRateLimit(
        RESEND_CODE_RATE_LIMIT_KEY + email,
        RESEND_CODE_LIMIT,
        RESEND_CODE_WINDOW
    );
    
    // 2. User bul
    User user = userRepository.findByEmailAndDeletedAtIsNull(email)
        .orElseThrow(() -> new NotFoundException("User not found"));
    
    // 3. Zaten verified mı?
    if (user.getIsVerified()) {
        throw new BadRequestException("Email already verified");
    }
    
    // 4. Eski aktif kodları invalidate et
    emailVerificationRepository.invalidatePendingVerifications(
        user.getId(), 
        VerificationPurpose.EMAIL_VERIFY, 
        Instant.now()
    );
    
    // 5. Yeni kod oluştur
    String code = generateVerificationCode();
    
    EmailVerification verification = EmailVerification.builder()
        .userId(user.getId())
        .code(code)
        .purpose(VerificationPurpose.EMAIL_VERIFY)
        .maxAttempts(MAX_ATTEMPTS)
        .expiresAt(Instant.now().plus(CODE_EXPIRY_MINUTES, ChronoUnit.MINUTES))
        .build();
    
    emailVerificationRepository.save(verification);
    
    // 6. Email gönder (TODO)
    log.info("TODO: Resend verification code {} to {}", code, email);
}
```

### 💻 AuthController.java - Güncelleme

```java
@PostMapping("/resend-code")
public ResponseEntity<MessageResponse> resendCode(@Valid @RequestBody ResendCodeRequest request) {
    authService.resendVerificationCode(request);
    return ResponseEntity.ok(new MessageResponse("Verification code sent"));
}
```

### 📊 Redis'te Ne Oluyor?

```
// İlk istek
SET rate_limit:resend_code:user@example.com 1 EX 60

// İkinci istek (1 dakika içinde)
INCR rate_limit:resend_code:user@example.com → 2 → HATA!

// 1 dakika sonra key expire olur, tekrar istek atılabilir
```

---

## 🟢 Task 1.5: Login with Email Code (Passwordless)

### 📊 Etkilenen Tablolar

```
Login Flow:

Step 1: POST /login → Kod gönder
─────────────────────────────────
1. auth.email_verifications → INSERT (purpose: LOGIN_OTP)

Step 2: POST /login/verify → Token al
─────────────────────────────────────
1. auth.email_verifications → UPDATE (verified_at)
2. auth.users → UPDATE (last_login_at)
3. auth.sessions → INSERT (yeni oturum - Task 1.6'da)
4. auth.refresh_tokens → INSERT (Task 1.6'da)
```

### 🔄 Login Flow Diyagramı

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           PASSWORDLESS LOGIN FLOW                            │
└─────────────────────────────────────────────────────────────────────────────┘

     Step 1: Kod İste                          Step 2: Kod Doğrula & Token Al
     ─────────────────                          ──────────────────────────────

     POST /login                                POST /login/verify
     {email}                                    {email, code}
         │                                          │
         ▼                                          ▼
    ┌─────────┐                                ┌─────────┐
    │ User    │ ── Var mı? ──────────────────> │ User    │
    │ Verified│ ── Verified mı? ────────────── │ Check   │
    └─────────┘                                └─────────┘
         │                                          │
         ▼                                          ▼
    ┌─────────────┐                            ┌─────────────┐
    │ Generate    │                            │ Verify Code │
    │ 6-digit OTP │                            │ (attempt    │
    │             │                            │  tracking)  │
    └─────────────┘                            └─────────────┘
         │                                          │
         ▼                                          ▼
    ┌─────────────┐                            ┌─────────────┐
    │ Save to     │                            │ Generate    │
    │ email_      │                            │ JWT Tokens  │
    │ verifications                            │ Access +    │
    │ (LOGIN_OTP) │                            │ Refresh     │
    └─────────────┘                            └─────────────┘
         │                                          │
         ▼                                          ▼
    ┌─────────────┐                            ┌─────────────┐
    │ Send Email  │                            │ Create      │
    │ (TODO)      │                            │ Session     │
    └─────────────┘                            └─────────────┘
         │                                          │
         ▼                                          ▼
    Response:                                  Response:
    {message: "Code sent"}                     {accessToken, refreshToken, expiresIn}
```

### 📁 Dosyalar

```
src/main/java/com/thatmoment/auth/
├── dto/
│   ├── request/
│   │   ├── LoginRequest.java            ← Yeni
│   │   └── LoginVerifyRequest.java      ← Yeni
│   └── response/
│       └── AuthTokensResponse.java      ← Yeni
├── service/
│   ├── AuthService.java                 ← Güncelle
│   └── JwtService.java                  ← Yeni
└── security/
    └── JwtTokenProvider.java            ← Yeni
```

### 💻 LoginRequest.java

```java
package com.thatmoment.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
}
```

### 💻 LoginVerifyRequest.java

```java
package com.thatmoment.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class LoginVerifyRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Code is required")
    @Pattern(regexp = "^[0-9]{6}$", message = "Code must be 6 digits")
    private String code;
    
    // Device bilgisi (opsiyonel - Task 1.6'da kullanılacak)
    private String deviceId;
    private String deviceName;
    private String platform;  // IOS, ANDROID
}
```

### 💻 AuthTokensResponse.java

```java
package com.thatmoment.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthTokensResponse {
    
    @JsonProperty("access_token")
    private String accessToken;
    
    @JsonProperty("refresh_token")
    private String refreshToken;
    
    @JsonProperty("token_type")
    @Builder.Default
    private String tokenType = "Bearer";
    
    @JsonProperty("expires_in")
    private Long expiresIn;  // Saniye cinsinden
}
```

### 💻 JwtService.java

```java
package com.thatmoment.auth.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
@Slf4j
public class JwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.access-token-expiration-minutes:15}")
    private int accessTokenExpirationMinutes;

    @Value("${jwt.refresh-token-expiration-days:7}")
    private int refreshTokenExpirationDays;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Access token oluştur (kısa ömürlü - 15 dk)
     */
    public String generateAccessToken(UUID userId, String email) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(accessTokenExpirationMinutes * 60L);

        return Jwts.builder()
            .subject(userId.toString())
            .claim("email", email)
            .claim("type", "access")
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiry))
            .signWith(secretKey)
            .compact();
    }

    /**
     * Refresh token oluştur (uzun ömürlü - 7 gün)
     */
    public String generateRefreshToken(UUID userId, UUID sessionId) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(refreshTokenExpirationDays * 24L * 60L * 60L);

        return Jwts.builder()
            .subject(userId.toString())
            .claim("sessionId", sessionId.toString())
            .claim("type", "refresh")
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiry))
            .signWith(secretKey)
            .compact();
    }

    /**
     * Token'ı validate et ve claims'leri döndür
     */
    public Claims validateToken(String token) {
        try {
            return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        } catch (ExpiredJwtException e) {
            throw new TokenExpiredException("Token has expired");
        } catch (JwtException e) {
            throw new InvalidTokenException("Invalid token");
        }
    }

    /**
     * Token'dan user ID çıkar
     */
    public UUID extractUserId(String token) {
        Claims claims = validateToken(token);
        return UUID.fromString(claims.getSubject());
    }

    /**
     * Token'dan session ID çıkar (sadece refresh token için)
     */
    public UUID extractSessionId(String token) {
        Claims claims = validateToken(token);
        String sessionId = claims.get("sessionId", String.class);
        return sessionId != null ? UUID.fromString(sessionId) : null;
    }

    /**
     * Access token expiration süresini saniye olarak döndür
     */
    public long getAccessTokenExpirationSeconds() {
        return accessTokenExpirationMinutes * 60L;
    }

    /**
     * Refresh token expiration süresini saniye olarak döndür
     */
    public long getRefreshTokenExpirationSeconds() {
        return refreshTokenExpirationDays * 24L * 60L * 60L;
    }
}
```

### 💻 Custom Exception'lar

```java
// TokenExpiredException.java
package com.thatmoment.common.exception.exceptions;

import org.springframework.http.HttpStatus;

public class TokenExpiredException extends ApiException {
    public TokenExpiredException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, "TOKEN_EXPIRED");
    }
}

// InvalidTokenException.java
package com.thatmoment.common.exception.exceptions;

import org.springframework.http.HttpStatus;

public class InvalidTokenException extends ApiException {
    public InvalidTokenException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, "INVALID_TOKEN");
    }
}
```

### 💻 AuthService.java - Login metodları

```java
// Login rate limit için
private static final String LOGIN_RATE_LIMIT_KEY = "login:";
private static final int LOGIN_LIMIT = 10;
private static final Duration LOGIN_WINDOW = Duration.ofHours(1);

// OTP için
private static final int OTP_EXPIRY_MINUTES = 5;

/**
 * Step 1: Login kodu gönder
 */
@Transactional
public void sendLoginCode(LoginRequest request) {
    String email = request.getEmail().toLowerCase().trim();
    
    // 1. Rate limit kontrolü
    rateLimitService.checkRateLimit(
        LOGIN_RATE_LIMIT_KEY + email,
        LOGIN_LIMIT,
        LOGIN_WINDOW
    );
    
    // 2. User bul ve kontrol et
    User user = userRepository.findByEmailAndDeletedAtIsNull(email)
        .orElseThrow(() -> new NotFoundException("User not found"));
    
    // 3. Verified mı?
    if (!user.getIsVerified()) {
        throw new BadRequestException("Email not verified. Please verify your email first.");
    }
    
    // 4. Aktif mi?
    if (!user.getIsActive()) {
        throw new ForbiddenException("Account is suspended");
    }
    
    // 5. Kilitli mi?
    if (user.isLocked()) {
        throw new ForbiddenException("Account is temporarily locked. Please try again later.");
    }
    
    // 6. Eski OTP'leri invalidate et
    emailVerificationRepository.invalidatePendingVerifications(
        user.getId(),
        VerificationPurpose.LOGIN_OTP,
        Instant.now()
    );
    
    // 7. Yeni OTP oluştur
    String code = generateVerificationCode();
    
    EmailVerification verification = EmailVerification.builder()
        .userId(user.getId())
        .code(code)
        .purpose(VerificationPurpose.LOGIN_OTP)
        .maxAttempts(MAX_ATTEMPTS)
        .expiresAt(Instant.now().plus(OTP_EXPIRY_MINUTES, ChronoUnit.MINUTES))
        .build();
    
    emailVerificationRepository.save(verification);
    
    // 8. Email gönder (TODO)
    log.info("TODO: Send login OTP {} to {}", code, email);
}

/**
 * Step 2: Login kodunu doğrula ve token döndür
 */
@Transactional
public AuthTokensResponse verifyLoginCode(LoginVerifyRequest request) {
    String email = request.getEmail().toLowerCase().trim();
    String code = request.getCode();
    
    // 1. User bul
    User user = userRepository.findByEmailAndDeletedAtIsNull(email)
        .orElseThrow(() -> new NotFoundException("User not found"));
    
    // 2. Aktif verification bul
    EmailVerification verification = emailVerificationRepository
        .findActiveVerification(user.getId(), VerificationPurpose.LOGIN_OTP, Instant.now())
        .orElseThrow(() -> new BadRequestException("No active login code. Please request a new one."));
    
    // 3. Kod kontrolü
    if (!verification.matches(code)) {
        verification.incrementAttempt();
        emailVerificationRepository.save(verification);
        
        // Failed login attempt kaydet
        user.recordFailedLogin(5, 30); // 5 yanlış deneme → 30 dk kilitle
        userRepository.save(user);
        
        int remaining = verification.getMaxAttempts() - verification.getAttemptCount();
        if (remaining <= 0) {
            throw new BadRequestException("Too many failed attempts. Please request a new code.");
        }
        
        throw new BadRequestException(
            String.format("Invalid code. %d attempts remaining.", remaining)
        );
    }
    
    // 4. Başarılı login
    verification.markAsVerified();
    emailVerificationRepository.save(verification);
    
    user.recordSuccessfulLogin();
    userRepository.save(user);
    
    // 5. Session oluştur (Task 1.6'da detaylandırılacak)
    UUID sessionId = UUID.randomUUID(); // Geçici - Task 1.6'da DeviceSession kullanılacak
    
    // 6. Token'ları oluştur
    String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail());
    String refreshToken = jwtService.generateRefreshToken(user.getId(), sessionId);
    
    log.info("User logged in: {}", user.getId());
    
    return AuthTokensResponse.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .expiresIn(jwtService.getAccessTokenExpirationSeconds())
        .build();
}
```

### 💻 AuthController.java - Güncelleme

```java
@PostMapping("/login")
public ResponseEntity<MessageResponse> login(@Valid @RequestBody LoginRequest request) {
    authService.sendLoginCode(request);
    return ResponseEntity.ok(new MessageResponse("Login code sent to your email"));
}

@PostMapping("/login/verify")
public ResponseEntity<AuthTokensResponse> verifyLogin(@Valid @RequestBody LoginVerifyRequest request) {
    AuthTokensResponse tokens = authService.verifyLoginCode(request);
    return ResponseEntity.ok(tokens);
}
```

### 📊 Veritabanında Ne Oluyor?

```sql
-- Step 1: POST /login
-- Yeni OTP oluştur
INSERT INTO auth.email_verifications 
    (id, user_id, code, purpose, attempt_count, max_attempts, expires_at, created_at, updated_at)
VALUES 
    ('771a9622-...', '550e8400-...', '789012', 'LOGIN_OTP', 0, 3, NOW() + INTERVAL '5 minutes', NOW(), NOW());

-- Step 2: POST /login/verify (başarılı)
-- 1. Verification güncelle
UPDATE auth.email_verifications 
SET verified_at = NOW(), updated_at = NOW()
WHERE id = '771a9622-...';

-- 2. User güncelle
UPDATE auth.users 
SET last_login_at = NOW(), 
    failed_login_attempts = 0, 
    locked_until = NULL,
    updated_at = NOW()
WHERE id = '550e8400-...';

-- 3. Session oluştur (Task 1.6'da)
```

### 📝 application.yml JWT Konfigürasyonu

```yaml
jwt:
  secret: ${JWT_SECRET:your-256-bit-secret-key-here-must-be-at-least-32-chars}
  access-token-expiration-minutes: 15
  refresh-token-expiration-days: 7
```

---

## 🟢 Task 1.6: Device Session

### 📊 Etkilenen Tablolar

```
┌─────────────────────────────────────────────────────────────────┐
│                      auth.sessions                               │
├─────────────────────────────────────────────────────────────────┤
│ id                 │ UUID        │ PK                           │
│ user_id            │ UUID        │ FK → auth.users              │
│ session_token      │ VARCHAR     │ UNIQUE, indexed              │
│ device_id          │ VARCHAR     │ Client'tan gelen device ID   │
│ device_name        │ VARCHAR     │ "iPhone 15 Pro"              │
│ platform           │ VARCHAR     │ 'IOS', 'ANDROID'             │
│ ip_address         │ INET        │ Client IP                    │
│ user_agent         │ TEXT        │ HTTP User-Agent              │
│ auth_method        │ VARCHAR     │ 'EMAIL', 'GOOGLE', 'APPLE'   │
│ expires_at         │ TIMESTAMP   │ Session expire zamanı        │
│ last_activity_at   │ TIMESTAMP   │ Son aktivite                 │
│ is_active          │ BOOLEAN     │ true/false                   │
│ revoked_at         │ TIMESTAMP   │ Logout zamanı                │
│ revoked_reason     │ VARCHAR     │ 'USER_LOGOUT', 'FORCE_LOGOUT'│
│ ─────────────────────────────────────────────────────────────── │
│ created_at         │ TIMESTAMP   │ BaseEntity                   │
│ updated_at         │ TIMESTAMP   │ BaseEntity                   │
│ created_by         │ UUID        │ BaseEntity                   │
│ updated_by         │ UUID        │ BaseEntity                   │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                    auth.refresh_tokens                           │
├─────────────────────────────────────────────────────────────────┤
│ id                 │ UUID        │ PK                           │
│ user_id            │ UUID        │ FK → auth.users              │
│ session_id         │ UUID        │ FK → auth.sessions           │
│ token_hash         │ VARCHAR     │ UNIQUE (hash'lenmiş token)   │
│ device_id          │ VARCHAR     │ Device ID                    │
│ expires_at         │ TIMESTAMP   │ Token expire zamanı          │
│ used_at            │ TIMESTAMP   │ Kullanıldığında (rotation)   │
│ created_ip         │ INET        │ Token oluşturulduğundaki IP  │
│ is_active          │ BOOLEAN     │ true/false                   │
│ ─────────────────────────────────────────────────────────────── │
│ created_at         │ TIMESTAMP   │ BaseEntity                   │
│ updated_at         │ TIMESTAMP   │ BaseEntity                   │
│ created_by         │ UUID        │ BaseEntity                   │
│ updated_by         │ UUID        │ BaseEntity                   │
└─────────────────────────────────────────────────────────────────┘
```

### 🔄 Session ve Refresh Token İlişkisi

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                                                                             │
│    User                                                                     │
│      │                                                                      │
│      │ 1:N                                                                  │
│      ▼                                                                      │
│    Sessions ───────────────────────────────────────────────────────────     │
│      │         │              │              │                              │
│      │      Session 1      Session 2      Session 3                         │
│      │      (iPhone)       (Android)      (iPad)                           │
│      │         │              │              │                              │
│      │ 1:N     │              │              │                              │
│      ▼         ▼              ▼              ▼                              │
│    Refresh   Token 1.1     Token 2.1     Token 3.1                         │
│    Tokens    Token 1.2*    Token 2.2*    (active)                          │
│              (rotated)     (active)                                         │
│                                                                             │
│    * Her refresh kullanıldığında yeni token oluşur (rotation)              │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 📁 Dosyalar

```
src/main/java/com/thatmoment/auth/
├── domain/
│   ├── Session.java                     ← Yeni
│   ├── RefreshToken.java                ← Yeni
│   └── enums/
│       ├── Platform.java                ← Yeni
│       └── RevokedReason.java           ← Yeni
├── repository/
│   ├── SessionRepository.java           ← Yeni
│   └── RefreshTokenRepository.java      ← Yeni
└── service/
    ├── SessionService.java              ← Yeni
    └── AuthService.java                 ← Güncelle
```

### 💻 Platform.java

```java
package com.thatmoment.auth.domain.enums;

public enum Platform {
    IOS,
    ANDROID,
    WEB  // Gelecekte web client için
}
```

### 💻 RevokedReason.java

```java
package com.thatmoment.auth.domain.enums;

public enum RevokedReason {
    USER_LOGOUT,        // Kullanıcı logout oldu
    FORCE_LOGOUT,       // Admin tarafından çıkış yapıldı
    SECURITY,           // Güvenlik nedeniyle
    TOKEN_EXPIRED,      // Token expire oldu
    DEVICE_CHANGE,      // Cihaz değişti
    PASSWORD_CHANGE     // Şifre değişti (password varsa)
}
```

### 💻 Session.java

```java
package com.thatmoment.auth.domain;

import com.thatmoment.auth.domain.enums.AuthMethod;
import com.thatmoment.auth.domain.enums.Platform;
import com.thatmoment.auth.domain.enums.RevokedReason;
import com.thatmoment.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "sessions", schema = "auth")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Session extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "session_token", nullable = false, unique = true, length = 500)
    private String sessionToken;

    @Column(name = "device_id", length = 255)
    private String deviceId;

    @Column(name = "device_name", length = 100)
    private String deviceName;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Platform platform;

    @Column(name = "ip_address", columnDefinition = "inet")
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "text")
    private String userAgent;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_method", length = 50)
    private AuthMethod authMethod;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "last_activity_at")
    @Builder.Default
    private Instant lastActivityAt = Instant.now();

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "revoked_reason", length = 50)
    private RevokedReason revokedReason;

    // ═══════════════════════════════════════════════════════════
    // BUSINESS METHODS
    // ═══════════════════════════════════════════════════════════

    /**
     * Session'ı revoke et (logout)
     */
    public void revoke(RevokedReason reason) {
        this.isActive = false;
        this.revokedAt = Instant.now();
        this.revokedReason = reason;
    }

    /**
     * Son aktiviteyi güncelle
     */
    public void updateActivity() {
        this.lastActivityAt = Instant.now();
    }

    /**
     * Session geçerli mi?
     */
    public boolean isValid() {
        return isActive && Instant.now().isBefore(expiresAt);
    }

    /**
     * Session expire olmuş mu?
     */
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
}
```

### 💻 RefreshToken.java

```java
package com.thatmoment.auth.domain;

import com.thatmoment.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens", schema = "auth")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class RefreshToken extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "session_id")
    private UUID sessionId;

    @Column(name = "token_hash", nullable = false, unique = true, length = 255)
    private String tokenHash;

    @Column(name = "device_id", length = 255)
    private String deviceId;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "used_at")
    private Instant usedAt;

    @Column(name = "created_ip", columnDefinition = "inet")
    private String createdIp;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    // ═══════════════════════════════════════════════════════════
    // BUSINESS METHODS
    // ═══════════════════════════════════════════════════════════

    /**
     * Token kullanıldığında (rotation için)
     */
    public void markAsUsed() {
        this.isActive = false;
        this.usedAt = Instant.now();
    }

    /**
     * Token'ı deaktif et
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * Token geçerli mi?
     */
    public boolean isValid() {
        return isActive && usedAt == null && Instant.now().isBefore(expiresAt);
    }
}
```

### 💻 SessionRepository.java

```java
package com.thatmoment.auth.repository;

import com.thatmoment.auth.domain.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SessionRepository extends JpaRepository<Session, UUID> {

    /**
     * Session token ile aktif session bul
     */
    Optional<Session> findBySessionTokenAndIsActiveTrue(String sessionToken);

    /**
     * User'ın aktif session'larını listele
     */
    List<Session> findByUserIdAndIsActiveTrueOrderByLastActivityAtDesc(UUID userId);

    /**
     * User'ın tüm aktif session'larını revoke et
     */
    @Modifying
    @Query("""
        UPDATE Session s 
        SET s.isActive = false, 
            s.revokedAt = :now, 
            s.revokedReason = 'FORCE_LOGOUT'
        WHERE s.userId = :userId AND s.isActive = true
    """)
    int revokeAllUserSessions(UUID userId, Instant now);

    /**
     * Expire olmuş session'ları temizle (scheduled job için)
     */
    @Modifying
    @Query("""
        UPDATE Session s 
        SET s.isActive = false, 
            s.revokedAt = :now, 
            s.revokedReason = 'TOKEN_EXPIRED'
        WHERE s.expiresAt < :now AND s.isActive = true
    """)
    int deactivateExpiredSessions(Instant now);
}
```

### 💻 RefreshTokenRepository.java

```java
package com.thatmoment.auth.repository;

import com.thatmoment.auth.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    /**
     * Token hash ile aktif refresh token bul
     */
    Optional<RefreshToken> findByTokenHashAndIsActiveTrue(String tokenHash);

    /**
     * Session'a ait tüm refresh token'ları deaktif et
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.isActive = false WHERE rt.sessionId = :sessionId")
    int deactivateBySessionId(UUID sessionId);

    /**
     * User'ın tüm refresh token'larını deaktif et
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.isActive = false WHERE rt.userId = :userId")
    int deactivateByUserId(UUID userId);

    /**
     * Expire olmuş token'ları temizle
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.isActive = false WHERE rt.expiresAt < :now AND rt.isActive = true")
    int deactivateExpiredTokens(Instant now);
}
```

### 💻 SessionService.java

```java
package com.thatmoment.auth.service;

import com.thatmoment.auth.domain.RefreshToken;
import com.thatmoment.auth.domain.Session;
import com.thatmoment.auth.domain.enums.AuthMethod;
import com.thatmoment.auth.domain.enums.Platform;
import com.thatmoment.auth.domain.enums.RevokedReason;
import com.thatmoment.auth.repository.RefreshTokenRepository;
import com.thatmoment.auth.repository.SessionRepository;
import com.thatmoment.common.util.HashingUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionService {

    private final SessionRepository sessionRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-token-expiration-days:7}")
    private int refreshTokenExpirationDays;

    /**
     * Yeni session ve refresh token oluştur
     */
    @Transactional
    public SessionTokenPair createSession(
        UUID userId,
        String deviceId,
        String deviceName,
        Platform platform,
        String ipAddress,
        String userAgent,
        AuthMethod authMethod,
        String refreshToken
    ) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(refreshTokenExpirationDays, ChronoUnit.DAYS);

        // 1. Session oluştur
        String sessionToken = UUID.randomUUID().toString();
        
        Session session = Session.builder()
            .userId(userId)
            .sessionToken(sessionToken)
            .deviceId(deviceId)
            .deviceName(deviceName)
            .platform(platform)
            .ipAddress(ipAddress)
            .userAgent(userAgent)
            .authMethod(authMethod)
            .expiresAt(expiresAt)
            .build();

        session = sessionRepository.save(session);
        log.info("Session created: {} for user: {}", session.getId(), userId);

        // 2. Refresh token kaydet (hash'lenmiş)
        String tokenHash = HashingUtils.sha256(refreshToken);

        RefreshToken refreshTokenEntity = RefreshToken.builder()
            .userId(userId)
            .sessionId(session.getId())
            .tokenHash(tokenHash)
            .deviceId(deviceId)
            .expiresAt(expiresAt)
            .createdIp(ipAddress)
            .build();

        refreshTokenRepository.save(refreshTokenEntity);
        log.info("Refresh token created for session: {}", session.getId());

        return new SessionTokenPair(session.getId(), sessionToken);
    }

    /**
     * User'ın aktif session'larını listele
     */
    public List<Session> getActiveSessions(UUID userId) {
        return sessionRepository.findByUserIdAndIsActiveTrueOrderByLastActivityAtDesc(userId);
    }

    /**
     * Tek session'ı revoke et
     */
    @Transactional
    public void revokeSession(UUID sessionId, RevokedReason reason) {
        sessionRepository.findById(sessionId).ifPresent(session -> {
            session.revoke(reason);
            sessionRepository.save(session);
            
            // Bu session'a ait refresh token'ları da deaktif et
            refreshTokenRepository.deactivateBySessionId(sessionId);
            
            log.info("Session revoked: {} reason: {}", sessionId, reason);
        });
    }

    /**
     * User'ın tüm session'larını revoke et
     */
    @Transactional
    public void revokeAllSessions(UUID userId) {
        int count = sessionRepository.revokeAllUserSessions(userId, Instant.now());
        refreshTokenRepository.deactivateByUserId(userId);
        log.info("Revoked {} sessions for user: {}", count, userId);
    }

    /**
     * Refresh token rotation - eski token'ı kullanılmış olarak işaretle
     */
    @Transactional
    public void rotateRefreshToken(String oldTokenHash, String newRefreshToken, UUID sessionId) {
        // Eski token'ı kullanılmış olarak işaretle
        refreshTokenRepository.findByTokenHashAndIsActiveTrue(oldTokenHash)
            .ifPresent(RefreshToken::markAsUsed);

        // Yeni token oluştur
        RefreshToken oldToken = refreshTokenRepository.findByTokenHashAndIsActiveTrue(oldTokenHash)
            .orElseThrow();

        String newTokenHash = HashingUtils.sha256(newRefreshToken);
        
        RefreshToken newToken = RefreshToken.builder()
            .userId(oldToken.getUserId())
            .sessionId(sessionId)
            .tokenHash(newTokenHash)
            .deviceId(oldToken.getDeviceId())
            .expiresAt(Instant.now().plus(refreshTokenExpirationDays, ChronoUnit.DAYS))
            .createdIp(oldToken.getCreatedIp())
            .build();

        refreshTokenRepository.save(newToken);
    }

    // Helper record
    public record SessionTokenPair(UUID sessionId, String sessionToken) {}
}
```

### 💻 HashingUtils.java

```java
package com.thatmoment.common.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public final class HashingUtils {

    private HashingUtils() {}

    /**
     * SHA-256 hash oluştur
     */
    public static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
```

### 💻 AuthService.java - verifyLoginCode Güncellemesi

```java
/**
 * Step 2: Login kodunu doğrula ve token döndür (Session ile)
 */
@Transactional
public AuthTokensResponse verifyLoginCode(LoginVerifyRequest request, String ipAddress, String userAgent) {
    String email = request.getEmail().toLowerCase().trim();
    String code = request.getCode();
    
    // 1. User bul
    User user = userRepository.findByEmailAndDeletedAtIsNull(email)
        .orElseThrow(() -> new NotFoundException("User not found"));
    
    // 2. Aktif verification bul
    EmailVerification verification = emailVerificationRepository
        .findActiveVerification(user.getId(), VerificationPurpose.LOGIN_OTP, Instant.now())
        .orElseThrow(() -> new BadRequestException("No active login code. Please request a new one."));
    
    // 3. Kod kontrolü
    if (!verification.matches(code)) {
        verification.incrementAttempt();
        emailVerificationRepository.save(verification);
        
        user.recordFailedLogin(5, 30);
        userRepository.save(user);
        
        int remaining = verification.getMaxAttempts() - verification.getAttemptCount();
        if (remaining <= 0) {
            throw new BadRequestException("Too many failed attempts. Please request a new code.");
        }
        
        throw new BadRequestException(
            String.format("Invalid code. %d attempts remaining.", remaining)
        );
    }
    
    // 4. Başarılı login
    verification.markAsVerified();
    emailVerificationRepository.save(verification);
    
    user.recordSuccessfulLogin();
    userRepository.save(user);
    
    // 5. Token'ları oluştur
    UUID tempSessionId = UUID.randomUUID();
    String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail());
    String refreshToken = jwtService.generateRefreshToken(user.getId(), tempSessionId);
    
    // 6. Session ve RefreshToken kaydet
    Platform platform = parsePlatform(request.getPlatform());
    
    SessionService.SessionTokenPair sessionPair = sessionService.createSession(
        user.getId(),
        request.getDeviceId(),
        request.getDeviceName(),
        platform,
        ipAddress,
        userAgent,
        AuthMethod.EMAIL,
        refreshToken
    );
    
    // 7. Refresh token'ı doğru session ID ile yeniden oluştur
    refreshToken = jwtService.generateRefreshToken(user.getId(), sessionPair.sessionId());
    
    log.info("User logged in: {} from device: {}", user.getId(), request.getDeviceId());
    
    return AuthTokensResponse.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .expiresIn(jwtService.getAccessTokenExpirationSeconds())
        .build();
}

private Platform parsePlatform(String platformStr) {
    if (platformStr == null) return null;
    try {
        return Platform.valueOf(platformStr.toUpperCase());
    } catch (IllegalArgumentException e) {
        return null;
    }
}
```

### 📊 Veritabanında Ne Oluyor?

```sql
-- Login başarılı olduğunda:

-- 1. Session oluştur
INSERT INTO auth.sessions 
    (id, user_id, session_token, device_id, device_name, platform, ip_address, user_agent, 
     auth_method, expires_at, last_activity_at, is_active, created_at, updated_at)
VALUES 
    ('881b0733-...', '550e8400-...', 'abc123-...', 'device-xyz', 'iPhone 15 Pro', 'IOS',
     '192.168.1.1', 'ThatMoment/1.0 iOS/17.0', 'EMAIL', 
     NOW() + INTERVAL '7 days', NOW(), true, NOW(), NOW());

-- 2. Refresh token oluştur
INSERT INTO auth.refresh_tokens
    (id, user_id, session_id, token_hash, device_id, expires_at, created_ip, is_active, created_at, updated_at)
VALUES
    ('992c1844-...', '550e8400-...', '881b0733-...', 'hashed_token_here', 'device-xyz',
     NOW() + INTERVAL '7 days', '192.168.1.1', true, NOW(), NOW());
```

---

## 📋 Hafta 2 Checklist

- [ ] VerifyEmailRequest DTO oluştur
- [ ] AuthService.verifyEmail() metodu yaz
- [ ] Controller'a verify-email endpoint ekle
- [ ] ResendCodeRequest DTO oluştur
- [ ] RateLimitService (basit Redis) yaz
- [ ] TooManyRequestsException oluştur
- [ ] AuthService.resendVerificationCode() yaz
- [ ] Controller'a resend-code endpoint ekle
- [ ] LoginRequest, LoginVerifyRequest DTO'ları oluştur
- [ ] AuthTokensResponse DTO oluştur
- [ ] JwtService yaz
- [ ] AuthService.sendLoginCode() yaz
- [ ] AuthService.verifyLoginCode() yaz (session olmadan)
- [ ] Controller'a login endpoint'leri ekle
- [ ] Platform, RevokedReason enum'ları oluştur
- [ ] Session entity yaz
- [ ] RefreshToken entity yaz
- [ ] SessionRepository yaz
- [ ] RefreshTokenRepository yaz
- [ ] SessionService yaz
- [ ] HashingUtils yaz
- [ ] AuthService.verifyLoginCode() session ile güncelle
- [ ] Integration testleri yaz
- [ ] PR aç

---

## 🔑 Önemli Noktalar

1. **Refresh Token Hash'leme** → Token'ı plain text saklama, SHA-256 hash kullan
2. **Token Rotation** → Her refresh kullanımında yeni token üret, eskiyi invalidate et
3. **Rate Limiting** → Redis ile basit counter implementasyonu
4. **Session Tracking** → Her device için ayrı session, logout işlemleri için gerekli
5. **IP ve User-Agent** → Güvenlik için kaydet, şüpheli aktivite tespitinde kullan

---

## ❓ Sık Sorulan Sorular

**S: Neden refresh token hash'leniyor?**
A: Veritabanı sızıntısında token'lar ele geçirilse bile kullanılamaz. Hash'ten token üretilemez.

**S: Token rotation neden önemli?**
A: Bir token çalınsa bile sadece bir kez kullanılabilir. İkinci kullanımda sistem saldırıyı algılar.

**S: Session token ve refresh token farkı ne?**
A: Session token sunucu tarafında session'ı tanımlar. Refresh token client'ta saklanır ve yeni access token almak için kullanılır.

**S: Rate limit neden Redis'te?**
A: Distributed sistemlerde tüm instance'lar aynı counter'ı görmeli. Redis bunu sağlar.

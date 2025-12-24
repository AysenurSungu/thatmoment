# Hafta 1: Phase 0 (Altyapı) + Phase 1 Başlangıç

## 📋 Bu Hafta Ne Yapacaksın?

Bu hafta projenin temelini atacağız. Sen User entity ve Register endpoint'i yazacaksın. Ama önce hazırladığım altyapıyı anlamalısın.

---

## 🏗️ Phase 0: Altyapı

### Task 0.1 - 0.5: Docker + Spring Boot + BaseEntity + Flyway + Exception Handling

Ben bu task'ları tamamladım:

```
✅ Docker ile PostgreSQL, Redis, LocalStack çalışıyor
✅ Spring Boot projesi ayakta
✅ BaseEntity sınıfları hazır
✅ Tüm tablolar Flyway ile oluşturulmuş
✅ Exception handling yapısı kurulu
```

### 🎯 Senin için öğrenilmesi gereken: BaseEntity Hierarchy

```
BaseEntity
    │
    ├── id (UUID) ───────────────────── Her tablonun primary key'i
    ├── created_at (Instant) ────────── Kayıt oluşturulma zamanı (otomatik)
    ├── updated_at (Instant) ────────── Son güncelleme zamanı (otomatik)
    ├── created_by (UUID) ───────────── Kim oluşturdu (JWT'den gelecek)
    └── updated_by (UUID) ───────────── Kim güncelledi (JWT'den gelecek)

SoftDeletableEntity extends BaseEntity
    │
    ├── deleted_at (Instant) ────────── Silinme zamanı (null = silinmemiş)
    ├── deleted_by (UUID) ───────────── Kim sildi
    └── delete_reason (String) ──────── Neden silindi

VersionedBaseEntity extends BaseEntity
    │
    └── version (Long) ──────────────── Optimistic locking için

VersionedSoftDeletableEntity extends SoftDeletableEntity
    │
    └── version (Long) ──────────────── Optimistic locking için
```

**Neden Önemli?**
- `SoftDeletableEntity` kullanan tablolarda kayıtlar gerçekten silinmez, `deleted_at` set edilir
- Query yazarken `WHERE deleted_at IS NULL` eklemeyi unutma!
- Repository'de `findByIdAndDeletedAtIsNull()` gibi metodlar kullan

---

## 🟢 Task 1.1: User Entity

### 📊 Etkilenen Tablo

```
┌─────────────────────────────────────────────────────────────────┐
│                        auth.users                                │
├─────────────────────────────────────────────────────────────────┤
│ id                 │ UUID        │ PK, auto-generated           │
│ email              │ VARCHAR     │ UNIQUE, NOT NULL             │
│ password_hash      │ VARCHAR     │ NULL (OAuth users için)      │
│ auth_method        │ VARCHAR     │ 'email', 'google', 'apple'   │
│ is_active          │ BOOLEAN     │ DEFAULT true                 │
│ is_verified        │ BOOLEAN     │ DEFAULT false                │
│ verified_at        │ TIMESTAMP   │ Email doğrulandığında        │
│ last_login_at      │ TIMESTAMP   │ Son giriş zamanı             │
│ failed_login_attempts │ INTEGER  │ DEFAULT 0                    │
│ locked_until       │ TIMESTAMP   │ Hesap kilitliyse             │
│ ─────────────────────────────────────────────────────────────── │
│ created_at         │ TIMESTAMP   │ SoftDeletableEntity          │
│ updated_at         │ TIMESTAMP   │ SoftDeletableEntity          │
│ created_by         │ UUID        │ SoftDeletableEntity          │
│ updated_by         │ UUID        │ SoftDeletableEntity          │
│ deleted_at         │ TIMESTAMP   │ SoftDeletableEntity          │
│ deleted_by         │ UUID        │ SoftDeletableEntity          │
│ delete_reason      │ VARCHAR     │ SoftDeletableEntity          │
└─────────────────────────────────────────────────────────────────┘
```

### 📁 Oluşturacağın Dosyalar

```
src/main/java/com/thatmoment/auth/
├── domain/
│   ├── User.java                    ← Entity
│   └── enums/
│       └── AuthMethod.java          ← Enum
└── repository/
    └── UserRepository.java          ← Repository
```

### 💻 User.java - Entity

```java
package com.thatmoment.auth.domain;

import com.thatmoment.auth.domain.enums.AuthMethod;
import com.thatmoment.common.entity.SoftDeletableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "users", schema = "auth")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // JPA için
@AllArgsConstructor(access = AccessLevel.PRIVATE)   // Builder için
@Builder
public class User extends SoftDeletableEntity {

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash")
    private String passwordHash;  // OAuth users için null olabilir

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_method", nullable = false, length = 50)
    @Builder.Default
    private AuthMethod authMethod = AuthMethod.EMAIL;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_verified", nullable = false)
    @Builder.Default
    private Boolean isVerified = false;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @Column(name = "failed_login_attempts", nullable = false)
    @Builder.Default
    private Integer failedLoginAttempts = 0;

    @Column(name = "locked_until")
    private Instant lockedUntil;

    // ═══════════════════════════════════════════════════════════
    // BUSINESS METHODS - Setter yerine anlamlı metodlar kullan!
    // ═══════════════════════════════════════════════════════════

    /**
     * Email doğrulandığında çağrılır
     */
    public void markAsVerified() {
        this.isVerified = true;
        this.verifiedAt = Instant.now();
    }

    /**
     * Başarılı login sonrası çağrılır
     */
    public void recordSuccessfulLogin() {
        this.lastLoginAt = Instant.now();
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
    }

    /**
     * Başarısız login sonrası çağrılır
     */
    public void recordFailedLogin(int maxAttempts, int lockMinutes) {
        this.failedLoginAttempts++;
        if (this.failedLoginAttempts >= maxAttempts) {
            this.lockedUntil = Instant.now().plusSeconds(lockMinutes * 60L);
        }
    }

    /**
     * Hesap kilitli mi?
     */
    public boolean isLocked() {
        return lockedUntil != null && Instant.now().isBefore(lockedUntil);
    }

    /**
     * Hesabı askıya al
     */
    public void suspend() {
        this.isActive = false;
    }

    /**
     * Hesabı aktifleştir
     */
    public void activate() {
        this.isActive = true;
        this.lockedUntil = null;
        this.failedLoginAttempts = 0;
    }
}
```

### 💡 Neden Setter Yok?

```java
// ❌ YANLIŞ - Setter ile
user.setIsVerified(true);
user.setVerifiedAt(Instant.now());

// ✅ DOĞRU - Business method ile
user.markAsVerified();
```

**Avantajları:**
1. İş mantığı tek yerde (entity içinde)
2. Tutarsız state oluşamaz (isVerified=true ama verifiedAt=null gibi)
3. Kod daha okunabilir

### 💻 AuthMethod.java - Enum

```java
package com.thatmoment.auth.domain.enums;

public enum AuthMethod {
    EMAIL,      // Email + kod ile giriş
    GOOGLE,     // Google OAuth
    APPLE       // Apple Sign-In
}
```

### 💻 UserRepository.java

```java
package com.thatmoment.auth.repository;

import com.thatmoment.auth.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Email ile aktif (silinmemiş) user bul
     * deleted_at IS NULL kontrolü önemli!
     */
    Optional<User> findByEmailAndDeletedAtIsNull(String email);

    /**
     * Email var mı kontrolü (silinmemiş kayıtlarda)
     */
    boolean existsByEmailAndDeletedAtIsNull(String email);

    /**
     * JPQL ile custom query örneği
     * Aktif ve doğrulanmış user bul
     */
    @Query("""
        SELECT u FROM User u 
        WHERE u.email = :email 
        AND u.isActive = true 
        AND u.isVerified = true 
        AND u.deletedAt IS NULL
    """)
    Optional<User> findActiveVerifiedUserByEmail(String email);
}
```

### ⚠️ Dikkat Edilecekler

1. **Schema belirt:** `@Table(name = "users", schema = "auth")`
2. **Soft delete kontrolü:** Repository metodlarında `AndDeletedAtIsNull` ekle
3. **Setter kullanma:** Business metodlar yaz
4. **Builder pattern:** `@Builder` + `@NoArgsConstructor` + `@AllArgsConstructor` kombinasyonu

### ✅ Test Senaryoları

```java
@Test
void should_create_user() {
    User user = User.builder()
        .email("test@example.com")
        .authMethod(AuthMethod.EMAIL)
        .build();
    
    User saved = userRepository.save(user);
    
    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getCreatedAt()).isNotNull();  // BaseEntity otomatik doldurur
    assertThat(saved.getIsVerified()).isFalse();   // Default değer
}

@Test
void should_not_allow_duplicate_email() {
    // İlk user
    userRepository.save(User.builder().email("test@example.com").build());
    
    // Aynı email ile ikinci user
    assertThrows(DataIntegrityViolationException.class, () -> {
        userRepository.save(User.builder().email("test@example.com").build());
    });
}

@Test
void should_find_by_email() {
    User user = userRepository.save(
        User.builder().email("test@example.com").build()
    );
    
    Optional<User> found = userRepository.findByEmailAndDeletedAtIsNull("test@example.com");
    
    assertThat(found).isPresent();
    assertThat(found.get().getId()).isEqualTo(user.getId());
}

@Test
void should_not_find_deleted_user() {
    User user = userRepository.save(
        User.builder().email("test@example.com").build()
    );
    
    // Soft delete
    user.softDelete(UUID.randomUUID(), "Test deletion");
    userRepository.save(user);
    
    Optional<User> found = userRepository.findByEmailAndDeletedAtIsNull("test@example.com");
    
    assertThat(found).isEmpty();  // Silinmiş user bulunmamalı
}
```

---

## 🟢 Task 1.2: Register Endpoint

### 📊 Etkilenen Tablolar

```
Register isteği geldiğinde:

1. auth.users tablosuna INSERT
2. auth.email_verifications tablosuna INSERT

┌─────────────────────────────────────────────────────────────────┐
│                   auth.email_verifications                       │
├─────────────────────────────────────────────────────────────────┤
│ id                 │ UUID        │ PK                           │
│ user_id            │ UUID        │ FK → auth.users              │
│ code               │ VARCHAR(6)  │ 6 haneli kod                 │
│ purpose            │ VARCHAR     │ 'EMAIL_VERIFY'               │
│ attempt_count      │ INTEGER     │ Yanlış deneme sayısı         │
│ max_attempts       │ INTEGER     │ DEFAULT 3                    │
│ expires_at         │ TIMESTAMP   │ Kodun geçerlilik süresi      │
│ verified_at        │ TIMESTAMP   │ Kod doğrulandığında          │
│ ─────────────────────────────────────────────────────────────── │
│ created_at         │ TIMESTAMP   │ BaseEntity                   │
│ updated_at         │ TIMESTAMP   │ BaseEntity                   │
│ created_by         │ UUID        │ BaseEntity                   │
│ updated_by         │ UUID        │ BaseEntity                   │
└─────────────────────────────────────────────────────────────────┘
```

### 🔄 Data Flow

```
┌──────────┐      ┌────────────────┐      ┌─────────────┐      ┌──────────────────────┐
│  Client  │ ──── │ AuthController │ ──── │ AuthService │ ──── │ UserRepository       │
│          │      │                │      │             │      │ EmailVerifRepository │
└──────────┘      └────────────────┘      └─────────────┘      └──────────────────────┘
     │                    │                      │                        │
     │  POST /register    │                      │                        │
     │  {email}           │                      │                        │
     │ ──────────────────>│                      │                        │
     │                    │   register(req)      │                        │
     │                    │ ────────────────────>│                        │
     │                    │                      │                        │
     │                    │                      │  1. Email var mı?      │
     │                    │                      │ ───────────────────────>
     │                    │                      │  existsByEmail()       │
     │                    │                      │ <───────────────────────
     │                    │                      │                        │
     │                    │                      │  2. User INSERT        │
     │                    │                      │ ───────────────────────>
     │                    │                      │  save(user)            │
     │                    │                      │ <───────────────────────
     │                    │                      │                        │
     │                    │                      │  3. Kod üret (123456)  │
     │                    │                      │                        │
     │                    │                      │  4. Verification INSERT│
     │                    │                      │ ───────────────────────>
     │                    │                      │  save(verification)    │
     │                    │                      │ <───────────────────────
     │                    │                      │                        │
     │                    │                      │  5. Email gönder (TODO)│
     │                    │                      │                        │
     │                    │   RegisterResponse   │                        │
     │                    │ <────────────────────│                        │
     │  201 Created       │                      │                        │
     │  {userId, message} │                      │                        │
     │ <──────────────────│                      │                        │
```

### 📁 Oluşturacağın Dosyalar

```
src/main/java/com/thatmoment/auth/
├── api/
│   └── AuthController.java
├── dto/
│   ├── request/
│   │   └── RegisterRequest.java
│   └── response/
│       └── RegisterResponse.java
├── domain/
│   ├── User.java                        ← Task 1.1'de yaptın
│   ├── EmailVerification.java           ← Yeni
│   └── enums/
│       ├── AuthMethod.java              ← Task 1.1'de yaptın
│       └── VerificationPurpose.java     ← Yeni
├── repository/
│   ├── UserRepository.java              ← Task 1.1'de yaptın
│   └── EmailVerificationRepository.java ← Yeni
└── service/
    └── AuthService.java                 ← Yeni
```

### 💻 VerificationPurpose.java

```java
package com.thatmoment.auth.domain.enums;

public enum VerificationPurpose {
    EMAIL_VERIFY,    // Yeni kayıtta email doğrulama
    LOGIN_OTP,       // Passwordless login kodu
    PASSWORD_RESET   // Şifre sıfırlama
}
```

### 💻 EmailVerification.java

```java
package com.thatmoment.auth.domain;

import com.thatmoment.auth.domain.enums.VerificationPurpose;
import com.thatmoment.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "email_verifications", schema = "auth")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class EmailVerification extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 6)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VerificationPurpose purpose;

    @Column(name = "attempt_count", nullable = false)
    @Builder.Default
    private Integer attemptCount = 0;

    @Column(name = "max_attempts", nullable = false)
    @Builder.Default
    private Integer maxAttempts = 3;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    // ═══════════════════════════════════════════════════════════
    // BUSINESS METHODS
    // ═══════════════════════════════════════════════════════════

    /**
     * Kod expired mı?
     */
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    /**
     * Zaten doğrulanmış mı?
     */
    public boolean isAlreadyVerified() {
        return verifiedAt != null;
    }

    /**
     * Max deneme aşıldı mı?
     */
    public boolean isMaxAttemptsExceeded() {
        return attemptCount >= maxAttempts;
    }

    /**
     * Kod geçerli mi? (tüm kontroller)
     */
    public boolean isValid() {
        return !isExpired() && !isAlreadyVerified() && !isMaxAttemptsExceeded();
    }

    /**
     * Yanlış kod girildiğinde
     */
    public void incrementAttempt() {
        this.attemptCount++;
    }

    /**
     * Doğrulama başarılı
     */
    public void markAsVerified() {
        this.verifiedAt = Instant.now();
    }

    /**
     * Kod eşleşiyor mu?
     */
    public boolean matches(String inputCode) {
        return this.code.equals(inputCode);
    }
}
```

### 💻 EmailVerificationRepository.java

```java
package com.thatmoment.auth.repository;

import com.thatmoment.auth.domain.EmailVerification;
import com.thatmoment.auth.domain.enums.VerificationPurpose;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, UUID> {

    /**
     * Aktif (kullanılmamış, expire olmamış) verification bul
     */
    @Query("""
        SELECT ev FROM EmailVerification ev
        WHERE ev.userId = :userId
        AND ev.purpose = :purpose
        AND ev.verifiedAt IS NULL
        AND ev.expiresAt > :now
        AND ev.attemptCount < ev.maxAttempts
        ORDER BY ev.createdAt DESC
        LIMIT 1
    """)
    Optional<EmailVerification> findActiveVerification(
        UUID userId, 
        VerificationPurpose purpose, 
        Instant now
    );

    /**
     * User için bekleyen tüm verification'ları invalidate et
     * Yeni kod gönderildiğinde eski kodları geçersiz kıl
     */
    @Modifying
    @Query("""
        UPDATE EmailVerification ev
        SET ev.expiresAt = :now
        WHERE ev.userId = :userId
        AND ev.purpose = :purpose
        AND ev.verifiedAt IS NULL
        AND ev.expiresAt > :now
    """)
    void invalidatePendingVerifications(UUID userId, VerificationPurpose purpose, Instant now);
}
```

### 💻 RegisterRequest.java

```java
package com.thatmoment.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
}
```

### 💻 RegisterResponse.java

```java
package com.thatmoment.auth.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class RegisterResponse {
    private UUID userId;
    private String message;
}
```

### 💻 AuthService.java

```java
package com.thatmoment.auth.service;

import com.thatmoment.auth.domain.EmailVerification;
import com.thatmoment.auth.domain.User;
import com.thatmoment.auth.domain.enums.AuthMethod;
import com.thatmoment.auth.domain.enums.VerificationPurpose;
import com.thatmoment.auth.dto.request.RegisterRequest;
import com.thatmoment.auth.dto.response.RegisterResponse;
import com.thatmoment.auth.repository.EmailVerificationRepository;
import com.thatmoment.auth.repository.UserRepository;
import com.thatmoment.common.exception.exceptions.ConflictException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    
    // Config'den alınacak değerler (şimdilik hardcoded)
    private static final int CODE_LENGTH = 6;
    private static final int CODE_EXPIRY_MINUTES = 15;
    private static final int MAX_ATTEMPTS = 3;
    
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String email = request.getEmail().toLowerCase().trim();
        
        // 1. Email zaten kayıtlı mı?
        if (userRepository.existsByEmailAndDeletedAtIsNull(email)) {
            throw new ConflictException("Email already registered");
        }
        
        // 2. User oluştur
        User user = User.builder()
            .email(email)
            .authMethod(AuthMethod.EMAIL)
            .build();
        
        user = userRepository.save(user);
        log.info("User created with id: {}", user.getId());
        
        // 3. Verification kodu üret ve kaydet
        String code = generateVerificationCode();
        
        EmailVerification verification = EmailVerification.builder()
            .userId(user.getId())
            .code(code)
            .purpose(VerificationPurpose.EMAIL_VERIFY)
            .maxAttempts(MAX_ATTEMPTS)
            .expiresAt(Instant.now().plus(CODE_EXPIRY_MINUTES, ChronoUnit.MINUTES))
            .build();
        
        emailVerificationRepository.save(verification);
        log.info("Verification code created for user: {}", user.getId());
        
        // 4. Email gönder (TODO: Email service entegrasyonu)
        // emailService.sendVerificationCode(email, code);
        log.info("TODO: Send verification code {} to {}", code, email);
        
        // 5. Response dön
        return RegisterResponse.builder()
            .userId(user.getId())
            .message("Registration successful. Please check your email for verification code.")
            .build();
    }

    /**
     * 6 haneli numerik kod üret
     */
    private String generateVerificationCode() {
        int code = secureRandom.nextInt(900000) + 100000; // 100000-999999
        return String.valueOf(code);
    }
}
```

### 💻 AuthController.java

```java
package com.thatmoment.auth.api;

import com.thatmoment.auth.dto.request.RegisterRequest;
import com.thatmoment.auth.dto.response.RegisterResponse;
import com.thatmoment.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
```

### 📊 Veritabanında Ne Oluyor?

```sql
-- Register çağrıldığında:

-- 1. auth.users tablosuna INSERT
INSERT INTO auth.users (id, email, auth_method, is_active, is_verified, created_at, updated_at)
VALUES ('550e8400-...', 'user@example.com', 'EMAIL', true, false, NOW(), NOW());

-- 2. auth.email_verifications tablosuna INSERT  
INSERT INTO auth.email_verifications (id, user_id, code, purpose, attempt_count, max_attempts, expires_at, created_at, updated_at)
VALUES ('661f9511-...', '550e8400-...', '123456', 'EMAIL_VERIFY', 0, 3, NOW() + INTERVAL '15 minutes', NOW(), NOW());
```

### ⚠️ Exception Handling

```java
// ConflictException.java - Email zaten varsa
package com.thatmoment.common.exception.exceptions;

import org.springframework.http.HttpStatus;

public class ConflictException extends ApiException {
    public ConflictException(String message) {
        super(message, HttpStatus.CONFLICT, "CONFLICT");
    }
}
```

**Response (409 Conflict):**
```json
{
  "type": "https://thatmoment.com/errors/conflict",
  "title": "Conflict",
  "status": 409,
  "detail": "Email already registered",
  "instance": "/api/v1/auth/register",
  "errorCode": "CONFLICT",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### ✅ Test Senaryoları

```java
@Test
void should_register_new_user() {
    // Given
    RegisterRequest request = new RegisterRequest();
    request.setEmail("test@example.com");
    
    // When
    ResponseEntity<RegisterResponse> response = restTemplate.postForEntity(
        "/api/v1/auth/register", 
        request, 
        RegisterResponse.class
    );
    
    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody().getUserId()).isNotNull();
    
    // Verify database
    Optional<User> user = userRepository.findByEmailAndDeletedAtIsNull("test@example.com");
    assertThat(user).isPresent();
    assertThat(user.get().getIsVerified()).isFalse();
}

@Test
void should_return_409_for_duplicate_email() {
    // Given - İlk kayıt
    RegisterRequest request = new RegisterRequest();
    request.setEmail("test@example.com");
    restTemplate.postForEntity("/api/v1/auth/register", request, RegisterResponse.class);
    
    // When - Aynı email ile tekrar
    ResponseEntity<ProblemDetail> response = restTemplate.postForEntity(
        "/api/v1/auth/register", 
        request, 
        ProblemDetail.class
    );
    
    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
}

@Test
void should_return_400_for_invalid_email() {
    // Given
    RegisterRequest request = new RegisterRequest();
    request.setEmail("invalid-email");
    
    // When
    ResponseEntity<ProblemDetail> response = restTemplate.postForEntity(
        "/api/v1/auth/register", 
        request, 
        ProblemDetail.class
    );
    
    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
}
```

---

## 📋 Hafta 1 Checklist

### Senin Yapacakların

- [ ] User.java entity oluştur
- [ ] AuthMethod.java enum oluştur
- [ ] UserRepository.java oluştur
- [ ] User entity unit testleri yaz
- [ ] EmailVerification.java entity oluştur
- [ ] VerificationPurpose.java enum oluştur
- [ ] EmailVerificationRepository.java oluştur
- [ ] RegisterRequest.java DTO oluştur
- [ ] RegisterResponse.java DTO oluştur
- [ ] AuthService.java - register metodu yaz
- [ ] AuthController.java oluştur
- [ ] Integration testleri yaz
- [ ] PR aç, review bekle

### Benim Review Checklist

- [ ] Entity'ler doğru base class'ı extend ediyor mu?
- [ ] Repository metodlarında soft delete kontrolü var mı?
- [ ] Business logic entity içinde mi?
- [ ] Transaction annotation doğru mu?
- [ ] Exception handling uygun mu?
- [ ] Test coverage yeterli mi?

---

## 🔑 Önemli Noktalar

1. **Entity'de Setter Kullanma** → Business metodlar yaz
2. **Soft Delete Kontrolü** → Repository'de `DeletedAtIsNull` ekle
3. **Transaction** → Service metodlarında `@Transactional` kullan
4. **Validation** → DTO'larda Jakarta Validation annotation'ları kullan
5. **Lowercase Email** → Email'leri her zaman lowercase yap

---

## ❓ Sık Sorulan Sorular

**S: created_by alanı nasıl dolacak?**
A: Şu an için NULL kalacak. JWT entegrasyonu (Hafta 3) tamamlandığında AuditorAware ile otomatik dolacak.

**S: Email gerçekten gönderilecek mi?**
A: Hayır, şimdilik log'a yazıyoruz. Email service entegrasyonu Phase 2'de yapılacak.

**S: Verification kodu neden 6 haneli?**
A: Mobil uygulamalarda SMS/Email doğrulaması için standart. Kullanıcı kolayca yazabilir.

**S: SecureRandom neden kullanıyoruz?**
A: Normal Random tahmin edilebilir. SecureRandom kriptografik olarak güvenli rastgele sayı üretir.

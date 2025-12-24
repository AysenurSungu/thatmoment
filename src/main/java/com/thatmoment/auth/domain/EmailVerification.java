package com.thatmoment.auth.domain;

import com.thatmoment.auth.domain.enums.VerificationPurpose;
import com.thatmoment.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "email_verifications", schema = "auth")
public class EmailVerification extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "code", nullable = false, length = 6)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "purpose", nullable = false, length = 20)
    private VerificationPurpose purpose;

    @Column(name = "attempt_count", nullable = false)
    private Integer attemptCount = 0;

    @Column(name = "max_attempts", nullable = false)
    private Integer maxAttempts = 3;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    protected EmailVerification() {
    }

    private EmailVerification(Builder builder) {
        this.userId = builder.userId;
        this.code = builder.code;
        this.purpose = builder.purpose;
        this.attemptCount = builder.attemptCount != null ? builder.attemptCount : 0;
        this.maxAttempts = builder.maxAttempts != null ? builder.maxAttempts : 3;
        this.expiresAt = builder.expiresAt;
        this.verifiedAt = builder.verifiedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public UUID getUserId() {
        return userId;
    }

    public String getCode() {
        return code;
    }

    public VerificationPurpose getPurpose() {
        return purpose;
    }

    public Integer getAttemptCount() {
        return attemptCount;
    }

    public Integer getMaxAttempts() {
        return maxAttempts;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getVerifiedAt() {
        return verifiedAt;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public boolean isAlreadyVerified() {
        return verifiedAt != null;
    }

    public boolean isMaxAttemptsExceeded() {
        return attemptCount != null && maxAttempts != null && attemptCount >= maxAttempts;
    }

    public boolean isValid() {
        return !isExpired() && !isAlreadyVerified() && !isMaxAttemptsExceeded();
    }

    public void incrementAttempt() {
        int attempts = attemptCount == null ? 0 : attemptCount;
        this.attemptCount = attempts + 1;
    }

    public void markAsVerified() {
        this.verifiedAt = Instant.now();
    }

    public boolean matches(String inputCode) {
        return code != null && code.equals(inputCode);
    }

    public static final class Builder {
        private UUID userId;
        private String code;
        private VerificationPurpose purpose;
        private Integer attemptCount;
        private Integer maxAttempts;
        private Instant expiresAt;
        private Instant verifiedAt;

        private Builder() {
        }

        public Builder userId(UUID userId) {
            this.userId = userId;
            return this;
        }

        public Builder code(String code) {
            this.code = code;
            return this;
        }

        public Builder purpose(VerificationPurpose purpose) {
            this.purpose = purpose;
            return this;
        }

        public Builder attemptCount(Integer attemptCount) {
            this.attemptCount = attemptCount;
            return this;
        }

        public Builder maxAttempts(Integer maxAttempts) {
            this.maxAttempts = maxAttempts;
            return this;
        }

        public Builder expiresAt(Instant expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }

        public Builder verifiedAt(Instant verifiedAt) {
            this.verifiedAt = verifiedAt;
            return this;
        }

        public EmailVerification build() {
            return new EmailVerification(this);
        }
    }
}

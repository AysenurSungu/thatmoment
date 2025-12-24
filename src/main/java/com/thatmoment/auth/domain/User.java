package com.thatmoment.auth.domain;

import com.thatmoment.auth.domain.enums.AuthMethod;
import com.thatmoment.common.entity.SoftDeletableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "users", schema = "auth")
public class User extends SoftDeletableEntity {

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash")
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_method", nullable = false, length = 20)
    private AuthMethod authMethod = AuthMethod.EMAIL;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @Column(name = "failed_login_attempts", nullable = false)
    private Integer failedLoginAttempts = 0;

    @Column(name = "locked_until")
    private Instant lockedUntil;

    protected User() {
    }

    private User(Builder builder) {
        this.email = builder.email;
        this.passwordHash = builder.passwordHash;
        this.authMethod = builder.authMethod != null ? builder.authMethod : AuthMethod.EMAIL;
        this.isActive = builder.isActive != null ? builder.isActive : true;
        this.isVerified = builder.isVerified != null ? builder.isVerified : false;
        this.verifiedAt = builder.verifiedAt;
        this.lastLoginAt = builder.lastLoginAt;
        this.failedLoginAttempts = builder.failedLoginAttempts != null ? builder.failedLoginAttempts : 0;
        this.lockedUntil = builder.lockedUntil;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public AuthMethod getAuthMethod() {
        return authMethod;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public Boolean getIsVerified() {
        return isVerified;
    }

    public Instant getVerifiedAt() {
        return verifiedAt;
    }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }

    public Integer getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public Instant getLockedUntil() {
        return lockedUntil;
    }

    public void markAsVerified() {
        this.isVerified = true;
        this.verifiedAt = Instant.now();
    }

    public void recordSuccessfulLogin() {
        this.lastLoginAt = Instant.now();
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
    }

    public void recordFailedLogin(int maxAttempts, int lockMinutes) {
        int attempts = failedLoginAttempts == null ? 0 : failedLoginAttempts;
        attempts += 1;
        this.failedLoginAttempts = attempts;

        if (attempts >= maxAttempts) {
            Instant now = Instant.now();
            this.lockedUntil = now.plusSeconds(lockMinutes * 60L);
        }
    }

    public boolean isLocked() {
        return lockedUntil != null && Instant.now().isBefore(lockedUntil);
    }

    public void suspend() {
        this.isActive = false;
    }

    public void activate() {
        this.isActive = true;
        this.lockedUntil = null;
        this.failedLoginAttempts = 0;
    }

    public static final class Builder {
        private String email;
        private String passwordHash;
        private AuthMethod authMethod;
        private Boolean isActive;
        private Boolean isVerified;
        private Instant verifiedAt;
        private Instant lastLoginAt;
        private Integer failedLoginAttempts;
        private Instant lockedUntil;

        private Builder() {
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder passwordHash(String passwordHash) {
            this.passwordHash = passwordHash;
            return this;
        }

        public Builder authMethod(AuthMethod authMethod) {
            this.authMethod = authMethod;
            return this;
        }

        public Builder isActive(Boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public Builder isVerified(Boolean isVerified) {
            this.isVerified = isVerified;
            return this;
        }

        public Builder verifiedAt(Instant verifiedAt) {
            this.verifiedAt = verifiedAt;
            return this;
        }

        public Builder lastLoginAt(Instant lastLoginAt) {
            this.lastLoginAt = lastLoginAt;
            return this;
        }

        public Builder failedLoginAttempts(Integer failedLoginAttempts) {
            this.failedLoginAttempts = failedLoginAttempts;
            return this;
        }

        public Builder lockedUntil(Instant lockedUntil) {
            this.lockedUntil = lockedUntil;
            return this;
        }

        public User build() {
            return new User(this);
        }
    }
}

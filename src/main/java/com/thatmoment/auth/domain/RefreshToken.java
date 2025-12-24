package com.thatmoment.auth.domain;

import com.thatmoment.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens", schema = "auth")
public class RefreshToken extends BaseEntity {

    @Column(name = "session_id", nullable = false)
    private UUID sessionId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "token_hash", nullable = false, length = 64)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "used_at")
    private Instant usedAt;

    protected RefreshToken() {
    }

    private RefreshToken(Builder builder) {
        this.sessionId = builder.sessionId;
        this.userId = builder.userId;
        this.tokenHash = builder.tokenHash;
        this.expiresAt = builder.expiresAt;
        this.isActive = builder.isActive != null ? builder.isActive : true;
        this.usedAt = builder.usedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public Instant getUsedAt() {
        return usedAt;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return Boolean.TRUE.equals(isActive) && !isExpired() && usedAt == null;
    }

    public void markAsUsed() {
        this.usedAt = Instant.now();
        this.isActive = false;
    }

    public void revoke() {
        this.isActive = false;
    }

    public static final class Builder {
        private UUID sessionId;
        private UUID userId;
        private String tokenHash;
        private Instant expiresAt;
        private Boolean isActive;
        private Instant usedAt;

        private Builder() {
        }

        public Builder sessionId(UUID sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public Builder userId(UUID userId) {
            this.userId = userId;
            return this;
        }

        public Builder tokenHash(String tokenHash) {
            this.tokenHash = tokenHash;
            return this;
        }

        public Builder expiresAt(Instant expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }

        public Builder isActive(Boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public Builder usedAt(Instant usedAt) {
            this.usedAt = usedAt;
            return this;
        }

        public RefreshToken build() {
            return new RefreshToken(this);
        }
    }
}

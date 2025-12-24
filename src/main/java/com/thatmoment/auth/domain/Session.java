package com.thatmoment.auth.domain;

import com.thatmoment.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.net.InetAddress;
import java.util.UUID;

@Entity
@Table(name = "sessions", schema = "auth")
public class Session extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "session_token", nullable = false, length = 500)
    private String sessionToken;

    @Column(name = "device_name", length = 100)
    private String deviceName;

    @Column(name = "platform", length = 50)
    private String platform;

    @JdbcTypeCode(SqlTypes.INET)
    @Column(name = "ip_address")
    private InetAddress ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "last_activity_at")
    private Instant lastActivityAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "revoked_reason", length = 100)
    private String revokedReason;

    protected Session() {
    }

    private Session(Builder builder) {
        this.userId = builder.userId;
        this.sessionToken = builder.sessionToken;
        this.deviceName = builder.deviceName;
        this.platform = builder.platform;
        this.ipAddress = builder.ipAddress;
        this.userAgent = builder.userAgent;
        this.isActive = builder.isActive != null ? builder.isActive : true;
        this.lastActivityAt = builder.lastActivityAt;
        this.revokedAt = builder.revokedAt;
        this.revokedReason = builder.revokedReason;
    }

    public static Builder builder() {
        return new Builder();
    }

    public UUID getUserId() {
        return userId;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getPlatform() {
        return platform;
    }

    public InetAddress getIpAddress() {
        return ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public Instant getLastActivityAt() {
        return lastActivityAt;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }

    public String getRevokedReason() {
        return revokedReason;
    }

    public void revoke(String reason) {
        this.isActive = false;
        this.revokedAt = Instant.now();
        this.revokedReason = reason;
    }

    public void updateActivity() {
        this.lastActivityAt = Instant.now();
    }

    public boolean isValid() {
        return Boolean.TRUE.equals(isActive) && revokedAt == null;
    }

    public static final class Builder {
        private UUID userId;
        private String sessionToken;
        private String deviceName;
        private String platform;
        private InetAddress ipAddress;
        private String userAgent;
        private Boolean isActive;
        private Instant lastActivityAt;
        private Instant revokedAt;
        private String revokedReason;

        private Builder() {
        }

        public Builder userId(UUID userId) {
            this.userId = userId;
            return this;
        }

        public Builder sessionToken(String sessionToken) {
            this.sessionToken = sessionToken;
            return this;
        }

        public Builder deviceName(String deviceName) {
            this.deviceName = deviceName;
            return this;
        }

        public Builder platform(String platform) {
            this.platform = platform;
            return this;
        }

        public Builder ipAddress(InetAddress ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }

        public Builder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public Builder isActive(Boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public Builder lastActivityAt(Instant lastActivityAt) {
            this.lastActivityAt = lastActivityAt;
            return this;
        }

        public Builder revokedAt(Instant revokedAt) {
            this.revokedAt = revokedAt;
            return this;
        }

        public Builder revokedReason(String revokedReason) {
            this.revokedReason = revokedReason;
            return this;
        }

        public Session build() {
            return new Session(this);
        }
    }
}

package com.thatmoment.modules.notification.domain;

import com.fasterxml.jackson.databind.JsonNode;
import com.thatmoment.common.entity.BaseEntity;
import com.thatmoment.modules.notification.domain.enums.NotificationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "notifications", schema = "notification")
public class Notification extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private NotificationType type;

    @Column(name = "title", length = 120)
    private String title;

    @Column(name = "message", length = 500)
    private String message;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload")
    private JsonNode payload;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    protected Notification() {
    }

    private Notification(Builder builder) {
        this.userId = builder.userId;
        this.type = builder.type;
        this.title = builder.title;
        this.message = builder.message;
        this.payload = builder.payload;
        this.isRead = builder.isRead != null ? builder.isRead : false;
    }

    public static Builder builder() {
        return new Builder();
    }

    public UUID getUserId() {
        return userId;
    }

    public NotificationType getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public JsonNode getPayload() {
        return payload;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void markRead() {
        this.isRead = true;
    }

    public static final class Builder {
        private UUID userId;
        private NotificationType type;
        private String title;
        private String message;
        private JsonNode payload;
        private Boolean isRead;

        private Builder() {
        }

        public Builder userId(UUID userId) {
            this.userId = userId;
            return this;
        }

        public Builder type(NotificationType type) {
            this.type = type;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder payload(JsonNode payload) {
            this.payload = payload;
            return this;
        }

        public Builder isRead(Boolean isRead) {
            this.isRead = isRead;
            return this;
        }

        public Notification build() {
            return new Notification(this);
        }
    }
}

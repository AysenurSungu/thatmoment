package com.thatmoment.modules.notification.dto.response;

import com.fasterxml.jackson.databind.JsonNode;
import com.thatmoment.modules.notification.domain.enums.NotificationType;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        NotificationType type,
        String title,
        String message,
        JsonNode payload,
        Boolean isRead,
        Instant createdAt
) {
}

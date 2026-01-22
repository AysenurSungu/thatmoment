package com.thatmoment.modules.notification.service.impl;

import com.thatmoment.common.dto.MessageResponse;
import com.thatmoment.common.exception.exceptions.BadRequestException;
import com.thatmoment.common.exception.exceptions.NotFoundException;
import com.thatmoment.modules.notification.constants.NotificationMessages;
import com.thatmoment.modules.notification.domain.Notification;
import com.thatmoment.modules.notification.dto.response.NotificationResponse;
import com.thatmoment.modules.notification.repository.NotificationRepository;
import com.thatmoment.modules.notification.service.NotificationService;
import com.thatmoment.modules.profile.dto.request.UpdateNotificationPreferencesRequest;
import com.thatmoment.modules.profile.dto.response.NotificationPreferencesResponse;
import com.thatmoment.modules.profile.service.UserPreferencesService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Service
class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserPreferencesService userPreferencesService;

    NotificationServiceImpl(
            NotificationRepository notificationRepository,
            UserPreferencesService userPreferencesService
    ) {
        this.notificationRepository = notificationRepository;
        this.userPreferencesService = userPreferencesService;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> listNotifications(UUID userId, String status, Pageable pageable) {
        Boolean isRead = parseStatus(status);
        Page<Notification> notifications = isRead == null
                ? notificationRepository.findByUserId(userId, pageable)
                : notificationRepository.findByUserIdAndIsRead(userId, isRead, pageable);
        return notifications.map(this::toResponse);
    }

    @Override
    @Transactional
    public MessageResponse markRead(UUID userId, UUID notificationId) {
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new NotFoundException(NotificationMessages.NOTIFICATION_NOT_FOUND));
        notification.markRead();
        return MessageResponse.of(NotificationMessages.NOTIFICATION_MARKED_READ);
    }

    @Override
    @Transactional
    public MessageResponse markAllRead(UUID userId) {
        notificationRepository.markAllRead(userId);
        return MessageResponse.of(NotificationMessages.NOTIFICATIONS_MARKED_READ);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationPreferencesResponse getPreferences(UUID userId) {
        return userPreferencesService.getNotificationPreferences(userId);
    }

    @Override
    @Transactional
    public NotificationPreferencesResponse updatePreferences(UUID userId, UpdateNotificationPreferencesRequest request) {
        return userPreferencesService.updateNotificationPreferences(userId, request);
    }

    private Boolean parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        String normalized = status.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "read" -> Boolean.TRUE;
            case "unread" -> Boolean.FALSE;
            default -> throw new BadRequestException(NotificationMessages.NOTIFICATION_STATUS_INVALID);
        };
    }

    private NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getPayload(),
                notification.getIsRead(),
                notification.getCreatedAt()
        );
    }
}

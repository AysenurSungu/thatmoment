package com.thatmoment.modules.notification.service;

import com.thatmoment.common.dto.MessageResponse;
import com.thatmoment.modules.notification.dto.response.NotificationResponse;
import com.thatmoment.modules.profile.dto.request.UpdateNotificationPreferencesRequest;
import com.thatmoment.modules.profile.dto.response.NotificationPreferencesResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface NotificationService {

    Page<NotificationResponse> listNotifications(UUID userId, String status, Pageable pageable);

    MessageResponse markRead(UUID userId, UUID notificationId);

    MessageResponse markAllRead(UUID userId);

    NotificationPreferencesResponse getPreferences(UUID userId);

    NotificationPreferencesResponse updatePreferences(UUID userId, UpdateNotificationPreferencesRequest request);
}

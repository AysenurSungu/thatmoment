package com.thatmoment.modules.notification.api;

import com.thatmoment.common.constants.ApiDescriptions;
import com.thatmoment.common.dto.MessageResponse;
import com.thatmoment.modules.auth.security.UserPrincipal;
import com.thatmoment.modules.notification.dto.response.NotificationResponse;
import com.thatmoment.modules.notification.service.NotificationService;
import com.thatmoment.modules.profile.dto.request.UpdateNotificationPreferencesRequest;
import com.thatmoment.modules.profile.dto.response.NotificationPreferencesResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = ApiDescriptions.TAG_NOTIFICATION, description = ApiDescriptions.TAG_NOTIFICATION_DESC)
@PreAuthorize("isAuthenticated()")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    @Operation(summary = ApiDescriptions.NOTIFICATION_LIST_SUMMARY)
    public Page<NotificationResponse> listNotifications(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20)
            @SortDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return notificationService.listNotifications(principal.getUserId(), status, pageable);
    }

    @PostMapping("/{id}/read")
    @Operation(summary = ApiDescriptions.NOTIFICATION_READ_SUMMARY)
    public MessageResponse markRead(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id
    ) {
        return notificationService.markRead(principal.getUserId(), id);
    }

    @PostMapping("/read-all")
    @Operation(summary = ApiDescriptions.NOTIFICATION_READ_ALL_SUMMARY)
    public MessageResponse markAllRead(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return notificationService.markAllRead(principal.getUserId());
    }

    @GetMapping("/preferences")
    @Operation(summary = ApiDescriptions.NOTIFICATION_PREFERENCES_GET_SUMMARY)
    public NotificationPreferencesResponse getPreferences(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return notificationService.getPreferences(principal.getUserId());
    }

    @PutMapping("/preferences")
    @Operation(summary = ApiDescriptions.NOTIFICATION_PREFERENCES_UPDATE_SUMMARY)
    @ResponseStatus(HttpStatus.OK)
    public NotificationPreferencesResponse updatePreferences(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateNotificationPreferencesRequest request
    ) {
        return notificationService.updatePreferences(principal.getUserId(), request);
    }
}

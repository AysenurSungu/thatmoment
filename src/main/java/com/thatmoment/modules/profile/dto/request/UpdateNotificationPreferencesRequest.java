package com.thatmoment.modules.profile.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdateNotificationPreferencesRequest(
        @NotNull Boolean planNotificationsEnabled,
        @NotNull Integer planReminderMinutes,
        @NotNull Boolean routineNotificationsEnabled,
        @NotNull Boolean journalNotificationsEnabled
) {
}

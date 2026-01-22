package com.thatmoment.modules.profile.dto.response;

public record NotificationPreferencesResponse(
        Boolean planNotificationsEnabled,
        Integer planReminderMinutes,
        Boolean routineNotificationsEnabled,
        Boolean journalNotificationsEnabled
) {
}

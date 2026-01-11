package com.thatmoment.modules.profile.dto.response;

import java.time.LocalTime;
import java.util.UUID;

public record UserPreferencesResponse(
        UUID userId,
        String theme,
        String language,
        String timezone,
        String weekStartDay,
        String timeFormat,
        Boolean notificationRoutines,
        Boolean notificationAchievements,
        Boolean notificationStreaks,
        Boolean notificationDailyReminder,
        LocalTime dailyReminderTime
) {
}

package com.thatmoment.modules.profile.dto.request;

import jakarta.validation.constraints.Size;

import java.time.LocalTime;

public record UpdateUserPreferencesRequest(
        @Size(max = 20)
        String theme,
        @Size(max = 10)
        String language,
        @Size(max = 50)
        String timezone,
        @Size(max = 10)
        String weekStartDay,
        @Size(max = 5)
        String timeFormat,
        Boolean notificationRoutines,
        Boolean notificationAchievements,
        Boolean notificationStreaks,
        Boolean notificationDailyReminder,
        LocalTime dailyReminderTime
) {
}

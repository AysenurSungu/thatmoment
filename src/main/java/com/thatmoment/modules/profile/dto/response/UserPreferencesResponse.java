package com.thatmoment.modules.profile.dto.response;

import com.thatmoment.modules.profile.domain.enums.Language;
import com.thatmoment.modules.profile.domain.enums.Theme;
import com.thatmoment.modules.profile.domain.enums.TimeFormat;
import com.thatmoment.modules.profile.domain.enums.WeekStartDay;
import java.time.LocalTime;
import java.util.UUID;

public record UserPreferencesResponse(
        UUID userId,
        Theme theme,
        Language language,
        String timezone,
        WeekStartDay weekStartDay,
        TimeFormat timeFormat,
        Boolean notificationRoutines,
        Boolean notificationAchievements,
        Boolean notificationStreaks,
        Boolean notificationDailyReminder,
        LocalTime dailyReminderTime
) {
}

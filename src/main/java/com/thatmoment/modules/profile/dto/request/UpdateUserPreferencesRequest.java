package com.thatmoment.modules.profile.dto.request;

import com.thatmoment.modules.profile.domain.enums.Language;
import com.thatmoment.modules.profile.domain.enums.Theme;
import com.thatmoment.modules.profile.domain.enums.TimeFormat;
import com.thatmoment.modules.profile.domain.enums.WeekStartDay;
import jakarta.validation.constraints.Size;
import java.time.LocalTime;

public record UpdateUserPreferencesRequest(
        Theme theme,
        Language language,
        @Size(max = 50)
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

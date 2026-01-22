package com.thatmoment.modules.profile.constants;

import com.thatmoment.modules.profile.domain.enums.Language;
import com.thatmoment.modules.profile.domain.enums.Theme;
import com.thatmoment.modules.profile.domain.enums.TimeFormat;
import com.thatmoment.modules.profile.domain.enums.WeekStartDay;
import java.time.LocalTime;

public final class ProfileDefaults {

    private ProfileDefaults() {
    }

    public static final Theme THEME = Theme.LIGHT;
    public static final Language LANGUAGE = Language.TR;
    public static final String TIMEZONE = "Europe/Istanbul";
    public static final WeekStartDay WEEK_START_DAY = WeekStartDay.MONDAY;
    public static final TimeFormat TIME_FORMAT = TimeFormat.H24;
    public static final LocalTime DAILY_REMINDER_TIME = LocalTime.of(9, 0);
    public static final Boolean JOURNAL_LOCK_ENABLED = Boolean.FALSE;
    public static final Boolean PLAN_NOTIFICATIONS_ENABLED = Boolean.TRUE;
    public static final Integer PLAN_REMINDER_MINUTES = 15;
    public static final Boolean ROUTINE_NOTIFICATIONS_ENABLED = Boolean.TRUE;
    public static final Boolean JOURNAL_NOTIFICATIONS_ENABLED = Boolean.TRUE;
}

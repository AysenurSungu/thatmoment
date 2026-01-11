package com.thatmoment.modules.profile.constants;

import java.time.LocalTime;

public final class ProfileDefaults {

    private ProfileDefaults() {
    }

    public static final String THEME = "light";
    public static final String LANGUAGE = "tr";
    public static final String TIMEZONE = "Europe/Istanbul";
    public static final String WEEK_START_DAY = "monday";
    public static final String TIME_FORMAT = "24h";
    public static final LocalTime DAILY_REMINDER_TIME = LocalTime.of(9, 0);
}

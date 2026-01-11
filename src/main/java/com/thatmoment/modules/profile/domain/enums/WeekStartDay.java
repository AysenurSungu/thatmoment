package com.thatmoment.modules.profile.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Locale;

public enum WeekStartDay {
    MONDAY("monday"),
    SUNDAY("sunday");

    private final String value;

    WeekStartDay(String value) {
        this.value = value;
    }

    @JsonCreator
    public static WeekStartDay fromValue(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        for (WeekStartDay day : values()) {
            if (day.value.equals(normalized)) {
                return day;
            }
        }
        throw new IllegalArgumentException("Unknown week start day: " + value);
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}

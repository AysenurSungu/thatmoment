package com.thatmoment.modules.profile.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Locale;

public enum TimeFormat {
    H12("12h"),
    H24("24h");

    private final String value;

    TimeFormat(String value) {
        this.value = value;
    }

    @JsonCreator
    public static TimeFormat fromValue(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        for (TimeFormat format : values()) {
            if (format.value.equals(normalized)) {
                return format;
            }
        }
        throw new IllegalArgumentException("Unknown time format: " + value);
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}

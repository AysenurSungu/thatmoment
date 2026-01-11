package com.thatmoment.modules.profile.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Locale;

public enum Theme {
    LIGHT("light"),
    DARK("dark"),
    AUTO("auto");

    private final String value;

    Theme(String value) {
        this.value = value;
    }

    @JsonCreator
    public static Theme fromValue(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        for (Theme theme : values()) {
            if (theme.value.equals(normalized)) {
                return theme;
            }
        }
        throw new IllegalArgumentException("Unknown theme: " + value);
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}

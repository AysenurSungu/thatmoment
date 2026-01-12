package com.thatmoment.modules.profile.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Locale;

public enum Language {
    TR("tr"),
    EN("en");

    private final String value;

    Language(String value) {
        this.value = value;
    }

    @JsonCreator
    public static Language fromValue(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        for (Language language : values()) {
            if (language.value.equals(normalized)) {
                return language;
            }
        }
        throw new IllegalArgumentException("Unknown language: " + value);
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}

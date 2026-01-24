package com.snakeleaderboard.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Locale;

public enum GameMode {
    STANDARD,
    MAP_SELECT,
    RACE;

    @JsonCreator
    public static GameMode fromString(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("mode is blank");
        }
        return GameMode.valueOf(trimmed.toUpperCase(Locale.ROOT));
    }
}

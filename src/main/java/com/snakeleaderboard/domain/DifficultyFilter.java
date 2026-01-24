package com.snakeleaderboard.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Locale;

public enum DifficultyFilter {
    ANY,
    EASY,
    NORMAL,
    HARD,
    EXPERT,
    INSANE;

    @JsonCreator
    public static DifficultyFilter fromString(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return ANY;
        }
        return DifficultyFilter.valueOf(trimmed.toUpperCase(Locale.ROOT));
    }
}

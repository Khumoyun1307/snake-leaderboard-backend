package com.snakeleaderboard.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Locale;

public enum Difficulty {
    EASY,
    NORMAL,
    HARD,
    EXPERT,
    INSANE;

    @JsonCreator
    public static Difficulty fromString(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("difficulty is blank");
        }
        return Difficulty.valueOf(trimmed.toUpperCase(Locale.ROOT));
    }
}

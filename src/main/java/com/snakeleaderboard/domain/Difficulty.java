package com.snakeleaderboard.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Locale;

/**
 * Difficulty level for a score submission.
 */
public enum Difficulty {
    EASY,
    NORMAL,
    HARD,
    EXPERT,
    INSANE;

    /**
     * Parses a difficulty value from an API string.
     *
     * <p>Parsing is case-insensitive and trims surrounding whitespace.</p>
     *
     * @param value raw value from JSON or request binding
     * @return parsed difficulty, or {@code null} if {@code value} is {@code null}
     * @throws IllegalArgumentException if the input is blank or not a known enum constant
     */
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

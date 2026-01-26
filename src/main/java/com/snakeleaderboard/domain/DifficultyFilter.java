package com.snakeleaderboard.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Locale;

/**
 * Difficulty filter for leaderboard queries.
 *
 * <p>Includes {@link #ANY} to represent an unfiltered request.</p>
 */
public enum DifficultyFilter {
    ANY,
    EASY,
    NORMAL,
    HARD,
    EXPERT,
    INSANE;

    /**
     * Parses a difficulty filter value from an API string.
     *
     * <p>Parsing is case-insensitive and trims surrounding whitespace. Blank inputs default to
     * {@link #ANY} to support optional query parameters.</p>
     *
     * @param value raw value from JSON or request binding
     * @return parsed filter, or {@code null} if {@code value} is {@code null}
     * @throws IllegalArgumentException if the input is not a known enum constant
     */
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

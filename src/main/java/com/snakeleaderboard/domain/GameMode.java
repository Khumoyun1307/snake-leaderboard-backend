package com.snakeleaderboard.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Locale;

/**
 * Game mode a score belongs to.
 */
public enum GameMode {
    STANDARD,
    MAP_SELECT,
    RACE;

    /**
     * Parses a game mode value from an API string.
     *
     * <p>Parsing is case-insensitive and trims surrounding whitespace.</p>
     *
     * @param value raw value from JSON or request binding
     * @return parsed game mode, or {@code null} if {@code value} is {@code null}
     * @throws IllegalArgumentException if the input is blank or not a known enum constant
     */
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

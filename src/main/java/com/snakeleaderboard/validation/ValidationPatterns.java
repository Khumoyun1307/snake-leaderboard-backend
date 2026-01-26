package com.snakeleaderboard.validation;

/**
 * Centralized regular expressions used for request validation.
 */
public final class ValidationPatterns {
    /** Supported {@code mode} values (case-insensitive). */
    public static final String MODE_PATTERN = "(?i)^(MAP_SELECT|RACE|STANDARD)$";
    /** Supported {@code difficulty} values (case-insensitive). */
    public static final String DIFFICULTY_PATTERN = "(?i)^(EASY|NORMAL|HARD|EXPERT|INSANE)$";
    /** Supported difficulty filter values, including blank and {@code ANY} (case-insensitive). */
    public static final String DIFFICULTY_FILTER_PATTERN = "(?i)^(?:$|ANY|EASY|NORMAL|HARD|EXPERT|INSANE)$";

    private ValidationPatterns() {}
}

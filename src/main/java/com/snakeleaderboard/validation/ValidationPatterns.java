package com.snakeleaderboard.validation;

public final class ValidationPatterns {
    public static final String MODE_PATTERN = "(?i)^(MAP_SELECT|RACE|STANDARD)$";
    public static final String DIFFICULTY_PATTERN = "(?i)^(EASY|NORMAL|HARD|EXPERT|INSANE)$";
    public static final String DIFFICULTY_FILTER_PATTERN = "(?i)^(?:$|ANY|EASY|NORMAL|HARD|EXPERT|INSANE)$";

    private ValidationPatterns() {}
}

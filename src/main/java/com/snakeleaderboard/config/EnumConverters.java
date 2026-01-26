package com.snakeleaderboard.config;

import com.snakeleaderboard.domain.Difficulty;
import com.snakeleaderboard.domain.DifficultyFilter;
import com.snakeleaderboard.domain.GameMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;

/**
 * Registers Spring MVC converters for binding enum-like request parameter values.
 *
 * <p>The API accepts case-insensitive string values; {@code null} inputs remain {@code null}.</p>
 */
@Configuration
public class EnumConverters {

    /**
     * Converts a request value into a {@link GameMode}.
     *
     * @return the Spring converter used for request parameter binding
     */
    @Bean
    public Converter<String, GameMode> gameModeConverter() {
        return value -> value == null ? null : GameMode.fromString(value);
    }

    /**
     * Converts a request value into a {@link Difficulty}.
     *
     * @return the Spring converter used for request parameter binding
     */
    @Bean
    public Converter<String, Difficulty> difficultyConverter() {
        return value -> value == null ? null : Difficulty.fromString(value);
    }

    /**
     * Converts a request value into a {@link DifficultyFilter}.
     *
     * @return the Spring converter used for request parameter binding
     */
    @Bean
    public Converter<String, DifficultyFilter> difficultyFilterConverter() {
        return value -> value == null ? null : DifficultyFilter.fromString(value);
    }
}

package com.snakeleaderboard.config;

import com.snakeleaderboard.domain.Difficulty;
import com.snakeleaderboard.domain.DifficultyFilter;
import com.snakeleaderboard.domain.GameMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;

@Configuration
public class EnumConverters {

    @Bean
    public Converter<String, GameMode> gameModeConverter() {
        return value -> value == null ? null : GameMode.fromString(value);
    }

    @Bean
    public Converter<String, Difficulty> difficultyConverter() {
        return value -> value == null ? null : Difficulty.fromString(value);
    }

    @Bean
    public Converter<String, DifficultyFilter> difficultyFilterConverter() {
        return value -> value == null ? null : DifficultyFilter.fromString(value);
    }
}

package com.snakeleaderboard.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Enables scheduled task execution (used for periodic cleanup jobs).
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {}

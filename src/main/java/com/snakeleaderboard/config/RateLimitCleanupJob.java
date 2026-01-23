package com.snakeleaderboard.config;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Component
public class RateLimitCleanupJob {

    private static final Duration WINDOW = Duration.ofMinutes(1);
    private final JdbcClient jdbc;

    public RateLimitCleanupJob(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Scheduled(fixedDelay = 60 * 60 * 1000) // every hour
    public void pruneExpired() {
        OffsetDateTime cutoff = OffsetDateTime.now(ZoneOffset.UTC).minus(WINDOW);
        jdbc.sql("""
                DELETE FROM rate_limits
                WHERE window_start <= ?
                """)
                .params(cutoff)
                .update();
    }
}

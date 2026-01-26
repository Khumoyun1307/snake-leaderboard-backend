package com.snakeleaderboard.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled maintenance job that deletes expired API sessions.
 */
@Component
public class SessionCleanupJob {
    private final SessionService sessionService;

    public SessionCleanupJob(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    /**
     * Deletes any sessions that have passed their expiry time.
     */
    @Scheduled(fixedDelay = 60 * 60 * 1000) // every hour
    public void cleanupExpired() {
        sessionService.deleteExpiredSessions();
    }
}

package com.snakeleaderboard.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SessionCleanupJob {
    /** Session Cleanup feature **/
    private final SessionService sessionService;

    public SessionCleanupJob(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Scheduled(fixedDelay = 60 * 60 * 1000) // every hour
    public void cleanupExpired() {
        sessionService.deleteExpiredSessions();
    }
}

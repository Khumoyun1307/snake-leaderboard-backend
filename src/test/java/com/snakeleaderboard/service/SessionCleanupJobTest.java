package com.snakeleaderboard.service;

import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SessionCleanupJobTest {

    @Test
    void cleanupExpired_callsService() {
        SessionService sessionService = mock(SessionService.class);
        SessionCleanupJob job = new SessionCleanupJob(sessionService);

        job.cleanupExpired();

        verify(sessionService).deleteExpiredSessions();
    }
}

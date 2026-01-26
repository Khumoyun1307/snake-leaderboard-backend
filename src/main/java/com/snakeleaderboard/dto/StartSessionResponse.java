package com.snakeleaderboard.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Response payload returned when creating a new session.
 *
 * @param sessionId session identifier (sent as {@code X-Session-Id})
 * @param sessionToken bearer token (sent as {@code X-Session-Token})
 * @param expiresAt expiry time for the session
 */
public record StartSessionResponse(
        UUID sessionId,
        String sessionToken,
        Instant expiresAt
) {}

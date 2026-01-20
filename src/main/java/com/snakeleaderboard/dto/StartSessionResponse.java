package com.snakeleaderboard.dto;

import java.time.Instant;
import java.util.UUID;

public record StartSessionResponse(
        UUID sessionId,
        String sessionToken,
        Instant expiresAt
) {}

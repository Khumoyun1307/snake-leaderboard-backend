package com.snakeleaderboard.repository;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ScoreWrite(
        UUID id,
        UUID playerId,
        String playerName,
        int score,
        int mapId,
        String mode,
        String difficulty,
        Long timeSurvivedMs,
        String gameVersion,
        OffsetDateTime createdAt
) {}

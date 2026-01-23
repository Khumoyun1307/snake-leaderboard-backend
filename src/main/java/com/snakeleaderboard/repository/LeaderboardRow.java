package com.snakeleaderboard.repository;

import java.time.OffsetDateTime;

public record LeaderboardRow(
        int mapId,
        String playerName,
        String difficulty,
        int score,
        Long timeSurvivedMs,
        OffsetDateTime createdAt
) {}

package com.snakeleaderboard.dto;

import java.time.Instant;

public record LeaderboardEntry(
        int rank,
        String playerName,
        int mapId,
        String difficulty,
        int score,
        long timeSurvivedMs,
        Instant createdAt
) {}

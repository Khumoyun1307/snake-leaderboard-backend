package com.snakeleaderboard.repository;

import java.time.OffsetDateTime;

/**
 * Projection of a score record used to build leaderboard responses.
 *
 * @param mapId map identifier for the score
 * @param playerName player display name at the time of submission
 * @param difficulty difficulty name for the score
 * @param score score value (higher is better)
 * @param timeSurvivedMs optional tie-breaker duration in milliseconds
 * @param createdAt time the score was recorded
 */
public record LeaderboardRow(
        int mapId,
        String playerName,
        String difficulty,
        int score,
        Long timeSurvivedMs,
        OffsetDateTime createdAt
) {}

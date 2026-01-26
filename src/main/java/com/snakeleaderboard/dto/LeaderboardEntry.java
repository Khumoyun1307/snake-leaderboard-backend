package com.snakeleaderboard.dto;

import java.time.Instant;

/**
 * A single leaderboard row returned to API consumers.
 *
 * @param rank 1-based rank within the queried page (includes {@code offset})
 * @param playerName display name for the player at the time of submission
 * @param mapId map identifier for this score
 * @param difficulty difficulty name for this score
 * @param score score value (higher is better)
 * @param timeSurvivedMs optional tie-breaker duration in milliseconds
 * @param createdAt time the score was recorded
 */
public record LeaderboardEntry(
        int rank,
        String playerName,
        int mapId,
        String difficulty,
        int score,
        long timeSurvivedMs,
        Instant createdAt
) {}

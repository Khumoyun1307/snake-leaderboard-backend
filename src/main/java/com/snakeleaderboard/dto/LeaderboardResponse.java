package com.snakeleaderboard.dto;

import java.util.List;

/**
 * Response payload for leaderboard queries.
 *
 * @param mapId map identifier from the request
 * @param mode game mode name from the request
 * @param difficulty optional difficulty filter name from the request (may be {@code null})
 * @param entries page of leaderboard entries
 */
public record LeaderboardResponse(
        int mapId,
        String mode,
        String difficulty,
        List<LeaderboardEntry> entries
) {}

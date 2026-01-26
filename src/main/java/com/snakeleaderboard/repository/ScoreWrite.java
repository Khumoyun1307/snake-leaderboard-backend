package com.snakeleaderboard.repository;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Internal write model for score persistence.
 *
 * @param id score row id (used for inserts)
 * @param playerId player identifier
 * @param playerName player display name
 * @param score score value (higher is better)
 * @param mapId map identifier
 * @param mode game mode name (stored in DB)
 * @param difficulty difficulty name (stored in DB)
 * @param timeSurvivedMs optional tie-breaker duration in milliseconds
 * @param gameVersion optional client/game version string
 * @param createdAt score timestamp
 */
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

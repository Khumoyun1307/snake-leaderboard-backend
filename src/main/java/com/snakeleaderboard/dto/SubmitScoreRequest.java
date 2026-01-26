package com.snakeleaderboard.dto;

import com.snakeleaderboard.domain.Difficulty;
import com.snakeleaderboard.domain.GameMode;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Request payload for submitting a score.
 *
 * @param playerId stable player identifier
 * @param playerName player display name (validated)
 * @param score score value (validated)
 * @param mapId map identifier (validated)
 * @param mode game mode
 * @param difficulty difficulty level
 * @param timeSurvivedMs optional tie-breaker duration in milliseconds
 * @param gameVersion optional client/game version string
 */
public record SubmitScoreRequest(

        @NotNull
        UUID playerId,

        @NotBlank
        @Size(min = 1, max = 24)
        @Pattern(regexp = "^[A-Za-z0-9 _-]+$", message = "playerName may contain letters, numbers, space, _, -")
        String playerName,

        @NotNull
        @Min(0)
        @Max(2_000_000) // sanity limit; adjust later
        Integer score,

        @NotNull
        @Min(0)
        @Max(10_000) // sanity limit; adjust later
        Integer mapId,

        @NotNull
        GameMode mode,

        @NotNull
        Difficulty difficulty,

        @Min(0)
        @Max(86_400_000) // max 24h in ms
        Long timeSurvivedMs,

        @Size(max = 32)
        String gameVersion
) {}

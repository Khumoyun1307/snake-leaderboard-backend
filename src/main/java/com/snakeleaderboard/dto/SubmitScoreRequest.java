package com.snakeleaderboard.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SubmitScoreRequest(

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

        @NotBlank
        @Size(max = 32)
        String mode,

        @NotBlank
        @Size(max = 32)
        String difficulty,

        @Min(0)
        @Max(86_400_000) // max 24h in ms
        Long timeSurvivedMs,

        @Size(max = 32)
        String gameVersion
) {}

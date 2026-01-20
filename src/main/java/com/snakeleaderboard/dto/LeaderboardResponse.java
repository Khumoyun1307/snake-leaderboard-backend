package com.snakeleaderboard.dto;

import java.util.List;

public record LeaderboardResponse(
        int mapId,
        String mode,
        String difficulty,
        List<LeaderboardEntry> entries
) {}

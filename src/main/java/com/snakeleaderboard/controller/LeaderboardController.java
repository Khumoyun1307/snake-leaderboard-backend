package com.snakeleaderboard.controller;

import com.snakeleaderboard.domain.DifficultyFilter;
import com.snakeleaderboard.domain.GameMode;
import com.snakeleaderboard.dto.LeaderboardResponse;
import com.snakeleaderboard.service.LeaderboardService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

@Validated
@RestController
@RequestMapping("/api")
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    public LeaderboardController(LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }

    @GetMapping("/leaderboard")
    public LeaderboardResponse leaderboard(
            @RequestParam
            @Min(0)
            @Max(10_000)
            int mapId,
            @RequestParam
            GameMode mode,
            @RequestParam(required = false)
            DifficultyFilter difficulty,
            @RequestParam(defaultValue = "10")
            @Min(1)
            @Max(50)
            int limit,
            @RequestParam(defaultValue = "0")
            @Min(0)
            @Max(10_000)
            int offset
    ) {
        String difficultyValue = difficulty == null ? null : difficulty.name();
        return new LeaderboardResponse(
                mapId,
                mode.name(),
                difficultyValue,
                leaderboardService.getTop(mapId, mode.name(), difficultyValue, limit, offset)
        );
    }
}

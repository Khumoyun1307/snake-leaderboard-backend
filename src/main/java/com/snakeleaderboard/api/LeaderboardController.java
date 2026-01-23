package com.snakeleaderboard.api;

import com.snakeleaderboard.dto.LeaderboardResponse;
import com.snakeleaderboard.service.LeaderboardService;
import com.snakeleaderboard.validation.ValidationPatterns;
import jakarta.validation.constraints.Pattern;
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
            @RequestParam int mapId,
            @RequestParam
            @Pattern(regexp = ValidationPatterns.MODE_PATTERN, message = "mode must be MAP_SELECT or RACE")
            String mode,
            @RequestParam(required = false)
            @Pattern(regexp = ValidationPatterns.DIFFICULTY_FILTER_PATTERN, message = "difficulty must be ANY, EASY, NORMAL, or HARD")
            String difficulty,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int offset
    ) {
        return new LeaderboardResponse(
                mapId,
                mode,
                difficulty,
                leaderboardService.getTop(mapId, mode, difficulty, limit, offset)
        );
    }
}

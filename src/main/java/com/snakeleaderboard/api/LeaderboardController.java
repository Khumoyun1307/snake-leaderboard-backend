package com.snakeleaderboard.api;

import com.snakeleaderboard.dto.LeaderboardResponse;
import com.snakeleaderboard.service.LeaderboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
            @RequestParam String mode,
            @RequestParam(required = false) String difficulty,
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

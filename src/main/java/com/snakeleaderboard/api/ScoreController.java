package com.snakeleaderboard.api;

import com.snakeleaderboard.dto.SubmitScoreRequest;
import com.snakeleaderboard.service.ScoreService;
import com.snakeleaderboard.service.SessionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.snakeleaderboard.error.UnauthorizedException;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class ScoreController {

    private final SessionService sessionService;
    private final ScoreService scoreService;

    public ScoreController(SessionService sessionService, ScoreService scoreService) {
        this.sessionService = sessionService;
        this.scoreService = scoreService;
    }

    @PostMapping("/scores")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> submitScore(
            @RequestHeader("X-Session-Id") UUID sessionId,
            @RequestHeader("X-Session-Token") String sessionToken,
            @Valid @RequestBody SubmitScoreRequest request
    ) {
        if (!sessionService.isValidSession(sessionId, sessionToken)) {
            throw new UnauthorizedException("Invalid or expired session");
        }

        UUID scoreId = scoreService.saveScore(request);
        return Map.of("scoreId", scoreId);
    }

}

package com.snakeleaderboard.controller;

import com.snakeleaderboard.dto.SubmitScoreRequest;
import com.snakeleaderboard.service.ScoreService;
import com.snakeleaderboard.service.SessionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.snakeleaderboard.error.UnauthorizedException;

import java.util.Map;
import java.util.UUID;

/**
 * API endpoint for submitting player scores.
 *
 * <p>Requests must include a valid session id/token pair, obtained from {@code POST /api/session}.</p>
 */
@RestController
@RequestMapping("/api")
public class ScoreController {

    private final SessionService sessionService;
    private final ScoreService scoreService;

    public ScoreController(SessionService sessionService, ScoreService scoreService) {
        this.sessionService = sessionService;
        this.scoreService = scoreService;
    }

    /**
     * Submits a score for a player.
     *
     * @param sessionId session id returned from {@code POST /api/session}
     * @param sessionToken session token returned from {@code POST /api/session}
     * @param request score details (validated)
     * @return a map containing the persisted {@code scoreId}
     * @throws UnauthorizedException when the session is invalid or expired
     */
    @PostMapping("/scores")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> submitScore(
            @RequestHeader("X-Session-Id") UUID sessionId,
            @RequestHeader("X-Session-Token") String sessionToken,
            @Valid @RequestBody SubmitScoreRequest request
    ) {
        if (!sessionService.isValidSession(sessionId, sessionToken, request.playerId())) {
            throw new UnauthorizedException("Invalid or expired session");
        }

        UUID scoreId = scoreService.saveScore(request);
        return Map.of("scoreId", scoreId);
    }

}

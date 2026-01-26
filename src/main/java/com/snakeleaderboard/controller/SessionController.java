package com.snakeleaderboard.controller;

import com.snakeleaderboard.dto.StartSessionResponse;
import com.snakeleaderboard.service.SessionService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * API endpoint for creating short-lived submission sessions.
 *
 * <p>Sessions are used to prevent anonymous score spam and to support per-session validation.</p>
 */
@RestController
@RequestMapping("/api")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    /**
     * Creates a new session and returns its bearer token.
     *
     * @return the session id/token pair and its expiration time
     */
    @PostMapping("/session")
    public StartSessionResponse startSession() {
        return sessionService.createSession();
    }
}

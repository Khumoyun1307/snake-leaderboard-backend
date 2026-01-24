package com.snakeleaderboard.controller;

import com.snakeleaderboard.dto.StartSessionResponse;
import com.snakeleaderboard.service.SessionService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping("/session")
    public StartSessionResponse startSession() {
        return sessionService.createSession();
    }
}

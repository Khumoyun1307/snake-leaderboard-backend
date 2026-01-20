package com.snakeleaderboard.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {

    /**
     * API info endpoint (moved off "/") so the website can use "/" for index.html.
     */
    @GetMapping("/api")
    public Map<String, Object> apiRoot() {
        return Map.of(
                "service", "snake-leaderboard-api",
                "status", "ok"
        );
    }

    /**
     * Health check endpoint (kept).
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("status", "up");
    }
}

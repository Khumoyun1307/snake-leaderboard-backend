package com.snakeleaderboard.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Lightweight endpoints for service discovery and health checks.
 */
@RestController
public class HealthController {

    /**
     * Returns basic service metadata.
     *
     * <p>This endpoint is intentionally served from {@code /api} (instead of {@code /}) so the
     * website can use {@code /} for {@code index.html}.</p>
     */
    @GetMapping("/api")
    public Map<String, Object> apiRoot() {
        return Map.of(
                "service", "snake-leaderboard-api",
                "status", "ok"
        );
    }

    /**
     * Health check endpoint.
     *
     * <p>Returns {@code {"status":"up"}} when the process is running.</p>
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("status", "up");
    }
}

package com.snakeleaderboard.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter implements Filter {

    private static final int LIMIT = 30; // requests
    private static final long WINDOW_MS = Duration.ofMinutes(1).toMillis();

    private static class CounterWindow {
        long windowStartMs;
        int count;
        CounterWindow(long windowStartMs, int count) {
            this.windowStartMs = windowStartMs;
            this.count = count;
        }
    }

    private final Map<String, CounterWindow> counters = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        // Only rate limit score submissions (recommended)
        if (!("POST".equalsIgnoreCase(req.getMethod()) && req.getRequestURI().equals("/api/scores"))) {
            chain.doFilter(request, response);
            return;
        }

        String ip = req.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) ip = req.getRemoteAddr();
        else ip = ip.split(",")[0].trim();

        long now = System.currentTimeMillis();

        CounterWindow w = counters.compute(ip, (k, existing) -> {
            if (existing == null || (now - existing.windowStartMs) >= WINDOW_MS) {
                return new CounterWindow(now, 1);
            }
            existing.count += 1;
            return existing;
        });

        if (w.count <= LIMIT) {
            chain.doFilter(request, response);
            return;
        }

        res.setStatus(429);
        res.setContentType("application/json");
        res.getWriter().write("{\"error\":\"Too Many Requests\"}");
    }
}

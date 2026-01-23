package com.snakeleaderboard.service;

import com.snakeleaderboard.dto.StartSessionResponse;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class SessionService {

    private static final Duration SESSION_TTL = Duration.ofMinutes(30);
    private final JdbcClient jdbc;
    private final SecureRandom secureRandom = new SecureRandom();

    public SessionService(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public StartSessionResponse createSession() {
        UUID sessionId = UUID.randomUUID();
        String token = generateToken(32);
        String tokenHash = sha256Hex(token);

        var now = java.time.OffsetDateTime.now(java.time.ZoneOffset.UTC);
        var expiresAt = now.plus(SESSION_TTL);

        jdbc.sql("""
            INSERT INTO sessions (id, token_hash, created_at, expires_at)
            VALUES (?, ?, ?, ?)
            """)
                .params(sessionId, tokenHash, now, expiresAt)
                .update();

        // response still uses Instant to keep API clean
        return new StartSessionResponse(sessionId, token, expiresAt.toInstant());
    }

    public boolean isValidSession(UUID sessionId, String token) {
        if (sessionId == null || token == null || token.isBlank()) return false;

        String tokenHash = sha256Hex(token);

        Integer count = jdbc.sql("""
            SELECT COUNT(*) FROM sessions
            WHERE id = ? AND token_hash = ? AND expires_at > ?
            """)
                .params(sessionId, tokenHash,
                        java.time.OffsetDateTime.now(java.time.ZoneOffset.UTC))
                .query(Integer.class)
                .single();

        return count != null && count == 1;
    }

    private String generateToken(int numBytes) {
        byte[] bytes = new byte[numBytes];
        secureRandom.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    private String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    public int deleteExpiredSessions() {
        return jdbc.sql("""
        DELETE FROM sessions
        WHERE expires_at <= ?
        """)
                .params(java.time.OffsetDateTime.now(java.time.ZoneOffset.UTC))
                .update();
    }
}

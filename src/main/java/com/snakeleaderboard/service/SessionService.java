package com.snakeleaderboard.service;

import com.snakeleaderboard.dto.StartSessionResponse;
import com.snakeleaderboard.repository.SessionRepository;
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
    private final SessionRepository sessionRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public SessionService(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    public StartSessionResponse createSession() {
        UUID sessionId = UUID.randomUUID();
        String token = generateToken(32);
        String tokenHash = sha256Hex(token);

        var now = java.time.OffsetDateTime.now(java.time.ZoneOffset.UTC);
        var expiresAt = now.plus(SESSION_TTL);

        sessionRepository.insertSession(sessionId, tokenHash, now, expiresAt);

        // response still uses Instant to keep API clean
        return new StartSessionResponse(sessionId, token, expiresAt.toInstant());
    }

    public boolean isValidSession(UUID sessionId, String token, UUID playerId) {
        if (sessionId == null || token == null || token.isBlank() || playerId == null) return false;

        String tokenHash = sha256Hex(token);

        int count = sessionRepository.countValidSessions(
                sessionId,
                tokenHash,
                playerId,
                java.time.OffsetDateTime.now(java.time.ZoneOffset.UTC)
        );

        return count == 1;
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
        return sessionRepository.deleteExpiredSessions(java.time.OffsetDateTime.now(java.time.ZoneOffset.UTC));
    }
}

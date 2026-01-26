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

/**
 * Issues and validates short-lived sessions for score submissions.
 *
 * <p>Sessions use a random bearer token. Only a SHA-256 hash of the token is stored server-side; the
 * raw token is returned once to the client. Treat the token like a password and only send it over
 * HTTPS.</p>
 */
@Service
public class SessionService {

    private static final Duration SESSION_TTL = Duration.ofMinutes(30);
    private final SessionRepository sessionRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public SessionService(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    /**
     * Creates a new session and persists its hashed token.
     *
     * @return session id/token pair and expiration time
     */
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

    /**
     * Validates a session id/token pair and binds the session to a player id.
     *
     * <p>The underlying repository call performs an {@code UPDATE} that succeeds only when the
     * session exists, matches the token hash, has not expired, and is either unclaimed or already
     * bound to the provided {@code playerId}.</p>
     *
     * @param sessionId session id
     * @param token raw bearer token
     * @param playerId player identifier attempting to use the session
     * @return {@code true} when the session is valid; {@code false} otherwise
     */
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

    /**
     * Generates a hex-encoded random token.
     *
     * @param numBytes number of random bytes to generate
     * @return lowercase hex token
     */
    private String generateToken(int numBytes) {
        byte[] bytes = new byte[numBytes];
        secureRandom.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    /**
     * Computes the SHA-256 hash of {@code input} and returns it as lowercase hex.
     */
    private String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /**
     * Deletes all expired sessions.
     *
     * @return number of sessions deleted
     */
    public int deleteExpiredSessions() {
        return sessionRepository.deleteExpiredSessions(java.time.OffsetDateTime.now(java.time.ZoneOffset.UTC));
    }
}

package com.snakeleaderboard.repository;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * JDBC-based persistence operations for short-lived API sessions.
 */
@Repository
public class SessionRepository {

    private final JdbcClient jdbc;

    public SessionRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Persists a newly created session.
     *
     * @param sessionId session id to store
     * @param tokenHash SHA-256 hash of the bearer token (the raw token is never stored)
     * @param createdAt creation time in UTC
     * @param expiresAt expiry time in UTC
     */
    public void insertSession(UUID sessionId, String tokenHash, OffsetDateTime createdAt, OffsetDateTime expiresAt) {
        jdbc.sql("""
                INSERT INTO sessions (id, token_hash, created_at, expires_at)
                VALUES (?, ?, ?, ?)
                """)
                .params(sessionId, tokenHash, createdAt, expiresAt)
                .update();
    }

    /**
     * Validates a session and binds it to a player id.
     *
     * <p>This method performs an {@code UPDATE} that succeeds only when the session exists, the
     * token hash matches, the session has not expired, and the session is either unclaimed or
     * already bound to the provided {@code playerId}. When successful, the session is bound to the
     * player via {@code player_id = COALESCE(player_id, ?)}.</p>
     *
     * @return number of rows updated (1 indicates a valid session; 0 indicates invalid/expired)
     */
    public int countValidSessions(UUID sessionId, String tokenHash, UUID playerId, OffsetDateTime now) {
        return jdbc.sql("""
                UPDATE sessions
                SET player_id = COALESCE(player_id, ?)
                WHERE id = ?
                  AND token_hash = ?
                  AND expires_at > ?
                  AND (player_id IS NULL OR player_id = ?)
                """)
                .params(playerId, sessionId, tokenHash, now, playerId)
                .update();
    }

    /**
     * Deletes all expired sessions.
     *
     * @param now current time in UTC
     * @return number of sessions deleted
     */
    public int deleteExpiredSessions(OffsetDateTime now) {
        return jdbc.sql("""
                DELETE FROM sessions
                WHERE expires_at <= ?
                """)
                .params(now)
                .update();
    }
}

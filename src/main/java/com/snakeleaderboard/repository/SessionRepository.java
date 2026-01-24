package com.snakeleaderboard.repository;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.UUID;

@Repository
public class SessionRepository {

    private final JdbcClient jdbc;

    public SessionRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public void insertSession(UUID sessionId, String tokenHash, OffsetDateTime createdAt, OffsetDateTime expiresAt) {
        jdbc.sql("""
                INSERT INTO sessions (id, token_hash, created_at, expires_at)
                VALUES (?, ?, ?, ?)
                """)
                .params(sessionId, tokenHash, createdAt, expiresAt)
                .update();
    }

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

    public int deleteExpiredSessions(OffsetDateTime now) {
        return jdbc.sql("""
                DELETE FROM sessions
                WHERE expires_at <= ?
                """)
                .params(now)
                .update();
    }
}

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

    public int countValidSessions(UUID sessionId, String tokenHash, OffsetDateTime now) {
        Integer count = jdbc.sql("""
                SELECT COUNT(*) FROM sessions
                WHERE id = ? AND token_hash = ? AND expires_at > ?
                """)
                .params(sessionId, tokenHash, now)
                .query(Integer.class)
                .single();
        return count == null ? 0 : count;
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

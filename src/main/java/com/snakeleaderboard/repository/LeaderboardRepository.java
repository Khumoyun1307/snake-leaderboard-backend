package com.snakeleaderboard.repository;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class LeaderboardRepository {

    private final JdbcClient jdbc;

    public LeaderboardRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public List<LeaderboardRow> fetchRaceRows(String mode, int limit, int offset) {
        return jdbc.sql("""
                SELECT map_id, player_name, difficulty, score, time_survived_ms, created_at
                FROM (
                    SELECT DISTINCT ON (player_id)
                           map_id, player_name, difficulty, score, time_survived_ms, created_at
                    FROM scores
                    WHERE mode = ?
                    ORDER BY player_id,
                             map_id DESC,
                             score DESC,
                             time_survived_ms DESC NULLS LAST,
                             created_at ASC
                ) best
                ORDER BY map_id DESC,
                         score DESC,
                         time_survived_ms DESC NULLS LAST,
                         created_at ASC
                LIMIT ?
                OFFSET ?
                """)
                .params(mode, limit, offset)
                .query(LeaderboardRow.class)
                .list();
    }

    public List<LeaderboardRow> fetchRaceRows(String mode, String difficulty, int limit, int offset) {
        return jdbc.sql("""
                SELECT map_id, player_name, difficulty, score, time_survived_ms, created_at
                FROM (
                    SELECT DISTINCT ON (player_id)
                           map_id, player_name, difficulty, score, time_survived_ms, created_at
                    FROM scores
                    WHERE mode = ?
                      AND difficulty = ?
                    ORDER BY player_id,
                             map_id DESC,
                             score DESC,
                             time_survived_ms DESC NULLS LAST,
                             created_at ASC
                ) best
                ORDER BY map_id DESC,
                         score DESC,
                         time_survived_ms DESC NULLS LAST,
                         created_at ASC
                LIMIT ?
                OFFSET ?
                """)
                .params(mode, difficulty, limit, offset)
                .query(LeaderboardRow.class)
                .list();
    }

    public List<LeaderboardRow> fetchRows(String mode, int limit, int offset) {
        return jdbc.sql("""
                SELECT map_id, player_name, difficulty, score, time_survived_ms, created_at
                FROM scores
                WHERE mode = ?
                ORDER BY score DESC,
                         time_survived_ms DESC NULLS LAST,
                         created_at ASC
                LIMIT ?
                OFFSET ?
                """)
                .params(mode, limit, offset)
                .query(LeaderboardRow.class)
                .list();
    }

    public List<LeaderboardRow> fetchRows(int mapId, String mode, int limit, int offset) {
        return jdbc.sql("""
                SELECT map_id, player_name, difficulty, score, time_survived_ms, created_at
                FROM scores
                WHERE map_id = ?
                  AND mode = ?
                ORDER BY score DESC,
                         time_survived_ms DESC NULLS LAST,
                         created_at ASC
                LIMIT ?
                OFFSET ?
                """)
                .params(mapId, mode, limit, offset)
                .query(LeaderboardRow.class)
                .list();
    }

    public List<LeaderboardRow> fetchRows(String mode, String difficulty, int limit, int offset) {
        return jdbc.sql("""
                SELECT map_id, player_name, difficulty, score, time_survived_ms, created_at
                FROM scores
                WHERE mode = ?
                  AND difficulty = ?
                ORDER BY score DESC,
                         time_survived_ms DESC NULLS LAST,
                         created_at ASC
                LIMIT ?
                OFFSET ?
                """)
                .params(mode, difficulty, limit, offset)
                .query(LeaderboardRow.class)
                .list();
    }

    public List<LeaderboardRow> fetchRows(int mapId, String mode, String difficulty, int limit, int offset) {
        return jdbc.sql("""
                SELECT map_id, player_name, difficulty, score, time_survived_ms, created_at
                FROM scores
                WHERE map_id = ?
                  AND mode = ?
                  AND difficulty = ?
                ORDER BY score DESC,
                         time_survived_ms DESC NULLS LAST,
                         created_at ASC
                LIMIT ?
                OFFSET ?
                """)
                .params(mapId, mode, difficulty, limit, offset)
                .query(LeaderboardRow.class)
                .list();
    }
}

package com.snakeleaderboard.repository;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JDBC-based data access for leaderboard queries.
 *
 * <p>For non-{@code RACE} modes, rows are ordered by:</p>
 * <ul>
 *   <li>higher {@code score}</li>
 *   <li>higher {@code time_survived_ms} (NULLs last)</li>
 *   <li>earlier {@code created_at}</li>
 * </ul>
 *
 * <p>For {@code RACE} mode, results include at most one row per player and are ranked by the
 * furthest map reached (highest {@code map_id}), then the same score/tie-break rules.</p>
 */
@Repository
public class LeaderboardRepository {

    private final JdbcClient jdbc;

    public LeaderboardRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Fetches a {@code RACE}-mode leaderboard page (one best score per player; any difficulty).
     *
     * @param mode mode name stored in the database
     * @param limit max results to return
     * @param offset number of results to skip (0-based)
     * @return leaderboard rows for the requested page
     */
    public List<LeaderboardRow> fetchRaceRows(String mode, int limit, int offset) {
        // Uses PostgreSQL DISTINCT ON to pick each player's best entry, then ranks by furthest map reached.
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

    /**
     * Fetches a {@code RACE}-mode leaderboard page filtered to a specific difficulty.
     *
     * @param mode mode name stored in the database
     * @param difficulty difficulty name stored in the database
     * @param limit max results to return
     * @param offset number of results to skip (0-based)
     * @return leaderboard rows for the requested page
     */
    public List<LeaderboardRow> fetchRaceRows(String mode, String difficulty, int limit, int offset) {
        // Uses PostgreSQL DISTINCT ON to pick each player's best entry, then ranks by furthest map reached.
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

    /**
     * Fetches leaderboard rows for a mode across all maps and difficulties.
     */
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

    /**
     * Fetches leaderboard rows for a specific map and mode across all difficulties.
     */
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

    /**
     * Fetches leaderboard rows for a mode filtered to a specific difficulty across all maps.
     */
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

    /**
     * Fetches leaderboard rows for a specific map, mode, and difficulty.
     */
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

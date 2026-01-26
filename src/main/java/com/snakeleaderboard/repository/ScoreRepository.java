package com.snakeleaderboard.repository;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * JDBC-based persistence operations for player scores.
 */
@Repository
public class ScoreRepository {

    private final JdbcClient jdbc;

    public ScoreRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Updates the stored display name for all of a player's scores.
     *
     * <p>This keeps leaderboard results in sync with the player's most recent name.</p>
     */
    public void updatePlayerName(UUID playerId, String playerName) {
        jdbc.sql("""
                UPDATE scores
                SET player_name = ?
                WHERE player_id = ?
                """)
                .params(playerName, playerId)
                .update();
    }

    /**
     * Inserts a new score row or conditionally updates an existing one.
     *
     * <p>The database enforces uniqueness on {@code (player_id, map_id, mode, difficulty)}. When a
     * conflict occurs, the existing row is updated <em>only</em> if the new submission is better
     * (higher score, or equal score with a higher {@code time_survived_ms}).</p>
     *
     * @param score score data to insert/update
     * @return a list containing the persisted row id when inserted/updated, or an empty list if the
     * submission did not beat the existing row
     */
    public List<UUID> upsertScore(ScoreWrite score) {
        return jdbc.sql("""
                INSERT INTO scores
                (id, player_id, player_name, score, map_id, mode, difficulty, time_survived_ms, game_version, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (player_id, map_id, mode, difficulty)
                DO UPDATE SET
                  player_name = EXCLUDED.player_name,
                  score = EXCLUDED.score,
                  time_survived_ms = EXCLUDED.time_survived_ms,
                  game_version = EXCLUDED.game_version,
                  created_at = EXCLUDED.created_at
                WHERE
                  (EXCLUDED.score > scores.score)
                  OR (
                    EXCLUDED.score = scores.score
                    AND COALESCE(EXCLUDED.time_survived_ms, 0) > COALESCE(scores.time_survived_ms, 0)
                  )
                RETURNING id;
                """)
                .params(
                        score.id(),
                        score.playerId(),
                        score.playerName(),
                        score.score(),
                        score.mapId(),
                        score.mode(),
                        score.difficulty(),
                        score.timeSurvivedMs(),
                        score.gameVersion(),
                        score.createdAt()
                )
                .query(UUID.class)
                .list();
    }

    /**
     * Finds the id of an existing score row for the given unique key.
     *
     * <p>Used when a submission does not improve the stored score and therefore does not update the
     * row (and returns no id from {@link #upsertScore(ScoreWrite)}).</p>
     */
    public UUID findScoreId(UUID playerId, int mapId, String mode, String difficulty) {
        return jdbc.sql("""
                SELECT id FROM scores
                WHERE player_id = ? AND map_id = ? AND mode = ? AND difficulty = ?
                """)
                .params(playerId, mapId, mode, difficulty)
                .query(UUID.class)
                .single();
    }
}

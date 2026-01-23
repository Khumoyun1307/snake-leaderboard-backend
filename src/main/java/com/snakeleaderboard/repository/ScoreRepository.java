package com.snakeleaderboard.repository;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class ScoreRepository {

    private final JdbcClient jdbc;

    public ScoreRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public void updatePlayerName(UUID playerId, String playerName) {
        jdbc.sql("""
                UPDATE scores
                SET player_name = ?
                WHERE player_id = ?
                """)
                .params(playerName, playerId)
                .update();
    }

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

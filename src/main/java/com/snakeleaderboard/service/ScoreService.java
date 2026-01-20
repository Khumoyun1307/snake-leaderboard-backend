package com.snakeleaderboard.service;

import com.snakeleaderboard.dto.SubmitScoreRequest;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
public class ScoreService {

    private final JdbcClient jdbc;

    public ScoreService(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public UUID saveScore(SubmitScoreRequest req) {
        UUID id = UUID.randomUUID();

        jdbc.sql("""
    INSERT INTO scores
    (id, player_name, score, map_id, mode, difficulty, time_survived_ms, game_version, created_at)
    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
    ON CONFLICT (player_name, map_id, mode, difficulty)
    DO UPDATE SET
      score = EXCLUDED.score,
      time_survived_ms = EXCLUDED.time_survived_ms,
      game_version = EXCLUDED.game_version,
      created_at = EXCLUDED.created_at
    WHERE
      (EXCLUDED.score > scores.score)
      OR (
        EXCLUDED.score = scores.score
        AND COALESCE(EXCLUDED.time_survived_ms, 0) > COALESCE(scores.time_survived_ms, 0)
      );
""")
                .params(
                        id,
                        req.playerName(),
                        req.score(),
                        req.mapId(),
                        req.mode(),
                        req.difficulty(),
                        req.timeSurvivedMs(),
                        req.gameVersion(),
                        OffsetDateTime.now(ZoneOffset.UTC)
                )
                .update();


        return id;
    }
}

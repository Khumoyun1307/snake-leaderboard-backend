package com.snakeleaderboard.service;

import com.snakeleaderboard.dto.LeaderboardEntry;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class LeaderboardService {

    private final JdbcClient jdbc;

    public LeaderboardService(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public List<LeaderboardEntry> getTop(
            int mapId,
            String mode,
            String difficulty,
            int limit,
            int offset
    ) {
        int safeLimit = Math.min(Math.max(limit, 1), 50);
        int safeOffset = Math.max(offset, 0);

        boolean anyDifficulty =
                (difficulty == null || difficulty.isBlank() || "ANY".equalsIgnoreCase(difficulty));

        boolean isRace = "RACE".equalsIgnoreCase(mode);
        boolean isMapSelect = "MAP_SELECT".equalsIgnoreCase(mode);

        // RACE: best per player, ranked by furthest map then score
        if (isRace) {
            List<RaceRow> rows;

            if (anyDifficulty) {
                rows = jdbc.sql("""
                        SELECT map_id, player_name, difficulty, score, time_survived_ms, created_at
                        FROM (
                            SELECT DISTINCT ON (player_name)
                                   map_id, player_name, difficulty, score, time_survived_ms, created_at
                            FROM scores
                            WHERE mode = ?
                            ORDER BY player_name,
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
                        .params(mode, safeLimit, safeOffset)
                        .query(RaceRow.class)
                        .list();
            } else {
                rows = jdbc.sql("""
                        SELECT map_id, player_name, difficulty, score, time_survived_ms, created_at
                        FROM (
                            SELECT DISTINCT ON (player_name)
                                   map_id, player_name, difficulty, score, time_survived_ms, created_at
                            FROM scores
                            WHERE mode = ?
                              AND difficulty = ?
                            ORDER BY player_name,
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
                        .params(mode, difficulty, safeLimit, safeOffset)
                        .query(RaceRow.class)
                        .list();
            }

            List<LeaderboardEntry> result = new ArrayList<>();
            int rank = safeOffset + 1;
            for (RaceRow row : rows) {
                result.add(new LeaderboardEntry(
                        rank++,
                        row.playerName(),
                        row.mapId(),
                        row.difficulty(),
                        row.score(),
                        row.timeSurvivedMs() == null ? 0 : row.timeSurvivedMs(),
                        row.createdAt().toInstant()
                ));
            }
            return result;
        }

        // MAP_SELECT: mapId == 0 means "ANY map"
        boolean anyMapForMapSelect = isMapSelect && mapId == 0;

        if (anyDifficulty) {
            List<Row> rows;

            if (anyMapForMapSelect) {
                rows = jdbc.sql("""
                        SELECT map_id, player_name, difficulty, score, time_survived_ms, created_at
                        FROM scores
                        WHERE mode = ?
                        ORDER BY score DESC,
                                 time_survived_ms DESC NULLS LAST,
                                 created_at ASC
                        LIMIT ?
                        OFFSET ?
                        """)
                        .params(mode, safeLimit, safeOffset)
                        .query(Row.class)
                        .list();
            } else {
                rows = jdbc.sql("""
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
                        .params(mapId, mode, safeLimit, safeOffset)
                        .query(Row.class)
                        .list();
            }

            List<LeaderboardEntry> result = new ArrayList<>();
            int rank = safeOffset + 1;
            for (Row row : rows) {
                result.add(new LeaderboardEntry(
                        rank++,
                        row.playerName(),
                        row.mapId(),
                        row.difficulty(),
                        row.score(),
                        row.timeSurvivedMs() == null ? 0 : row.timeSurvivedMs(),
                        row.createdAt().toInstant()
                ));
            }
            return result;
        } else {
            List<Row> rows;

            if (anyMapForMapSelect) {
                rows = jdbc.sql("""
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
                        .params(mode, difficulty, safeLimit, safeOffset)
                        .query(Row.class)
                        .list();
            } else {
                rows = jdbc.sql("""
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
                        .params(mapId, mode, difficulty, safeLimit, safeOffset)
                        .query(Row.class)
                        .list();
            }

            List<LeaderboardEntry> result = new ArrayList<>();
            int rank = safeOffset + 1;
            for (Row row : rows) {
                result.add(new LeaderboardEntry(
                        rank++,
                        row.playerName(),
                        row.mapId(),
                        row.difficulty(),
                        row.score(),
                        row.timeSurvivedMs() == null ? 0 : row.timeSurvivedMs(),
                        row.createdAt().toInstant()
                ));
            }
            return result;
        }
    }

    private record Row(
            int mapId,
            String playerName,
            String difficulty,
            int score,
            Long timeSurvivedMs,
            java.time.OffsetDateTime createdAt
    ) {}

    private record RaceRow(
            int mapId,
            String playerName,
            String difficulty,
            int score,
            Long timeSurvivedMs,
            java.time.OffsetDateTime createdAt
    ) {}
}

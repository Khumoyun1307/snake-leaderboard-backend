package com.snakeleaderboard.mapper;

import com.snakeleaderboard.dto.LeaderboardEntry;
import com.snakeleaderboard.repository.LeaderboardRow;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Maps database leaderboard rows to API DTOs and computes page-local ranking.
 */
@Component
public class LeaderboardEntryMapper {

    /**
     * Converts raw rows into API entries and assigns ranks starting at {@code offset + 1}.
     *
     * @param rows rows returned from the repository
     * @param offset the request offset used for pagination
     * @return mapped entries with computed ranks
     */
    public List<LeaderboardEntry> toEntries(List<LeaderboardRow> rows, int offset) {
        List<LeaderboardEntry> result = new ArrayList<>(rows.size());
        int rank = offset + 1;
        for (LeaderboardRow row : rows) {
            result.add(toEntry(rank++, row));
        }
        return result;
    }

    private LeaderboardEntry toEntry(int rank, LeaderboardRow row) {
        long timeSurvivedMs = row.timeSurvivedMs() == null ? 0 : row.timeSurvivedMs();
        return new LeaderboardEntry(
                rank,
                row.playerName(),
                row.mapId(),
                row.difficulty(),
                row.score(),
                timeSurvivedMs,
                row.createdAt().toInstant()
        );
    }
}

package com.snakeleaderboard.mapper;

import com.snakeleaderboard.dto.LeaderboardEntry;
import com.snakeleaderboard.repository.LeaderboardRow;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class LeaderboardEntryMapper {

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

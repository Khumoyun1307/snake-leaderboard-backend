package com.snakeleaderboard.service;

import com.snakeleaderboard.dto.LeaderboardEntry;
import com.snakeleaderboard.mapper.LeaderboardEntryMapper;
import com.snakeleaderboard.repository.LeaderboardRepository;
import com.snakeleaderboard.repository.LeaderboardRow;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LeaderboardService {

    private final LeaderboardRepository leaderboardRepository;
    private final LeaderboardEntryMapper leaderboardEntryMapper;

    public LeaderboardService(LeaderboardRepository leaderboardRepository, LeaderboardEntryMapper leaderboardEntryMapper) {
        this.leaderboardRepository = leaderboardRepository;
        this.leaderboardEntryMapper = leaderboardEntryMapper;
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
            List<LeaderboardRow> rows = anyDifficulty
                    ? leaderboardRepository.fetchRaceRows(mode, safeLimit, safeOffset)
                    : leaderboardRepository.fetchRaceRows(mode, difficulty, safeLimit, safeOffset);
            return leaderboardEntryMapper.toEntries(rows, safeOffset);
        }

        // MAP_SELECT: mapId == 0 means "ANY map"
        boolean anyMapForMapSelect = isMapSelect && mapId == 0;

        if (anyDifficulty) {
            List<LeaderboardRow> rows = anyMapForMapSelect
                    ? leaderboardRepository.fetchRows(mode, safeLimit, safeOffset)
                    : leaderboardRepository.fetchRows(mapId, mode, safeLimit, safeOffset);
            return leaderboardEntryMapper.toEntries(rows, safeOffset);
        } else {
            List<LeaderboardRow> rows = anyMapForMapSelect
                    ? leaderboardRepository.fetchRows(mode, difficulty, safeLimit, safeOffset)
                    : leaderboardRepository.fetchRows(mapId, mode, difficulty, safeLimit, safeOffset);
            return leaderboardEntryMapper.toEntries(rows, safeOffset);
        }
    }
}

package com.snakeleaderboard.service;

import com.snakeleaderboard.dto.LeaderboardEntry;
import com.snakeleaderboard.mapper.LeaderboardEntryMapper;
import com.snakeleaderboard.repository.LeaderboardRepository;
import com.snakeleaderboard.repository.LeaderboardRow;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Leaderboard query orchestration.
 *
 * <p>Applies API-specific semantics (mode rules, optional difficulty filter, and pagination) and
 * delegates database access to {@link LeaderboardRepository}.</p>
 */
@Service
public class LeaderboardService {

    private final LeaderboardRepository leaderboardRepository;
    private final LeaderboardEntryMapper leaderboardEntryMapper;

    public LeaderboardService(LeaderboardRepository leaderboardRepository, LeaderboardEntryMapper leaderboardEntryMapper) {
        this.leaderboardRepository = leaderboardRepository;
        this.leaderboardEntryMapper = leaderboardEntryMapper;
    }

    /**
     * Returns a page of leaderboard entries for the requested query.
     *
     * <p>Special rules:</p>
     * <ul>
     *   <li>{@code difficulty} may be {@code null}/blank/{@code ANY} to indicate "all difficulties".</li>
     *   <li>For {@code MAP_SELECT}, {@code mapId == 0} indicates "any map".</li>
     *   <li>For {@code RACE}, results are "best per player" and ranked by furthest map reached.</li>
     * </ul>
     *
     * @param mapId map identifier (may be {@code 0} for {@code MAP_SELECT})
     * @param mode game mode name
     * @param difficulty difficulty name or {@code ANY} (nullable)
     * @param limit requested page size (clamped to {@code [1,50]})
     * @param offset requested offset (clamped to {@code >= 0})
     * @return leaderboard entries with computed ranks for this page
     */
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

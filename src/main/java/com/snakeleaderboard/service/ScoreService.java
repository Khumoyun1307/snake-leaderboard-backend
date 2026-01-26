package com.snakeleaderboard.service;

import com.snakeleaderboard.dto.SubmitScoreRequest;
import com.snakeleaderboard.repository.ScoreRepository;
import com.snakeleaderboard.repository.ScoreWrite;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

/**
 * Score submission orchestration.
 */
@Service
public class ScoreService {

    private final ScoreRepository scoreRepository;

    public ScoreService(ScoreRepository scoreRepository) {
        this.scoreRepository = scoreRepository;
    }

    /**
     * Persists a score submission.
     *
     * <p>If a row for {@code (playerId, mapId, mode, difficulty)} already exists, it is updated only
     * when the submission is better; otherwise, the existing row is preserved and its id is
     * returned.</p>
     *
     * @param req validated score submission request
     * @return id of the persisted score row (new or existing)
     */
    @Transactional
    public UUID saveScore(SubmitScoreRequest req) {
        UUID id = UUID.randomUUID();

        scoreRepository.updatePlayerName(req.playerId(), req.playerName());

        OffsetDateTime createdAt = OffsetDateTime.now(ZoneOffset.UTC);
        var ids = scoreRepository.upsertScore(new ScoreWrite(
                id,
                req.playerId(),
                req.playerName(),
                req.score(),
                req.mapId(),
                req.mode().name(),
                req.difficulty().name(),
                req.timeSurvivedMs(),
                req.gameVersion(),
                createdAt
        ));

        if (!ids.isEmpty()) {
            return ids.get(0);
        }

        return scoreRepository.findScoreId(req.playerId(), req.mapId(), req.mode().name(), req.difficulty().name());
    }
}

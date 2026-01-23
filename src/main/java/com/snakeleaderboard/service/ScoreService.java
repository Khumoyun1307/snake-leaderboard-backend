package com.snakeleaderboard.service;

import com.snakeleaderboard.dto.SubmitScoreRequest;
import com.snakeleaderboard.repository.ScoreRepository;
import com.snakeleaderboard.repository.ScoreWrite;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
public class ScoreService {

    private final ScoreRepository scoreRepository;

    public ScoreService(ScoreRepository scoreRepository) {
        this.scoreRepository = scoreRepository;
    }

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
                req.mode(),
                req.difficulty(),
                req.timeSurvivedMs(),
                req.gameVersion(),
                createdAt
        ));

        if (!ids.isEmpty()) {
            return ids.get(0);
        }

        return scoreRepository.findScoreId(req.playerId(), req.mapId(), req.mode(), req.difficulty());
    }
}

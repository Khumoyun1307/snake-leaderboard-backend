ALTER TABLE scores
    ADD COLUMN IF NOT EXISTS player_id UUID;

UPDATE scores
SET player_id = md5(player_name)::uuid
WHERE player_id IS NULL;

ALTER TABLE scores
    ALTER COLUMN player_id SET NOT NULL;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'uq_scores_best_per_player'
    ) THEN
        ALTER TABLE scores
            DROP CONSTRAINT uq_scores_best_per_player;
    END IF;
END$$;

ALTER TABLE scores
    ADD CONSTRAINT uq_scores_best_per_player
    UNIQUE (player_id, map_id, mode, difficulty);

-- 1) Remove duplicates keeping the "best" row per (player_name, map_id, mode, difficulty)
-- Best = higher score, then higher time_survived_ms, then earlier created_at
WITH ranked AS (
    SELECT
        id,
        ROW_NUMBER() OVER (
            PARTITION BY player_name, map_id, mode, difficulty
            ORDER BY score DESC,
                     time_survived_ms DESC NULLS LAST,
                     created_at ASC
        ) AS rn
    FROM scores
)
DELETE FROM scores s
USING ranked r
WHERE s.id = r.id
  AND r.rn > 1;

-- 2) Enforce uniqueness so upsert can target it
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'uq_scores_best_per_player'
    ) THEN
        ALTER TABLE scores
            ADD CONSTRAINT uq_scores_best_per_player
            UNIQUE (player_name, map_id, mode, difficulty);
    END IF;
END$$;

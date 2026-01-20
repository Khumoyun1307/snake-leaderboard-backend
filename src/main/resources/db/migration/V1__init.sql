CREATE TABLE IF NOT EXISTS scores (
  id UUID PRIMARY KEY,
  player_name VARCHAR(24) NOT NULL,
  score INT NOT NULL,
  map_id INT NOT NULL,
  mode VARCHAR(32) NOT NULL,
  difficulty VARCHAR(32) NOT NULL,
  time_survived_ms BIGINT,
  game_version VARCHAR(32),
  created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_scores_leaderboard
  ON scores (map_id, mode, difficulty, score DESC, time_survived_ms DESC, created_at ASC);

CREATE TABLE IF NOT EXISTS sessions (
  id UUID PRIMARY KEY,
  token_hash VARCHAR(64) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL,
  expires_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_sessions_expires_at
  ON sessions (expires_at);

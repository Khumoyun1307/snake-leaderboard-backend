CREATE TABLE IF NOT EXISTS rate_limits (
  ip VARCHAR(64) PRIMARY KEY,
  window_start TIMESTAMPTZ NOT NULL,
  count INT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_rate_limits_window_start
  ON rate_limits (window_start);

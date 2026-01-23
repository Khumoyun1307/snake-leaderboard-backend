# Snake Leaderboard API

Backend service and static site for a modern Snake game. The Java Swing client and the web UI share the same REST API for leaderboards and score submission.

Live deployment: https://fit-maisie-khumoyun-7d8e10bd.koyeb.app
Health check: GET /health -> {"status":"up"}

## Highlights
- Session-based score submission with SHA-256 token hashing (raw tokens never stored)
- Best-score upsert per player/map/mode/difficulty
- Ranked leaderboard queries with pagination and mode-aware sorting
- Database-backed rate limiting for score submissions (30 requests per minute per IP)
- Flyway migrations and scheduled cleanup of expired sessions
- Single-origin hosting for API and static site

## Architecture
Clients (Java Swing desktop + web UI) -> Spring Boot API -> PostgreSQL

Key modules
- `src/main/java/com/snakeleaderboard/api`: REST controllers
- `src/main/java/com/snakeleaderboard/service`: business logic and SQL via `JdbcClient`
- `src/main/java/com/snakeleaderboard/config`: scheduling, routing, and rate limiting
- `src/main/resources/db/migration`: schema migrations
- `src/main/resources/static`: marketing site and leaderboard UI

## Tech stack
- Java 21
- Spring Boot 4.0.1
- PostgreSQL
- Flyway
- Spring JdbcClient
- GitHub Actions (CI) and Koyeb (deploy)

## API
Base path: `/api`

### Start session
POST `/api/session`

Response
```json
{
  "sessionId": "2b7f2f1a-6f3b-4a2f-a2dd-8c6d49d3d3e1",
  "sessionToken": "f3a9c8d1b2e4a6f8...",
  "expiresAt": "2026-01-20T12:34:56Z"
}
```

### Submit score
POST `/api/scores`

Headers
```
X-Session-Id: <sessionId>
X-Session-Token: <sessionToken>
```

Body
```json
{
  "playerName": "player1",
  "score": 42,
  "mapId": 2,
  "mode": "MAP_SELECT",
  "difficulty": "NORMAL",
  "timeSurvivedMs": 26000,
  "gameVersion": "1.0.0"
}
```

Response (201)
```json
{
  "scoreId": "c3f2f7b2-7c4e-4f0b-8f0c-1db4c2a9c6ab"
}
```

### Get leaderboard
GET `/api/leaderboard?mapId=2&mode=MAP_SELECT&difficulty=NORMAL&limit=10&offset=0`

Query parameters
| Name | Required | Description |
| --- | --- | --- |
| `mapId` | Yes | Map identifier (MAP_SELECT supports 0 for any map) |
| `mode` | Yes | Game mode (STANDARD, MAP_SELECT, RACE) |
| `difficulty` | No | Difficulty (omit or use ANY in clients) |
| `limit` | No | Max results (default 10, clamped to 1-50) |
| `offset` | No | Pagination offset (default 0) |

Notes
- RACE ignores `mapId` and ranks by furthest map, then score, then time.
- MAP_SELECT supports `mapId=0` to aggregate any map.

### Service info
GET `/api`

Response
```json
{
  "service": "snake-leaderboard-api",
  "status": "ok"
}
```

### Health
GET `/health`

Response
```json
{
  "status": "up"
}
```

## Data model
### `scores`
- One row per player/map/mode/difficulty (best score only)
- Ranking order: highest score, longest survival time, earliest submission
- Indexed for fast leaderboard queries

### `sessions`
- UUID session id and SHA-256 token hash
- Short-lived sessions (30 minute TTL)
- Indexed by `expires_at` for cleanup

### `rate_limits`
- One row per IP with a rolling 60-second window and count
- Used by the rate limit filter for multi-instance safe throttling

## Validation and security
- Player name allows letters, numbers, spaces, underscore, and dash
- Scores, map ids, and survival time are range-validated
- Session tokens are random and only their hash is stored
- Score submissions are rate limited per IP

## Rate limit config
The filter can trust `X-Forwarded-For` when your proxy is trusted.

Example
```
rate_limit.trust_forwarded_headers=true
rate_limit.trusted_proxies=203.0.113.4,10.0.0.0/8
```

Use `rate_limit.trusted_proxies=*` to trust all proxies (not recommended).

## Web UI
Static pages are served by Spring Boot from `src/main/resources/static`:
- `/` landing page with a demo
- `/leaderboard/` live leaderboard UI
- `/downloads/`, `/developers/`, `/about/`
- `/rules/` gameplay rules
- `/contact/` contact page

Update placeholder links in the HTML pages to point to your GitHub, portfolio, and email.

## Local development
Requirements
- Java 21
- PostgreSQL

Environment variables
```
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/snakeleaderboard
SPRING_DATASOURCE_USERNAME=snakeuser
SPRING_DATASOURCE_PASSWORD=snakepass
```

Run locally
```bash
./mvnw spring-boot:run
```

Build and run
```bash
./mvnw clean package
java -jar target/snake-leaderboard-api-0.0.1-SNAPSHOT.jar
```

## Database migrations
Flyway runs migrations automatically on startup from `src/main/resources/db/migration`.

## Testing
Unit tests
```bash
./mvnw test
```

Integration tests and E2E tests (Testcontainers, requires Docker)
```bash
./mvnw verify
```

## CI and deployment
- CI: `.github/workflows/ci.yml` runs tests on every push and PR
- Deploy: `.github/workflows/deploy.yml` deploys to Koyeb after a successful CI run

Required secrets for deploy:
- `KOYEB_API_TOKEN`
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

## License
GPL-3.0. See `LICENSE`.

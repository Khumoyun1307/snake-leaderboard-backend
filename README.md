# Snake Leaderboard API (Spring Boot + PostgreSQL + Flyway)

Backend service for an online leaderboard used by a Java Swing Snake game.

## Live Deployment

Base URL:
- https://fit-maisie-khumoyun-7d8e10bd.koyeb.app

Health:
- GET `/health` → `{"status":"up"}`

## Tech Stack

- Java + Spring Boot (REST API)
- PostgreSQL (Neon)
- Flyway migrations
- Deployed on Koyeb (HTTPS)

## API Overview

All API routes are under `/api`.

### Start a session
Creates a temporary session required for submitting scores.

- POST `/api/session`
- Response: `StartSessionResponse` (contains `sessionId` and `sessionToken`)

### Get leaderboard
- GET `/api/leaderboard?mapId=...&mode=...&difficulty=...&limit=10&offset=0`

Query params:
- `mapId` (int) required
- `mode` (string) required
- `difficulty` (string) optional
- `limit` (int) optional (default 10)
- `offset` (int) optional (default 0)

Response:
- `LeaderboardResponse` (contains `entries`)

### Submit score
- POST `/api/scores`
- Headers:
    - `X-Session-Id: <uuid>`
    - `X-Session-Token: <string>`
- Body:
    - `SubmitScoreRequest`

Returns:
- `201 Created` with `{"scoreId":"<uuid>"}`

## Local Development

### Prerequisites
- Java 17+ (or your project’s Java version)
- Maven
- PostgreSQL (local) or Docker

### Configuration

This project uses environment variables for DB config:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

For local development, use `application-local.properties` (not committed) and run with:
- `SPRING_PROFILES_ACTIVE=local`

### Run
```bash
./mvnw spring-boot:run

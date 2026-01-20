# 🐍 Snake Leaderboard API

Cloud-hosted backend service for an online leaderboard used by a Java Swing Snake game.

This project provides secure session-based score submission, leaderboard queries, and database-backed persistence with automatic migrations and cleanup.

---

## 🌍 Live Deployment

**Base URL:**  
https://fit-maisie-khumoyun-7d8e10bd.koyeb.app

**Health Check:**  
`GET /health`  
Response:
```json
{ "status": "up" }
```

---

## 🧰 Tech Stack

- **Java 21**
- **Spring Boot**
- **PostgreSQL** (Neon)
- **Flyway** (database migrations)
- **Koyeb** (cloud hosting, HTTPS)
- **JDBC Client** (data access)

---

## 📦 Features

- Secure, short-lived sessions for score submission
- Token hashing (raw session tokens are never stored)
- Automatic session expiration (30-minute TTL)
- Scheduled cleanup of expired sessions
- Ranked leaderboard queries with indexing
- Duplicate prevention (best score per player/map/mode/difficulty)
- IP-based rate limiting for score submissions
- Health endpoint for uptime monitoring

---

## 🔐 Session Model

Clients must request a session before submitting a score.

Sessions:
- Are identified by `sessionId` (UUID)
- Use a random token (`sessionToken`)
- Store only a **SHA-256 hash** of the token in the database
- Expire automatically after **30 minutes**
- Are periodically deleted by a scheduled cleanup job

---

## 📡 API Endpoints

All endpoints are under `/api`

---

### ▶ Start Session

Creates a temporary session required for submitting scores.

**POST** `/api/session`

**Response**
```json
{
  "sessionId": "2b7f2f1a-6f3b-4a2f-a2dd-8c6d49d3d3e1",
  "sessionToken": "f3a9c8d1b2e4a6f8...",
  "expiresAt": "2026-01-20T12:34:56Z"
}
```

---

### 📊 Get Leaderboard

**GET**  
`/api/leaderboard?mapId=2&mode=MAP_SELECT&difficulty=NORMAL&limit=10&offset=0`

**Query Parameters**
| Name | Required | Description |
|------|----------|-------------|
| `mapId` | Yes | Map identifier |
| `mode` | Yes | Game mode |
| `difficulty` | No | Difficulty level |
| `limit` | No | Max results (default: 10) |
| `offset` | No | Pagination offset (default: 0) |

**Response**
```json
{
  "mapId": 2,
  "mode": "MAP_SELECT",
  "difficulty": "NORMAL",
  "entries": []
}
```

---

### 🏆 Submit Score

**POST** `/api/scores`

**Headers**
```
X-Session-Id: <sessionId>
X-Session-Token: <sessionToken>
```

**Body**
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

**Response (201 Created)**
```json
{
  "scoreId": "c3f2f7b2-7c4e-4f0b-8f0c-1db4c2a9c6ab"
}
```

---

## ⚙ Rate Limiting

Score submissions are protected with IP-based rate limiting:

- Applies to: `POST /api/scores`
- Limit: **30 requests per minute per IP**
- Returns:
```json
{ "error": "Too Many Requests" }
```
with HTTP status `429`

---

## 🗄 Database Schema

### `scores`
- Stores all leaderboard entries
- Enforces unique "best score" per:
  ```
  (player_name, map_id, mode, difficulty)
  ```
- Ranked by:
  - Highest score
  - Longest survival time
  - Earliest submission time

### `sessions`
- Stores active sessions
- Fields:
  - `id` (UUID)
  - `token_hash` (SHA-256)
  - `created_at`
  - `expires_at`
- Indexed on `expires_at` for fast cleanup

---

## 🧪 Local Development

### Requirements
- Java 21
- PostgreSQL
- Maven (or Maven Wrapper)

### Environment Variables
Set these before running:
```
SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD
```

Example:
```
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/snakeleaderboard
SPRING_DATASOURCE_USERNAME=snakeuser
SPRING_DATASOURCE_PASSWORD=snakepass
```

---

## ▶ Run Locally

Using Maven Wrapper:
```bash
./mvnw spring-boot:run
```

Or build and run:
```bash
./mvnw clean package
java -jar target/snake-leaderboard-api-0.0.1-SNAPSHOT.jar
```

---

## 🚀 Deployment

- Hosted on **Koyeb**
- Uses **Neon PostgreSQL**
- Flyway automatically applies schema migrations on startup
- Deploys automatically from GitHub on push (CI/CD ready)

---

## 📄 License

MIT (or choose your preferred license)

---

## 👤 Author

Built by a Java Developer learning full-stack backend engineering through real-world cloud deployment and production-grade system design.

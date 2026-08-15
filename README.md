# DevLaunch – AI-Powered Developer Career Hub

A full-stack web application for developers to manage, showcase, and accelerate their software careers using AI-driven insights and automation. DevLaunch consolidates resume building, job application tracking, study planning, GitHub/LeetCode analytics, AI mock interviews, AI resume review, gamification, and placement readiness into one platform.

- **Frontend:** React 18 · TypeScript · Vite · Tailwind CSS
- **Backend:** Java 21 · Spring Boot 3.5 · Spring Security + JWT
- **Data & infra:** MySQL 8 · Redis · RabbitMQ · Docker · Microsoft Azure (Container Apps)

---

## Features

**Implemented today:**

- **Authentication & profiles** — register, login, JWT-based stateless auth (BCrypt password hashing), profile management, change password, forgot/reset password (async email via RabbitMQ + SMTP, single-use 30-minute tokens)
- **Resume Builder** — multiple resumes per user, six sections (education, experience, projects, skills, certifications, achievements), four seeded templates, server-side PDF export (OpenPDF)
- **Job Application Tracker** — status pipeline (Wishlist → Applied → Assessment → Interview → Offer → Rejected), priorities, work modes, timeline events, interview scheduling, private notes, file attachments, analytics, Kanban-style board
- **Study Planner** — scheduled study tasks, priorities/statuses, calendar view, completion streaks, reminders
- **GitHub Analytics** — live GitHub profile/repository/language statistics (username-based, no OAuth, Redis-cached)
- **LeetCode Tracker** — live solved counts and difficulty breakdown via the LeetCode GraphQL API (Redis-cached)
- **AI Mock Interview** — HR / Java / Spring Boot / SQL / React categories, text **or voice** answers (Whisper transcription), scored feedback, full history and analytics; deterministic fallbacks when no AI key is configured
- **AI Resume Review (ATS)** — resume score + ATS score with keyword/section/summary analysis; deterministic fallback when no AI key is configured
- **Gamification** — XP ledger, levels, 17 unlockable badges, unlock notifications
- **Placement Readiness** — weighted 0–100 score with snapshots and milestone notifications
- **Notifications** — in-app notification center (9 types), async creation via RabbitMQ, unread badge polling
- **Admin module** — user/resume/job/study/AI-report/announcement/feedback management with pagination
- **Announcements & feedback** — admin announcements (fan-out notifications), user feedback submission
- **API docs** — Swagger UI (springdoc) with JWT bearer scheme

**Planned / future (not implemented):** OAuth login (Google/GitHub), refresh-token flow, global leaderboard, question-bank admin management, rate limiting, Flyway/Liquibase migrations, frontend component/E2E tests, ESLint config, LinkedIn integration, portfolio generator, mobile app.

---

## Technology Stack

| Layer | Technologies |
|---|---|
| Frontend | React 18, TypeScript 5.5, Vite 5, Tailwind CSS 3.4, React Router 6, Axios, recharts, react-hook-form + zod, react-hot-toast, date-fns, lucide-react, Vitest |
| Backend | Java 21, Spring Boot 3.5, Spring Security, JJWT 0.12.6, Spring Data JPA/Hibernate, Spring Mail, Spring AMQP/RabbitMQ, Spring Data Redis, springdoc 2.8.6, MapStruct 1.6.3, Lombok, OpenPDF 2.0.4 |
| Database | MySQL 8+ (`ddl-auto: update` — schema auto-created, no migration tool) |
| Messaging | RabbitMQ 3 (topic exchange `devlaunch.events`, 11 queues + DLQ, retry with backoff) |
| Caching | Redis 7 (Spring Cache abstraction, graceful degradation) |
| External APIs | GitHub REST API, LeetCode GraphQL API, OpenAI-compatible Chat Completions + Whisper, SMTP (Gmail) |
| DevOps | Docker / Docker Compose, GitHub Actions (CI + CD), Azure Container Registry + Azure Container Apps |

---

## System Architecture

Modular-monolith client–server architecture:

```
React SPA (Vite dev :3000, or nginx :80)
        │  REST / JSON (JWT Bearer)
        ▼
Spring Boot API (:8080)
   ├── Controller → Service → Repository → MySQL 8
   ├── Redis cache (@Cacheable / @CacheEvict)   — graceful fallback when Redis is down
   ├── RabbitMQ publisher → consumers           — async emails, notifications, gamification
   ├── Schedulers                               — interview reminders (07:00), overdue tasks (09:00), token purge (03:00)
   └── External clients                          — GitHub REST, LeetCode GraphQL, OpenAI-compatible AI, SMTP
```

- **Layering:** `Controller → Service interface → Service impl → Repository`, DTO boundary via MapStruct mappers, centralized `GlobalExceptionHandler`.
- **Events:** services publish lightweight events on RabbitMQ; consumers handle side effects (email, notifications, XP/badges) with retries and dead-lettering. The business flow never breaks when the broker is down.
- **Caching:** dashboard (5 m), GitHub/LeetCode (30 m), study/job/achievements (5 m), notification unread count (2 m). Cache failures are logged and reads fall back to the database.
- **Auth:** stateless JWT (HMAC-SHA256, 24 h expiry) checked by `JwtAuthenticationFilter`; BCrypt password hashing; `STUDENT` / `ADMIN` roles; `/api/admin/**` gated to ADMIN; CORS origin driven by `FRONTEND_BASE_URL`.

---

## Project Structure

```
DevLaunch-AI/
├── backend/                        # Spring Boot application (Maven)
│   ├── pom.xml
│   ├── mvnw / mvnw.cmd             # Maven wrapper
│   └── src/
│       ├── main/java/com/devlaunch/
│       │   ├── cache/              # Redis cache manager, TTLs, graceful error handler
│       │   ├── config/             # Security, OpenAPI, data initializer
│       │   ├── controller/         # REST controllers (one per module)
│       │   ├── dto/                # request/response DTOs
│       │   ├── entity/             # JPA entities + enums
│       │   ├── exception/          # GlobalExceptionHandler + custom exceptions
│       │   ├── mapper/             # MapStruct mappers
│       │   ├── messaging/          # RabbitMQ publisher, config, consumers, events
│       │   ├── repository/         # Spring Data JPA repositories
│       │   ├── security/           # JWT service, filter, user details
│       │   ├── service/            # interfaces, impl/, ai/ providers
│       │   └── util/
│       ├── main/resources/         # application.yml, data.sql (seed data)
│       └── test/                   # 35 test classes (208 tests)
├── frontend/                       # React + Vite application
│   ├── package.json / package-lock.json
│   ├── vite.config.ts              # dev port 3000, /api proxy → :8080
│   ├── nginx.conf                  # production static serving + /api proxy
│   ├── Dockerfile                  # multi-stage build (VITE_API_URL build arg)
│   └── src/
│       ├── api/                    # Axios client + endpoint constants
│       ├── components/             # feature components + ui design system
│       ├── constants/              # routes, storage keys, interview config
│       ├── context/                # Auth, Theme, Notification providers
│       ├── hooks/                  # useAuth, useWebcam, useMediaRecorder, …
│       ├── layouts/                # AuthLayout, DashboardLayout
│       ├── pages/                  # auth, dashboard, module and admin pages
│       ├── routes/                 # central route table with guards
│       ├── services/               # 15 typed API service modules
│       ├── types/                  # TypeScript interfaces
│       └── utils/                  # validation, date, format, jwt, …
├── docker/
│   └── docker-compose.yml          # backend, frontend, MySQL, Redis, RabbitMQ
├── docs/                           # design + technical documentation (01…11)
├── .github/workflows/
│   ├── ci.yml                      # validation on PR/push to develop & main
│   └── cd.yml                      # Azure deploy on push to main (OIDC)
└── postman/                        # empty (no collections shipped)
```

---

## Prerequisites

- **Java 21** (backend compiles and runs on Java 21)
- **Node.js 20 + npm** (frontend; lockfile generated with npm)
- **Maven 3.9+** (or use the included `./mvnw` wrapper)
- **Docker + Docker Compose** (for MySQL/Redis/RabbitMQ and container builds)
- **MySQL 8** (local or via Docker Compose)

---

## Local Setup

### 1. Infrastructure (MySQL, Redis, RabbitMQ)

```bash
docker network create devlaunch-network   # required once — the compose file uses an external network
cd docker
docker compose up -d                      # mysql (:3307), redis (:6379), rabbitmq (:5672, UI :15672)
```

> The MySQL container maps host port **3307** → container 3306. The backend defaults to `jdbc:mysql://localhost:3306/devlaunch`, so when using the compose database point the backend at port 3307 (see Environment Variables).

### 2. Backend

```bash
cd backend
./mvnw spring-boot:run                    # serves on http://localhost:8080
```

- Swagger UI: http://localhost:8080/swagger-ui.html
- On first startup `DataInitializer` seeds the `STUDENT` / `ADMIN` roles, four resume templates, and a default admin (`admin@devlaunch.com`, password defined in `DataInitializer` — change it after first login).
- `data.sql` seeds the interview question bank (500 questions across 5 categories) and 17 achievement definitions on every startup (idempotent `INSERT IGNORE`).

### 3. Frontend

```bash
cd frontend
npm ci
npm run dev                               # serves on http://localhost:3000, proxies /api → :8080
```

> `npm run lint` is defined but fails until an ESLint config is added (none is shipped).

---

## Environment Variables

All backend configuration is overridable via environment variables (`${VAR:default}` in `application.yml`). Sensitive values must be supplied via environment; never commit real credentials.

| Variable | Default | Purpose |
|---|---|---|
| `SERVER_PORT` | `8080` | Backend port |
| `SPRING_DATASOURCE_URL` | `jdbc:mysql://localhost:3306/devlaunch…` | MySQL connection URL (use port `3307` for the compose MySQL) |
| `SPRING_DATASOURCE_USERNAME` / `SPRING_DATASOURCE_PASSWORD` | `root` / `root` | MySQL credentials (dev default — set real values in production) |
| `REDIS_HOST` / `REDIS_PORT` | `localhost` / `6379` | Redis connection |
| `REDIS_PASSWORD` / `REDIS_SSL` | empty / `false` | Redis auth + TLS |
| `RABBITMQ_HOST` / `RABBITMQ_PORT` | `localhost` / `5672` | RabbitMQ connection |
| `RABBITMQ_USERNAME` / `RABBITMQ_PASSWORD` | `guest` / `guest` | RabbitMQ credentials |
| `MAIL_HOST` / `MAIL_PORT` | `smtp.gmail.com` / `587` | SMTP for reset emails |
| `MAIL_USERNAME` / `MAIL_PASSWORD` / `MAIL_FROM` | empty | SMTP credentials (empty ⇒ failures are logged, never thrown) |
| `FRONTEND_BASE_URL` | `http://localhost:3000` | Frontend origin — used for password-reset links **and** the CORS allowed origin |
| `RESET_TOKEN_EXPIRY_MINUTES` | `30` | Reset-token validity |
| `AI_PROVIDER_API_KEY` | empty | Enables real OpenAI providers; when empty the app uses deterministic fallbacks |
| `AI_MODEL` / `AI_BASE_URL` | `gpt-4o-mini` / `https://api.openai.com/v1` | Chat Completions model/endpoint (any OpenAI-compatible endpoint works) |
| `AI_WHISPER_MODEL` | `whisper-1` | Voice transcription model |
| `UPLOAD_DIR` | `./uploads` | Job application attachment storage |
| `JWT_SECRET` | dev placeholder | JWT signing key (**must** be overridden in production) |
| `JWT_EXPIRATION` | `86400000` (24 h) | Access-token lifetime (ms) |
| `JPA_DDL_AUTO` | `update` | Hibernate schema handling (keep `update` — no migrations yet) |
| `VITE_API_URL` (frontend build) | empty | Backend API base URL baked into the Vite bundle; empty ⇒ uses the `/api` proxy |

---

## Running the Application

| Component | Command | URL |
|---|---|---|
| Backend | `cd backend && ./mvnw spring-boot:run` | http://localhost:8080 |
| Frontend (dev) | `cd frontend && npm run dev` | http://localhost:3000 |
| Swagger UI | — | http://localhost:8080/swagger-ui.html |
| RabbitMQ management | `docker compose up -d` (docker/) | http://localhost:15672 (guest/guest) |

---

## Docker

`docker/docker-compose.yml` defines the full application stack (external network `devlaunch-network`):

| Service | Image / build | Ports | Notes |
|---|---|---|---|
| `mysql` | `mysql:8.0` | `3307:3306` | database `devlaunch` |
| `redis` | `redis:7-alpine` | `6379:6379` | cache |
| `rabbitmq` | `rabbitmq:3-management` | `5672:5672`, `15672:15672` | broker + management UI |
| `backend` | builds `backend/Dockerfile` | `8081:8080` | env vars wired to service names (`devlaunch-mysql`, `redis`, `rabbitmq`) |
| `frontend` | builds `frontend/Dockerfile` | `5173:80` | nginx; proxies `/api` → `devlaunch-backend:8080` |

- **Backend Dockerfile:** multi-stage — Maven 3.9.9 / Temurin 21 build → `eclipse-temurin:21-jre` runtime, `EXPOSE 8080`.
- **Frontend Dockerfile:** multi-stage — `node:20-alpine` build (`npm ci` → `npm run build`) → `nginx:alpine` serving `dist/` with `nginx.conf`, `EXPOSE 80`. Accepts a `VITE_API_URL` build argument that Vite bakes into the bundle (`import.meta.env.VITE_API_URL` in `src/api/client.ts`); empty by default (local/dev builds use the `/api` proxy).
- The compose nginx proxy (`devlaunch-backend:8080`) works on the compose network; in Azure Container Apps the frontend reaches the API through the build-time `VITE_API_URL` instead.

---

## API Overview

The backend exposes REST endpoints under `/api/**` (JSON, JWT Bearer for protected routes). Endpoint families:

- **Auth:** `POST /api/auth/register`, `/login`, `/forgot-password`, `/reset-password`
- **Users:** `GET/PUT /api/users/me`, `PUT /api/users/change-password`, `PUT/DELETE /api/users/me/github`, `PUT/DELETE /api/users/me/leetcode`
- **Dashboard:** `GET /api/dashboard`
- **Resumes:** `/api/resumes/**`, `/api/resumes/{id}/pdf`, `/api/resume-templates`, section CRUD under `/api/resumes/{resumeId}/{educations|experiences|projects|skills|certifications|achievements}`
- **Job applications:** `/api/job-applications/**` (CRUD, status, analytics, timeline, interviews, notes, attachments)
- **Study planner:** `/api/study-planners/**`
- **GitHub:** `GET /api/github/{username}[/repositories|/languages]`
- **LeetCode:** `GET /api/leetcode/{username}`
- **AI:** `POST /api/ai/resume-review`, `/api/ai/mock-interview/questions`, `/api/ai/mock-interview/feedback`, `/api/ai/transcribe`, `GET /api/ai/mock-interview/history|categories`
- **Achievements:** `/api/achievements`, `/user`, `/summary`, `/history`, `/progress`
- **Notifications:** `/api/notifications`, `/unread-count`, `/{id}/read`, `/read-all`
- **Announcements / feedback:** `GET /api/announcements/active`, `POST /api/feedback`
- **Admin (ROLE_ADMIN):** `/api/admin/**` — dashboard, users, resumes, job-applications, study-plans, ai reports, announcements, feedback
- **Test:** `GET /api/test` (demo authenticated endpoint)

The interactive reference is Swagger UI; the endpoint-by-endpoint contract is documented in `docs/05_API_CONTRACT.md` and `DEVLAUNCH_COMPLETE_TECHNICAL_DOCUMENTATION.md` (§24).

---

## Authentication and Security

- **Stateless JWT** — login issues an HMAC-SHA256 token (24 h expiry) stored in `localStorage` and sent as `Authorization: Bearer …` by the Axios interceptor; `JwtAuthenticationFilter` validates it on every request and populates the `SecurityContext`.
- **BCrypt** password hashing for register, login, reset, and change-password.
- **Roles** — `STUDENT` (default) and `ADMIN`; `/api/admin/**` requires `ROLE_ADMIN`; frontend routes are gated with `ProtectedRoute` / `AdminRoute` / `PublicOnlyRoute`.
- **CORS** — enabled with the allowed origin taken from `FRONTEND_BASE_URL` (keep it in sync with the deployed frontend origin); preflight `OPTIONS` requests are permitted.
- **Password reset** — 256-bit single-use tokens, 30-minute expiry, identical responses for unknown emails, async + swallowed SMTP failures (enumeration-safe).
- **Secrets** — all credentials flow through environment variables; dev placeholders exist in `application.yml` only and must be overridden in production.

---

## CI/CD

GitHub Actions workflows live in `.github/workflows/`:

### CI — `ci.yml` (validation only)

- Triggers: pull requests targeting `develop`/`main`, and pushes to `develop`/`main`.
- Jobs on Ubuntu runners:
  1. **Backend tests** — Java 21 (Temurin, Maven cache) → `./mvnw test` (208 tests).
  2. **Frontend build** — Node 20 → `npm ci` → `npm run build`.
  3. **Docker build** — builds both Dockerfiles locally (`:ci` tags) to validate them.
- CI never pushes images and never touches Azure.

### CD — `cd.yml` (deploys to existing Azure resources)

- Triggers: push to `main`, plus manual `workflow_dispatch`; gated by the GitHub **`production`** environment; single-deployment concurrency guard.
- Azure authentication via **OIDC** (no long-lived Azure client secret stored in GitHub) using secrets `AZURE_CLIENT_ID`, `AZURE_TENANT_ID`, `AZURE_SUBSCRIPTION_ID`.
- Builds and pushes immutable images `devlaunchacr.azurecr.io/devlaunch-backend:${{ github.sha }}` and `devlaunchacr.azurecr.io/devlaunch-frontend:${{ github.sha }}` (no `latest` tag; each revision maps to an exact commit and can be rolled back).
- Deploys by updating the **existing** Container Apps `devlaunch-backend` and `devlaunch-frontend` in resource group `devlaunch-rg` with `az containerapp update --image …` — **image only**; Azure Container Apps creates a normal new revision and the apps' existing environment variables/secrets are preserved.
- The frontend build requires the repository **variable `VITE_API_URL`** (the public URL of the existing `devlaunch-backend` Container App); the build fails loudly if it is unset.

```
GitHub → GitHub Actions → CI (tests + build + docker validation)
       → GitHub Actions → CD (OIDC login → ACR push → containerapp update)
       → Azure Container Apps (devlaunch-backend, devlaunch-frontend)
```

---

## Azure Deployment

The application is deployed to **existing** Azure resources (created and configured outside this repository — the deployment does not create or modify them):

| Resource | Name |
|---|---|
| Resource group | `devlaunch-rg` |
| Container Registry | `devlaunchacr` |
| Container Apps | `devlaunch-backend`, `devlaunch-frontend` |
| Container Apps environment | `devlaunch-env` |

Required GitHub configuration:

- **Secrets** (on the `production` environment): `AZURE_CLIENT_ID`, `AZURE_TENANT_ID`, `AZURE_SUBSCRIPTION_ID` — the workflow uses Azure OIDC federated credentials (issuer `https://token.actions.githubusercontent.com`).
- **Variables** (repository): `VITE_API_URL` — the public URL of the existing `devlaunch-backend` Container App.
- The Container Apps must already be configured with their runtime environment variables/secrets (`SPRING_DATASOURCE_*`, `REDIS_*`, `RABBITMQ_*`, `JWT_SECRET`, `FRONTEND_BASE_URL`, `AI_PROVIDER_API_KEY`, `MAIL_*`, `UPLOAD_DIR`, …). The CD workflow only swaps the image.

See `docs/10_DEPLOYMENT.md` for the full CI/CD and Azure setup guide.

---

## Testing

**Backend** (`cd backend && ./mvnw test`) — 35 test classes, 208 tests, 0 failures:

- Cache behavior (TTLs, serializers, graceful degradation), messaging (RabbitMQ publisher, integration via `rabbitmq-mock`, consumers), controllers (Auth, AI), service logic (auth, users, dashboard, jobs, notifications, gamification, admin, AI, email, levels, PDF), AI providers (sample/heuristic providers, Whisper), repository (question bank).
- Uses H2 in-memory (`application-test.yml`) and `rabbitmq-mock`; no external services needed. If RabbitMQ connection-refused logs appear in a local run they are expected (no broker) and tests pass via graceful degradation.

**Frontend** (`cd frontend && npm test`) — Vitest unit tests for `src/utils` (validation, error helpers).

**Not present:** frontend component/E2E tests, API contract tests, performance tests, postman collections (folder is empty).

---

## Troubleshooting

- **`docker compose up` fails on network** — the compose file uses an external network; create it first: `docker network create devlaunch-network`.
- **Backend can't reach MySQL** — the compose MySQL maps host port `3307`; set `SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3307/devlaunch…` for local runs against the compose database.
- **RabbitMQ/Redis "connection refused" logs** — infra isn't running; start it with `docker compose up -d` in `docker/`. The app stays functional (events dropped with a log; cache falls back to DB).
- **CORS errors or wrong reset links** — `FRONTEND_BASE_URL` must match the frontend origin (CORS allowed origin and reset-link base both come from it).
- **Deployed frontend calls the wrong API** — `VITE_API_URL` is baked at build time; rebuild with the correct value (CD passes it via the repository variable).
- **Swagger 401s** — click "Authorize" in Swagger UI and paste `Bearer <token>` from a login.

---

## Future Improvements

- Flyway/Liquibase database migrations (replace `ddl-auto: update`)
- Refresh-token flow + logout/revocation
- Rate limiting and account lockout on login/forgot-password
- Global leaderboard (cache name is reserved)
- Question-bank admin management
- ESLint config and frontend component/E2E tests
- OAuth login (Google/GitHub), LinkedIn integration, portfolio generator
- Full dark-mode theme (toggle exists; light theme is active)

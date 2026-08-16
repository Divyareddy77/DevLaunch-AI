# DevLaunch - System Architecture

## Project Information

| Field | Details |
|--------|----------|
| Project Name | DevLaunch |
| Document | System Architecture |
| Architecture Style | Modular Monolith |
| Backend | Spring Boot |
| Frontend | React + TypeScript |
| Database | MySQL |

---

# 1. Introduction

This document describes the overall architecture of the DevLaunch application.

The project follows a **Modular Monolith Architecture** with **Layered Architecture** principles. Each module is logically separated while remaining within a single deployable Spring Boot application.

This approach provides clean code organization, easier maintenance, and the flexibility to migrate individual modules into microservices in the future if required.

---

# 2. High-Level Architecture

```
                    +---------------------------+
                    |     React Frontend        |
                    |   (TypeScript + Vite)     |
                    +------------+--------------+
                                 |
                           HTTP / REST APIs
                                 |
                    +------------v--------------+
                    |    Spring Boot Backend    |
                    +------------+--------------+
                                 |
             +-------------------+-------------------+
             |                   |                   |
      Authentication       Business Logic       Notifications
             |                   |                   |
             +-------------------+-------------------+
                                 |
                         Spring Data JPA
                                 |
                    +------------v--------------+
                    |        MySQL Database      |
                    +---------------------------+
```

---

# 3. Architecture Pattern

The application follows a **Modular Monolith** architecture.

Each feature is developed as an independent module while sharing a common application runtime.

Modules include:

- Authentication
- Resume Builder
- Job Tracker
- Study Planner
- AI Mock Interview
- GitHub Analytics
- LeetCode Tracker
- Dashboard
- Notification Service
- Admin Module

---

# 4. Layered Architecture

Each module follows the same layered structure.

```
Controller
      │
      ▼
Service Interface
      │
      ▼
Service Implementation
      │
      ▼
Repository
      │
      ▼
Database
```

Supporting layers:

- DTO
- Entity
- Mapper
- Validation
- Exception Handling
- Security
- Configuration

---

# 5. Backend Package Structure

```
backend/
└── src/main/java/com/devlaunch
    ├── cache                    # Redis cache manager, TTLs, graceful error handler
    ├── config                   # Security, OpenAPI, data initializer
    ├── controller               # REST controllers (one per module)
    ├── dto
    │   ├── request
    │   └── response
    ├── entity                   # JPA entities + enums
    ├── exception                # GlobalExceptionHandler + custom exceptions
    ├── mapper                   # MapStruct mappers
    ├── messaging                # RabbitMQ publisher, consumers, events, config
    ├── repository
    ├── security                 # JWT service/filter, custom user details
    ├── service
    │   ├── interfaces
    │   ├── impl
    │   └── ai                   # AI providers (OpenAI + deterministic fallbacks)
    ├── util
    └── DevLaunchApplication.java
```

---

# 6. Frontend Structure

```
frontend/src/
├── api          # Axios client + endpoint constants
├── components   # feature components + ui design system
├── constants    # routes, storage keys, interview config, resume templates
├── context      # Auth, Theme, Notification providers
├── hooks        # useAuth, useWebcam, useMediaRecorder, useResumeDownload, …
├── layouts      # AuthLayout, DashboardLayout
├── pages        # auth, dashboard, module and admin pages
├── routes       # central route table with guards
├── services     # 14 typed API service modules
├── types        # TypeScript interfaces
├── utils        # validation, date, format, jwt, interview, …
├── index.css    # Tailwind + design-system keyframes
├── main.tsx     # React root
└── App.tsx      # providers + router + Toaster
```

---

# 7. Module Responsibilities

### Authentication

- Registration
- Login
- JWT Authentication
- Profile Management

---

### Resume Builder

- Resume CRUD
- Resume Templates
- PDF Export

---

### Job Tracker

- Add Job Applications
- Update Status
- Search & Filter

---

### Study Planner

- Daily Tasks
- Weekly Planner
- Progress Tracking

---

### Dashboard

- Analytics
- Progress Summary
- Career Statistics

---

### Notification Service

- Interview Reminders
- Study Reminders
- Application Updates

---

### Admin

- User Management
- Reports
- Announcements

---

# 8. Request Flow

```
User

↓

React UI

↓

Axios

↓

REST API

↓

Controller

↓

Service

↓

Repository

↓

MySQL
```

---

# 9. Security Architecture

Security components:

- Spring Security
- JWT Authentication (stateless, HMAC-SHA256)
- BCrypt Password Encryption
- Role-Based Authorization (STUDENT / ADMIN)
- CORS Configuration (allowed origin from `FRONTEND_BASE_URL`, OPTIONS preflight permitted)
- Request Validation
- Global Exception Handling

---

# 10. Database Access

The application uses:

- Spring Data JPA
- Hibernate ORM
- Repository Pattern

Business logic never directly interacts with the database.

---

# 11. Exception Handling

The project uses centralized exception handling.

Examples:

- ResourceNotFoundException
- DuplicateEmailException
- UnauthorizedException
- ValidationException

These are handled using a global exception handler to return consistent API responses.

---

# 12. Logging Strategy

Application logging is implemented using Spring Boot's logging infrastructure (SLF4J/Logback), configured in `application.yml`: `logging.level` (root INFO, `com.devlaunch` DEBUG), a console pattern, and a rolling file at `logs/devlaunch-backend.log` (10 MB × 30). Services log via Lombok `@Slf4j`, and the messaging layer uses the `MessagingLog` helper for structured publish/retry/DLQ logging.

Logs include:

- Authentication Events
- API Requests
- Errors
- Warnings

Sensitive information — passwords, tokens, and event payload contents such as password-reset tokens — is never logged (see `RabbitEventPublisher`/`MessagingLog`).

---

# 13. Deployment Architecture

The application is deployed to **Microsoft Azure Container Apps** using the **existing** resources below (the CD pipeline updates only the container image on these apps — nothing is created or reconfigured by deployment):

```
GitHub → GitHub Actions
   ├── CI  (validation: backend tests, frontend build, docker builds)
   └── CD  (Azure OIDC login → push images to devlaunchacr → update Container Apps)
              │
              ▼
Azure Container Registry (devlaunchacr)
              │
              ▼
Azure Container Apps
   ├── devlaunch-frontend  (nginx :80)
   └── devlaunch-backend   (Spring Boot :8080)
              │
              ▼
MySQL 8 · Redis · RabbitMQ  (managed by the existing environment configuration)
```

- **Resources (existing, source of truth):** resource group `devlaunch-rg`, registry `devlaunchacr`, Container Apps `devlaunch-backend` + `devlaunch-frontend`, Container Apps environment `devlaunch-env`.
- **Images:** immutable tags `devlaunchacr.azurecr.io/devlaunch-{backend,frontend}:${{ github.sha }}` (no `latest`).
- **Authentication:** GitHub Actions Azure OIDC federated credentials (`AZURE_CLIENT_ID`, `AZURE_TENANT_ID`, `AZURE_SUBSCRIPTION_ID`).
- For local development the same components run via `docker/docker-compose.yml` (MySQL, Redis, RabbitMQ, backend, frontend).

---

# 14. Architectural Principles

The application follows these principles:

- Separation of Concerns
- Single Responsibility Principle
- Open/Closed Principle
- Dependency Injection
- Reusability
- Scalability
- Maintainability

---

# 15. Achievements & Gamification

A dedicated gamification bounded context rewards users for platform activity:

- **Event-driven evaluation.** Every module publishes a lightweight `ActivityEvent` on the `gamification.activity` routing key when a resume is created, an ATS review completes, a job application is added, a mock interview finishes, a study task is completed, a GitHub/LeetCode account is connected (or freshly synced via the `AccountSyncEventPublisher`), or the placement readiness score updates. The `AchievementActivityConsumer` resolves the user and delegates to the gamification service.
- **Single transactional write path.** `GamificationServiceImpl#recordActivity` awards the activity XP, evaluates the full badge catalog (progress computed from repository counts, event values, and the cached GitHub/LeetCode profiles), unlocks newly satisfied badges (awarding their XP reward), and raises the unlock / level-up notifications via the shared `NotificationEventProcessor` — all in one transaction. The unique constraint on `user_achievements` is the duplicate-prevention backstop.
- **Level system.** `LevelService` maps total XP to a level using the explicit spec table (0/100/250/500/900/1400) and continues the curve automatically from level 7 (increment grows by 50 per level).
- **Redis caching.** Summary, progress, unlocked badges, and XP history are cached under the `achievements` cache (5-minute TTL, user-scoped keys) and evicted on every activity write; a `leaderboard` cache name is reserved for the future global ranking.
- **No coupling back into business modules.** Services only publish events; they never read or write gamification tables, and the gamification module never modifies existing REST contracts (the dashboard and profile simply consume the new read endpoints).

---

# 16. Conclusion

The DevLaunch architecture is designed to provide a clean, modular, secure, and scalable foundation for the application.

The modular monolith approach allows rapid development while maintaining the flexibility to migrate individual modules into microservices in future versions if required.
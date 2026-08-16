# 07 — Implementation Status

**Purpose:** Tracks the real implementation status of every DevLaunch module. Each entry is verified against the actual code in this repository — nothing here is assumed from the design docs. Status legend:

- ✅ **COMPLETED** — fully implemented and verified in code
- 🟡 **PARTIALLY IMPLEMENTED** — some parts exist, others do not
- 🔴 **FUTURE / NOT IMPLEMENTED** — planned or mentioned, but not present in the code

> Source of truth: `backend/src/main/java`, `frontend/src`, `.github/workflows`, `docker/`, and the README. See `DEVLAUNCH_COMPLETE_TECHNICAL_DOCUMENTATION.md` for the full technical detail behind each row.

---

# 1. Completed Modules (✅)

| Module | Status | Backend evidence | Frontend evidence |
|---|---|---|---|
| Authentication (register / login / JWT / BCrypt) | ✅ | `AuthController`, `AuthServiceImpl`, `SecurityConfig`, `JwtService`, `JwtAuthenticationFilter` | `LoginPage`, `RegisterPage`, `AuthContext`, `api/client.ts` |
| Profile management + change password | ✅ | `UserController`, `UserServiceImpl` | `ProfilePage`, `ProfileForm`, `ChangePasswordForm` |
| Password reset (forgot / reset, async email) | ✅ | `AuthServiceImpl`, `PasswordResetToken`, `EmailServiceImpl`, RabbitMQ `ForgotPasswordEmailConsumer` | `ForgotPasswordPage`, `ResetPasswordPage` |
| Resume builder (multi-resume, 6 sections, 4 templates) | ✅ | `ResumeController` + 6 section controllers, `ResumeTemplateController`, `DataInitializer` seeds | `ResumePage`, `CreateResumePage`, `EditResumePage`, `components/resume/*` |
| Resume PDF export (server-side, OpenPDF) | ✅ | `ResumePdfController`, `ResumePdfServiceImpl`, `ResumePdfTemplate` | `useResumeDownload` hook, download buttons |
| Job application tracker (status pipeline, priorities, work modes) | ✅ | `JobApplicationController` (19 endpoints), `JobApplicationServiceImpl` | `JobApplicationsPage` (Kanban), forms, filters |
| Timeline, interview scheduling, notes, attachments | ✅ | `ApplicationTimelineEvent`, `InterviewSchedule`, `InterviewNote`, `ApplicationAttachment`, `LocalAttachmentStorageService` | `TimelinePanel`, `InterviewsPanel`, `InterviewNotesPanel`, `AttachmentsPanel` |
| Study planner (flat task model, calendar, streaks, reminders) | ✅ | `StudyPlannerController`, `StudyPlannerServiceImpl`, `StudyPlannerOverdueScheduler` | `StudyPlannerPage`, `CalendarView`, `StudyFilters` |
| GitHub analytics (live REST, Redis-cached) | ✅ | `GitHubController`, `GitHubServiceImpl`, `@Cacheable("github")` | `GitHubAnalyticsPage`, `components/github/*` |
| LeetCode tracker (live GraphQL, Redis-cached) | ✅ | `LeetCodeController`, `LeetCodeServiceImpl`, `@Cacheable("leetcode")` | `LeetCodeTrackerPage`, `components/leetcode/*` |
| AI mock interview (5 categories, text + voice answers, history) | ✅ | `AiController`, `AiServiceImpl`, `InterviewQuestionBankServiceImpl`, `Sample/OpenAiMockInterviewProvider`, `OpenAiWhisperTranscriber` | `MockInterviewPage`, `components/ai/*`, `useMediaRecorder`, `useWebcam` |
| AI resume review / ATS analysis | ✅ | `AiServiceImpl.reviewResume`, `OpenAi/SampleResumeReviewProvider`, `ResumeReview` | `ResumeReviewPage`, `components/ai/*` |
| Interview question bank (500 seeded questions, 5 categories) | ✅ | `InterviewQuestion` entity, `data.sql`, `InterviewQuestionBankServiceImpl` | `MockInterviewSetup` category picker |
| Gamification: XP ledger, levels, 17 badges | ✅ | `GamificationServiceImpl`, `LevelService`, `XpHistory`, `AchievementDefinition` (17 seeds) | `AchievementsPage`, `LevelCard`, `AchievementCard`, `Confetti`, `UnlockPopup` |
| Placement readiness score (weighted 0–100 + snapshots) | ✅ | `DashboardServiceImpl.calculatePlacementReadiness`, `ReadinessSnapshot` | `PlacementReadinessCard` |
| Notification center (9 types, async via RabbitMQ) | ✅ | `NotificationController`, `NotificationServiceImpl`, `messaging/consumer/*` | `NotificationsPage`, `NotificationBell`, `NotificationContext` |
| Announcements + feedback | ✅ | `AnnouncementController`, `FeedbackController`, `AdminServiceImpl` (fan-out) | `AnnouncementBanner`, admin announcement/feedback pages |
| Admin module (users, resumes, jobs, study, AI reports, announcements, feedback) | ✅ | `AdminController` (22 endpoints), `AdminServiceImpl` | 8 admin pages under `/admin`, `AdminRoute` |
| Swagger / OpenAPI with JWT bearer | ✅ | `OpenApiConfig`, springdoc dependency | — |
| Redis caching with graceful degradation | ✅ | `cache/*` (9 caches, TTLs, `GracefulCacheErrorHandler`) | — |
| RabbitMQ messaging with retry + DLQ | ✅ | `messaging/config/RabbitMQConfig.java` (11 queues + DLQ, 3 retries, backoff) | — |
| Docker + Docker Compose full stack | ✅ | `backend/Dockerfile`, `frontend/Dockerfile`, `docker/docker-compose.yml`, `nginx.conf` | — |
| CI (tests + builds + Docker validation) | ✅ | `.github/workflows/ci.yml` | — |
| CD to existing Azure Container Apps (OIDC, immutable SHA tags) | ✅ | `.github/workflows/cd.yml` | — |
| Backend test suite (35 classes, 208 tests) | ✅ | `backend/src/test/**` | — |

# 2. Partially Implemented Modules (🟡)

| Module | What exists | What is missing / different from the original plan |
|---|---|---|
| Study planner hierarchy | Flat `study_planners` task model (fully functional) | The original plan's `study_plans` → `study_tasks` hierarchy was merged into a single table — no plan/task split |
| Dark mode theme | `ThemeContext` toggle + localStorage persistence + `data-theme` attribute | Code comment states only the light theme is active; dark-mode styling is not fully applied |
| GitHub / LeetCode integration | Username-based live fetching with Redis caching (fully functional) | No OAuth; profiles are not persisted locally (offline dashboard would show empty values) |
| Voice answers in mock interview | `useMediaRecorder` + Whisper transcription (fully functional) | Requires an `AI_PROVIDER_API_KEY`; without a key only text answers are practical |

# 3. Future / Not Implemented (🔴)

| Feature | Evidence / note |
|---|---|
| OAuth login (Google / GitHub) | Only username linking exists — no OAuth handshake anywhere |
| Refresh-token flow | `jwt.refresh-token.expiration` is configured in `application.yml` but no refresh endpoint or client logic exists |
| Global leaderboard | Only a reserved `CacheNames.LEADERBOARD` constant exists; not wired into the cache manager or any endpoint |
| Question-bank admin management | `InterviewQuestion` javadoc mentions a "future admin module"; no admin endpoint exists for the question bank |
| Rate limiting / account lockout | No rate-limit filter or lockout mechanism on login / forgot-password |
| Database migration tooling | Schema is managed with `ddl-auto: update`; no Flyway/Liquibase |
| ESLint configuration | `npm run lint` invokes `eslint .` but no `eslint.config.*` is shipped (the script fails until one is added) |
| Frontend component / route / context tests | Only two Vitest files exist (`utils/validation.test.ts`, `utils/error.test.ts`) |
| E2E tests (Playwright / Cypress) | Not present |
| API contract tests / performance tests | Not present |
| Postman collections | `postman/` contains only `.gitkeep` |
| LinkedIn integration / portfolio generator / mobile app | Mentioned in `docs/01` as future scope; not implemented |

---

# 4. Maintenance Notes

- **Seed data** (`backend/src/main/resources/data.sql`) is idempotent (`INSERT IGNORE`) and runs on every startup: 500 interview questions (100 per category × 5) + 17 achievement definitions.
- **Default data** (`DataInitializer`): `STUDENT` / `ADMIN` roles and four resume templates. A default development admin account is initialized by `DataInitializer`. Production deployments should replace/disable default credentials and use secure secret management.
- When implementing a new feature, update this file together with `docs/05_API_CONTRACT.md` (endpoints) and `docs/04_DATABASE.md` (schema) so the documentation stays in sync with the code.

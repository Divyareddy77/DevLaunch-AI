# 09 — Testing

**Purpose:** Documents the actual test suites of the DevLaunch project — what is covered, how to run the tests, and what is not covered yet.

---

# 1. Test Stack

| Area | Tooling |
|---|---|
| Backend | JUnit 5, Mockito, Spring MockMvc, H2 (in-memory, `MODE=MySQL`), `rabbitmq-mock` (in-process AMQP broker) |
| Frontend | Vitest |
| CI | The GitHub Actions `CI` workflow runs the backend tests and the frontend build on every PR/push to `develop`/`main` (`.github/workflows/ci.yml`) |

---

# 2. Backend Tests

Run with the Maven wrapper from the `backend/` directory:

```bash
./mvnw test
```

**Current state: 35 test classes, 208 tests, 0 failures / 0 errors / 0 skipped.**

Test files live in `backend/src/test/java/` and use the `test` profile (`application-test.yml` — H2 in-memory, `ddl-auto: create-drop`, `sql.init.mode: never`). No external services are required.

Coverage by area:

- **Cache** — `CacheBehaviorIntegrationTest`, `CacheGracefulDegradationTest`, `GracefulCacheErrorHandlerTest`, `LoggingCacheTest`, `RedisCacheConfigTest`, `UserCacheKeyGeneratorTest` (TTLs, serializers, fallback on Redis failure)
- **Messaging** — `RabbitEventPublisherTest`, `RabbitMqMessagingIntegrationTest` (rabbitmq-mock), `RabbitMQConfigTest`, consumer tests (`AchievementActivity`, `ForgotPasswordEmail`, `MockInterviewCompleted`, `NotificationEventProcessor`, `ResumeReviewCompleted`, `StudyReminder`)
- **Controllers** — `AuthControllerTest`, `AiControllerTest`
- **Services** — `AuthServiceImplTest`, `UserServiceImplTest`, `DashboardServiceImplTest`, `JobApplicationServiceImplTest`, `NotificationServiceImplTest`, `GamificationServiceImplTest`, `AdminServiceImplTest`, `AiServiceImplTest`, `EmailServiceImplTest`, `LevelServiceTest`, `InterviewQuestionBankServiceImplTest`, `ResumePdfServiceImplTest`
- **AI providers** — `SampleMockInterviewProviderTest`, `SampleResumeReviewProviderTest`, `OpenAiWhisperTranscriberTest`, `OpenAiWhisperTranscriberHttpTest`, `QuestionConceptsCoverageTest`
- **Repository** — `InterviewQuestionRepositoryTest`

> If RabbitMQ "connection refused" logs appear in a local run they are expected (no broker is running); the application degrades gracefully and the tests still pass.

# 3. Frontend Tests

Run with npm from the `frontend/` directory:

```bash
npm test            # vitest run
npm run build       # production build (tsc -b && vite build) — also validated in CI
```

**Current state:** unit tests for `src/utils/` — `validation.test.ts` and `error.test.ts` (Vitest).

# 4. What Is Not Covered Yet

- Frontend component / route / context tests
- End-to-end (E2E) tests (no Playwright/Cypress)
- API contract tests
- Performance / load tests
- Postman collections (the `postman/` folder is empty)

These are planned improvements, not current capabilities.

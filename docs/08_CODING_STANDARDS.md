# 08 — Coding Standards

**Purpose:** Documents the coding standards and conventions actually used in this repository, derived from the real code in `backend/`, `frontend/`, and the git history. When contributing, match these existing patterns.

---

# 1. Language & Tooling

| Area | Standard |
|---|---|
| Backend language | Java 21 (records, switch expressions, text blocks in use) |
| Backend build | Maven (`backend/pom.xml`), Maven wrapper `./mvnw` committed |
| Frontend language | TypeScript 5.5 in **strict mode** (`strict`, `noUnusedLocals`, `noUnusedParameters`, `noFallthroughCasesInSwitch`) |
| Frontend build | Vite 5 (`vite.config.ts`), `npm run build` = `tsc -b && vite build` |
| Styling | Tailwind CSS 3.4 (`tailwind.config.ts`), custom `primary` indigo palette, design-system classes + keyframes in `index.css` |

# 2. Backend Structure & Patterns

## 2.1 Package layout

```
com.devlaunch
├── cache        # Redis cache config, cache-name constants, key generator, error handler
├── config       # Security, OpenAPI, data initializer
├── controller   # REST controllers — thin, one per module
├── dto          # request/ + response/ DTOs
├── entity       # JPA entities + enums/
├── exception    # GlobalExceptionHandler + custom exceptions
├── mapper       # MapStruct mappers
├── messaging    # RabbitMQ publisher, config, consumers, event records
├── repository   # Spring Data JPA repositories
├── security     # JWT service/filter, user details
└── service      # interfaces/ + impl/ + ai/ providers
```

## 2.2 Layering rules

- **Controller → Service interface → Service implementation → Repository.** Controllers never contain business logic and never touch repositories directly.
- Service interfaces live in `service/interfaces`, implementations in `service/impl` (`@Service`), named `XxxServiceImpl`.
- Entities never leave the service layer — controllers only exchange DTOs.
- Every cross-module side effect is an **event published on RabbitMQ** (emails, notifications, XP/badges); services do not call each other's notification logic directly.

## 2.3 Conventions applied throughout

- **Constructor injection** everywhere — no field injection (`@Autowired` on fields is not used).
- **Lombok** for boilerplate: `@Getter`, `@Setter`, `@Builder`, `@RequiredArgsConstructor`, `@Slf4j` on classes; record types used for the messaging event payloads (`messaging/event/*`).
- **MapStruct** (`@Mapper(componentModel = "spring")`) for DTO ↔ entity mapping; system-managed fields (e.g. `user`, timestamps) are explicitly ignored with `@Mapping(target = "...", ignore = true)` and populated in the service layer.
- **Bean Validation** on every request DTO (`@NotBlank`, `@Email`, `@Size`, …) + `@Valid` in controllers; errors are centralized in `GlobalExceptionHandler` → `ErrorResponse { timestamp, status, error, message, path }`.
- **Javadoc** on every public class and public method (with `{@link}` references), `@author DevLaunch`, and a `What → Why → How` style comment header on complex logic. Methods with long signatures keep their parameters documented with `@param`/`@return`.
- **Enums** with Javadoc on every constant; enum constants are UPPER_SNAKE_CASE. Human-readable labels are centralized in `util/EnumLabels.java`.
- **Constants** are grouped in dedicated holder classes (`cache/CacheNames.java`, `messaging/EventTopics.java`, `security/...`) with private constructors — no string literals scattered across services.
- **Logging** via `@Slf4j`; debug for cache hit/miss and internal detail, warn for degradations (Redis down, provider fallback), info for lifecycle events. Never log passwords, tokens, or full authorization headers.
- **Graceful degradation** is a project-wide rule: Redis down → fall back to DB; RabbitMQ down → log and drop events; AI provider missing/failing → deterministic fallback providers; GitHub/LeetCode outage → empty defaults in the dashboard.

# 3. Frontend Structure & Patterns

## 3.1 Layout

```
src/
├── api/        # Axios client (interceptors) + endpoint URL constants
├── components/ # feature components + ui/ design system; barrel index.ts per folder
├── constants/  # routes, storage keys, messages, app config, interview, resume templates
├── context/    # Auth, Theme, Notification providers (React Context only — no Redux/Zustand)
├── hooks/      # useAuth, useCountUp, useMediaRecorder, useResumeDownload, useWebcam
├── layouts/    # AuthLayout, DashboardLayout
├── pages/      # one folder per feature; admin pages under pages/admin
├── routes/     # central route table with guards
├── services/   # one typed API module per backend module
├── types/      # per-module TypeScript interfaces
└── utils/      # validation, date, error, format, jwt, navigation, interview, speaking
```

## 3.2 Conventions applied throughout

- **Functional components + hooks only** (`useState`, `useEffect`, `useContext`, `useMemo`, `useCallback`); no class components.
- **Typed everything**: explicit interfaces for props, context values, service responses, and forms (react-hook-form + zod schemas via `@hookform/resolvers`).
- **Route paths** are centralized in `constants/routes.ts` and imported everywhere (including `routes/index.tsx`); sidebar items live in `utils/navigation.ts`.
- **API access** goes through `services/*.ts` which call the shared Axios instance in `api/client.ts` (JWT request interceptor + 401 response interceptor); no raw `fetch`/`axios` calls in components.
- **Forms** use `react-hook-form` + zod; submit handlers set loading state, call the service, toast on success (`react-hot-toast`), and show extracted backend messages on failure (`utils/error.ts`).
- **JSDoc header comments** on every module (`/** ... */`) with `@author DevLaunch`.
- **Design system**: reusable primitives in `components/ui/` (Button, Card, Input, Modal, Badge, Spinner, …); charts through recharts wrappers in `components/charts/`.
- **Barrel exports** (`index.ts`) for component folders, imported with named imports.
- **Error/loading states**: `ErrorMessage`, `EmptyState`, `LoadingScreen`, skeleton shimmer classes; every data fetch handles loading, error, and empty cases.

# 4. API Conventions

- REST under `/api/**`, JSON; `GET/POST/PUT/PATCH/DELETE` with `@RequestMapping` prefixes on the controller class.
- Auth column: **Public** (`/api/auth/**`, Swagger), **Admin** (`/api/admin/**`, `ROLE_ADMIN`), everything else authenticated.
- Ownership is enforced in every service (the requested entity must belong to the authenticated user).
- Success responses are plain DTOs; errors are always the standardized `ErrorResponse`.
- New endpoints must be added to `docs/05_API_CONTRACT.md` and, if persisted, `docs/04_DATABASE.md`.

# 5. Database Conventions

- Schema is managed by Hibernate `ddl-auto: update` (no migration tool yet).
- Every audited entity extends `BaseEntity` (`id`, `created_at`, `updated_at`).
- Relationship annotations use lazy fetching (`FetchType.LAZY`) with `open-in-view: false`.
- Seed data lives in `resources/data.sql` and **must stay idempotent** (`INSERT IGNORE` + unique keys).
- Reference data seeded at startup in `config/DataInitializer.java` (roles, templates, default admin) — idempotent existence checks only.

# 6. Testing Standards

- **Backend**: JUnit 5 + Mockito; unit tests for services/consumers, `@SpringBootTest` with `@ActiveProfiles("test")` (H2 `MODE=MySQL`, `sql.init.mode: never`) for integration behavior; `rabbitmq-mock` for messaging without a live broker.
- **Frontend**: Vitest for pure utility functions (`src/utils/*.test.ts`).
- CI runs `./mvnw test` (backend) and `npm run build` (frontend) — keep them green on every change.

# 7. Git Workflow

- **Branching**: Git-Flow-inspired — feature branches (`feature/*`) merged into `develop` via PRs; stable milestones merged from `develop` into `main` via PRs; `main` is the deployable branch.
- **Commit messages**: Conventional Commits as used in this repository's history:
  - `feat(auth): implement secure password reset flow`
  - `feat(achievements): add gamification and achievement system`
  - `docs: update documentation to match implementation`
  - `test: verify automatic deployment`
  - `ci: add CI/CD workflows for Azure deployment`
  - `chore: prepare project for CI/CD`
- One logical change per commit; documentation and code changes that belong together are committed together (as in `docs: update documentation to match implementation`).

# 8. Documentation Standards

- The docs in `docs/` describe the **implemented** system — when behavior changes, update the relevant doc in the same change.
- `README.md` is the entry point and includes a documentation map (see the "Documentation Map" section).
- Implemented vs planned features are kept strictly separated (✅/🟡/🔴) — see `docs/07_TASKS.md`.

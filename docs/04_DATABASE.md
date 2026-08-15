# DevLaunch - Database Design

## Project Information

| Field | Details |
|--------|----------|
| Project Name | DevLaunch |
| Database | MySQL 8+ (H2 `MODE=MySQL` for tests) |
| ORM | Spring Data JPA (Hibernate) |
| Schema handling | `ddl-auto: update` (auto-created; **no migration tool**) |
| Auditing | `BaseEntity` (`id`, `created_at`, `updated_at`) via `@EnableJpaAuditing` |

> **Note:** this document describes the database schema **as actually implemented**. Where the original design (`docs/02`, earlier drafts) differs, the implemented schema is authoritative — see [§9 Deviations from the original plan](#9-deviations-from-the-original-plan).

---

# 1. Introduction

The database stores user information, resumes (with their sections), job applications (with timeline/interviews/notes/attachments), study planner tasks, interview sessions, resume reviews, password reset tokens, notifications, announcements, feedback, and the gamification tables (achievements, XP ledger, readiness snapshots).

The schema is created by Hibernate from the JPA entities in `backend/src/main/java/com/devlaunch/entity/`, and `data.sql` seeds the interview question bank (500 questions across 5 categories) and 17 achievement definitions on every startup (idempotent `INSERT IGNORE`).

---

# 2. Entity Overview

The application consists of the following entities (actual):

- Users, Roles
- Resumes, ResumeTemplates
- Education, Skills, Projects, Experience, Certifications, Achievements (resume sections)
- JobApplications, ApplicationTimelineEvents, InterviewSchedules, InterviewNotes, ApplicationAttachments
- StudyPlanners (flat task model — no plan/task split)
- InterviewQuestions, InterviewSessions (+ question snapshots)
- ResumeReviews
- PasswordResetTokens
- Notifications, Announcements, Feedback
- AchievementDefinitions, UserAchievements, XpHistory
- ReadinessSnapshots

---

# 3. Entity Relationship Overview

```
Role ─┬─ User
User ─┬─ Resume ─┬─ Education / Skill / Project / Experience / Certification / Achievement
      │          └─ ResumeTemplate (optional, Many-to-One)
      ├─ JobApplication ─┬─ ApplicationTimelineEvent
      │                  ├─ InterviewSchedule
      │                  ├─ InterviewNote
      │                  └─ ApplicationAttachment
      ├─ StudyPlanner
      ├─ InterviewSession ── InterviewSessionQuestion (element collection)
      ├─ ResumeReview
      ├─ Notification
      ├─ Feedback
      ├─ Announcement (created_by)
      ├─ PasswordResetToken
      ├─ UserAchievement ── AchievementDefinition
      ├─ XpHistory
      └─ ReadinessSnapshot
```

---

# 4. Tables (as implemented)

## Roles — `roles`

| Column | Type | Notes |
|----------|------|------|
| id | BIGINT PK | auto-increment |
| role_name | VARCHAR (enum) | `STUDENT`, `ADMIN` — unique, seeded by `DataInitializer` |

## Users — `users`

| Column | Type | Notes |
|----------|------|------|
| id | BIGINT PK | auto-increment |
| first_name / last_name | VARCHAR | |
| email | VARCHAR | **unique** |
| password | VARCHAR | BCrypt hash (`@JsonIgnore` on the API DTO) |
| phone | VARCHAR | |
| github_username | VARCHAR | linked GitHub username (live-fetched, not persisted separately) |
| leetcode_username | VARCHAR | linked LeetCode username |
| is_active | BOOLEAN | admin can deactivate; inactive users cannot log in |
| role_id | BIGINT FK → roles.id | |
| created_at / updated_at | TIMESTAMP | BaseEntity auditing |

## Resume Templates — `resume_templates`

| Column | Type | Notes |
|----------|------|------|
| id | BIGINT PK | |
| name | VARCHAR | unique — Professional, Modern, Minimal, Creative (seeded) |
| description | VARCHAR | |
| preview_image_url | VARCHAR | |

## Resumes — `resumes`

| Column | Type | Notes |
|----------|------|------|
| id | BIGINT PK | |
| user_id | BIGINT FK → users.id | |
| template_id | BIGINT FK → resume_templates.id | optional |
| headline | VARCHAR | |
| summary | TEXT | |
| linkedin_url / github_url / portfolio_url | VARCHAR | |

## Resume Sections

Each section is a child of a resume (`resume_id` FK, cascade).

### Education — `educations`

institution_name, degree, field_of_study, grade, start_date, end_date, currently_studying, description

### Experience — `experiences`

company_name, job_title, employment_type, location, start_date, end_date, currently_working, description

### Projects — `projects`

project_name, description, technologies, github_url, live_url, start_date, end_date, currently_working

### Skills — `skills`

skill_name, proficiency

### Certifications — `certifications`

certification_name, issuing_organization, issue_date, expiry_date, credential_id, credential_url

### Achievements — `achievements`

title, description, date_achieved

*(Resume "achievements" — distinct from the gamification badges in `achievement_definitions`.)*

## Job Applications — `job_applications`

| Column | Type | Notes |
|----------|------|------|
| id | BIGINT PK | |
| user_id | BIGINT FK → users.id | |
| resume_id | BIGINT FK → resumes.id | optional link |
| company_name, job_role, company_location | VARCHAR | |
| job_type, salary, application_date | | |
| status | VARCHAR (enum) | `WISHLIST` → `APPLIED` → `ASSESSMENT` → `INTERVIEW` → `OFFER` → `REJECTED` |
| work_mode | VARCHAR (enum) | `REMOTE`, `HYBRID`, `ONSITE` |
| priority | VARCHAR (enum) | `LOW`, `MEDIUM`, `HIGH`, `URGENT` |
| job_url, company_website | VARCHAR | |
| recruiter_name, recruiter_email, referral | | |
| technology, notes | VARCHAR / TEXT | |

### Application Timeline Events — `application_timeline_events`

event_type (enum: ADDED, APPLIED, ASSESSMENT, INTERVIEW, OFFER, REJECTED, STATUS_UPDATED, INTERVIEW_SCHEDULED, INTERVIEW_CANCELLED, ATTACHMENT_ADDED), title, notes, occurred_at — child of job_application

### Interview Schedules — `interview_schedules`

title, round, scheduled_date, scheduled_time, meeting_link, interviewer, notes, cancelled — child of job_application

### Interview Notes — `interview_notes`

content — child of job_application

### Application Attachments — `application_attachments`

file_name, stored_file_name (opaque), content_type, file_size, category (enum: RESUME, COVER_LETTER, OFFER_LETTER, ASSESSMENT, INTERVIEW_FEEDBACK, OTHER) — child of job_application; file bytes stored on disk under `UPLOAD_DIR` (`./uploads`)

## Study Planners — `study_planners`

**Flat task model** — each row is one scheduled task (there is no plan/task hierarchy):

| Column | Type | Notes |
|----------|------|------|
| id | BIGINT PK | |
| user_id | BIGINT FK → users.id | |
| title, description | VARCHAR / TEXT | |
| study_date | DATE | |
| start_time / end_time | TIME | optional |
| priority | VARCHAR (enum) | `LOW`, `MEDIUM`, `HIGH` |
| status | VARCHAR (enum) | `PENDING`, `IN_PROGRESS`, `COMPLETED` |

## Interview Questions — `interview_questions`

category (enum: HR, JAVA, SPRING_BOOT, SQL, REACT), question, difficulty (enum: EASY, MEDIUM, HARD), active — **unique (category, question)**; 500 rows seeded by `data.sql`

## Interview Sessions — `interview_sessions`

| Column | Type | Notes |
|----------|------|------|
| id | BIGINT PK | |
| user_id | BIGINT FK → users.id | |
| session_id | VARCHAR | client-generated UUID |
| interview_type | VARCHAR (enum) | HR / JAVA / SPRING_BOOT / SQL / REACT |
| overall_score | INT | |
| question_count | INT | |
| difficulty | VARCHAR (enum) | EASY / MEDIUM / HARD / MIXED |
| timed | BOOLEAN | |
| duration_seconds / word_count | INT | |
| technical / communication / confidence / problem_solving / clarity / vocabulary / professionalism | INT | per-dimension scores |
| completed_at | TIMESTAMP | |

### Interview Session Questions — element collection (`interview_session_questions`)

question_id, question, answer, score, feedback, improved_answer (+ question_order) — snapshotted per session; sessions also store `strengths` / `improvements` / `suggestions` string element collections

## Resume Reviews — `resume_reviews`

user_id FK, resume_id FK, target_role, resume_score, ats_score — only the score summary is persisted (the full AI report is returned but not stored)

## Password Reset Tokens — `password_reset_tokens`

| Column | Type | Notes |
|----------|------|------|
| id | BIGINT PK | |
| user_id | BIGINT FK → users.id | |
| token | VARCHAR | unique, 64-hex (256-bit) |
| expires_at | TIMESTAMP | default 30 min |
| used | BOOLEAN | single-use |

## Notifications — `notifications`

user_id FK, title, message, type (enum: ANNOUNCEMENT, RESUME, JOB, STUDY, MOCK_INTERVIEW, RESUME_REVIEW, READINESS, ACHIEVEMENT, SYSTEM), is_read

## Announcements — `announcements`

title, content, is_active, created_by (FK → users.id)

## Feedback — `feedback`

user_id FK, message — *(the originally planned `rating` column is **not** implemented)*

## Achievement Definitions — `achievement_definitions` (badge catalog)

| Column | Type | Notes |
|----------|------|------|
| id | BIGINT PK | |
| code | VARCHAR(50) | unique, e.g. `ATS_EXPERT` — 17 badges seeded by `data.sql` |
| category | VARCHAR(30) | enum: RESUME, INTERVIEW, JOB_TRACKER, STUDY_PLANNER, GITHUB, LEETCODE, PLACEMENT, CONSISTENCY, SPECIAL |
| title / description / icon / color | | display text, emoji icon, hex accent |
| xp_reward | INT | XP granted on unlock |
| activity_type | VARCHAR(40) | enum; NULL for cross-cutting badges (Power User) |
| target_value | INT | unlock threshold |
| sort_order | INT | catalog order |

## User Achievements — `user_achievements`

user_id FK, achievement_id FK, unlocked_at — **unique (user_id, achievement_id)** (a badge can never unlock twice)

## XP History — `xp_history`

user_id FK, amount (INT > 0), reason (enum, e.g. activity type or `ACHIEVEMENT_UNLOCKED`), description — append-only ledger; total XP = sum of entries

## Readiness Snapshots — `readiness_snapshots`

user_id FK, score, created_at — stored when the placement readiness score changes

---

# 5. Relationships (summary)

- One Role → Many Users
- One User → Many Resumes, Job Applications, Study Planners, Notifications, Feedback, Password Reset Tokens, Interview Sessions, Resume Reviews, User Achievements, XP History, Readiness Snapshots
- One Resume → Many Education / Skill / Project / Experience / Certification / Achievement records; optional Many-to-One ResumeTemplate
- One Job Application → Many Timeline Events, Interview Schedules, Interview Notes, Attachments
- One Achievement Definition → Many User Achievements

---

# 6. Database Constraints

- Email is unique.
- Password cannot be null (BCrypt-hashed).
- Every user has exactly one role.
- Every resume/section belongs to one user/resume.
- `interview_questions(category, question)` is unique.
- `user_achievements(user_id, achievement_id)` is unique (duplicate-unlock backstop).
- Foreign keys enforce referential integrity; user deletion cascades to their platform data.

---

# 7. Indexing & Performance

- Indexes on `email`, `company_name`, and status columns support the common lookups.
- The question bank uses a native `ORDER BY RAND() LIMIT n` query (no full-table load).
- Expensive reads are cached in Redis (dashboard, GitHub/LeetCode profiles, study/job lists, achievements) — see `docs/03_ARCHITECTURE.md` and `DEVLAUNCH_COMPLETE_TECHNICAL_DOCUMENTATION.md`.

---

# 8. Achievements & Gamification

The gamification tables (`achievement_definitions`, `user_achievements`, `xp_history`, `readiness_snapshots`) are created by `ddl-auto: update` and managed at runtime by `GamificationServiceImpl` / the RabbitMQ `AchievementActivityConsumer`. The badge catalog (17 definitions) is seeded idempotently in `data.sql`. See `docs/03_ARCHITECTURE.md` §15 and `DEVLAUNCH_COMPLETE_TECHNICAL_DOCUMENTATION.md` §14 for the design.

---

# 9. Deviations from the original plan

| Planned table | Actual state |
|---|---|
| `study_plans` + `study_tasks` (plan hierarchy) | Merged into a single flat `study_planners` table — no plan/task split |
| `github_profiles` (persisted per-user) | **Not persisted** — GitHub data is fetched live and Redis-cached |
| `leetcode_stats` (persisted per-user) | **Not persisted** — LeetCode data is fetched live and Redis-cached |
| `interview_history` (simple category/score/date) | Replaced by rich `interview_sessions` + snapshotted questions |
| `feedback.rating` column | Not implemented — Feedback stores a message only |
| — | Added: resume templates, timeline/interviews/notes/attachments, interview question bank, resume reviews, password reset tokens, gamification tables, readiness snapshots |

---

# 10. Conclusion

The implemented database design is normalized, seeded with reference data (interview questions, badge catalog, roles, templates), and created automatically by Hibernate. It supports all functional requirements of the DevLaunch application; a migration tool (Flyway/Liquibase) is planned as a future hardening step.

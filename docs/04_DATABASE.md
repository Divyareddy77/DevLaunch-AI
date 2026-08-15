# DevLaunch - Database Design

## Project Information

| Field | Details |
|--------|----------|
| Project Name | DevLaunch |
| Database | MySQL |
| ORM | Spring Data JPA (Hibernate) |
| Database Version | MySQL 8+ |

---

# 1. Introduction

This document defines the logical database design for the DevLaunch application.

The database stores user information, resumes, job applications, study plans, interview history, GitHub analytics, LeetCode statistics, notifications, and administrative data.

The design follows normalization principles to minimize redundancy while maintaining performance and scalability.

---

# 2. Database Overview

The application consists of the following primary entities:

- Users
- Roles
- Resumes
- Education
- Skills
- Projects
- Experience
- Certifications
- Job Applications
- Study Plans
- Study Tasks
- Interview History
- GitHub Profiles
- LeetCode Statistics
- Notifications
- Feedback
- Announcements

---

# 3. Entity Relationship Overview

```
User
 │
 ├── Resume
 │      ├── Education
 │      ├── Skill
 │      ├── Project
 │      ├── Experience
 │      └── Certification
 │
 ├── JobApplication
 │
 ├── StudyPlan
 │      └── StudyTask
 │
 ├── InterviewHistory
 │
 ├── GitHubProfile
 │
 ├── LeetCodeStats
 │
 ├── Notification
 │
 └── Feedback
```

---

# 4. Tables

## Users

Stores user information.

| Column | Type |
|----------|------|
| id | BIGINT |
| first_name | VARCHAR |
| last_name | VARCHAR |
| email | VARCHAR |
| password | VARCHAR |
| phone | VARCHAR |
| role_id | BIGINT |
| created_at | TIMESTAMP |
| updated_at | TIMESTAMP |

---

## Roles

Stores user roles.

| Column | Type |
|----------|------|
| id | BIGINT |
| role_name | VARCHAR |

Roles:

- STUDENT
- ADMIN

---

## Resume

Stores resume details.

| Column | Type |
|----------|------|
| id | BIGINT |
| user_id | BIGINT |
| title | VARCHAR |
| summary | TEXT |
| template_name | VARCHAR |

---

## Education

| Column | Type |
|----------|------|
| id | BIGINT |
| resume_id | BIGINT |
| institution | VARCHAR |
| degree | VARCHAR |
| specialization | VARCHAR |
| cgpa | DECIMAL |
| start_year | YEAR |
| end_year | YEAR |

---

## Skills

| Column | Type |
|----------|------|
| id | BIGINT |
| resume_id | BIGINT |
| skill_name | VARCHAR |
| skill_level | VARCHAR |

---

## Projects

| Column | Type |
|----------|------|
| id | BIGINT |
| resume_id | BIGINT |
| project_name | VARCHAR |
| description | TEXT |
| technologies | VARCHAR |
| github_url | VARCHAR |

---

## Experience

| Column | Type |
|----------|------|
| id | BIGINT |
| resume_id | BIGINT |
| company | VARCHAR |
| role | VARCHAR |
| duration | VARCHAR |
| description | TEXT |

---

## Certifications

| Column | Type |
|----------|------|
| id | BIGINT |
| resume_id | BIGINT |
| certificate_name | VARCHAR |
| organization | VARCHAR |
| issue_date | DATE |

---

## Job Applications

| Column | Type |
|----------|------|
| id | BIGINT |
| user_id | BIGINT |
| company_name | VARCHAR |
| job_role | VARCHAR |
| location | VARCHAR |
| application_date | DATE |
| status | VARCHAR |
| notes | TEXT |

Application Status:

- Wishlist
- Applied
- Assessment
- Interview
- Offer
- Rejected

---

## Study Plans

| Column | Type |
|----------|------|
| id | BIGINT |
| user_id | BIGINT |
| title | VARCHAR |
| start_date | DATE |
| end_date | DATE |

---

## Study Tasks

| Column | Type |
|----------|------|
| id | BIGINT |
| study_plan_id | BIGINT |
| task_name | VARCHAR |
| status | VARCHAR |

Task Status:

- Pending
- Completed

---

## Interview History

| Column | Type |
|----------|------|
| id | BIGINT |
| user_id | BIGINT |
| category | VARCHAR |
| score | INT |
| interview_date | DATE |

---

## GitHub Profile

| Column | Type |
|----------|------|
| id | BIGINT |
| user_id | BIGINT |
| username | VARCHAR |
| repositories | INT |
| followers | INT |
| following | INT |

---

## LeetCode Statistics

| Column | Type |
|----------|------|
| id | BIGINT |
| user_id | BIGINT |
| username | VARCHAR |
| easy_solved | INT |
| medium_solved | INT |
| hard_solved | INT |
| total_solved | INT |

---

## Notifications

| Column | Type |
|----------|------|
| id | BIGINT |
| user_id | BIGINT |
| title | VARCHAR |
| message | TEXT |
| is_read | BOOLEAN |
| created_at | TIMESTAMP |

---

## Feedback

| Column | Type |
|----------|------|
| id | BIGINT |
| user_id | BIGINT |
| message | TEXT |
| rating | INT |
| submitted_at | TIMESTAMP |

---

## Announcements

| Column | Type |
|----------|------|
| id | BIGINT |
| title | VARCHAR |
| description | TEXT |
| created_at | TIMESTAMP |

---

# 5. Relationships

- One Role → Many Users
- One User → Many Resumes
- One Resume → Many Education Records
- One Resume → Many Skills
- One Resume → Many Projects
- One Resume → Many Experience Records
- One Resume → Many Certifications
- One User → Many Job Applications
- One User → Many Study Plans
- One Study Plan → Many Study Tasks
- One User → Many Interview History Records
- One User → One GitHub Profile
- One User → One LeetCode Statistics
- One User → Many Notifications
- One User → Many Feedback Entries

---

# 6. Database Constraints

- Email must be unique.
- Password cannot be null.
- Every user must have one role.
- Every resume belongs to one user.
- Every study task belongs to one study plan.
- Foreign key constraints enforce referential integrity.

---

# 7. Indexing Strategy

Indexes will be created on:

- email
- company_name
- username
- application_status

to improve search performance.

---

# 8. Achievements & Gamification

The gamification module adds three tables seeded and managed by the achievement consumer. `ddl-auto: update` creates them automatically; the badge catalog is seeded idempotently in `data.sql` (`INSERT IGNORE` on the unique `code`).

## achievement_definitions

Static badge catalog shared by every user.

| Column | Type | Notes |
| --- | --- | --- |
| id | BIGINT PK | auto-increment |
| code | VARCHAR(50) | unique, e.g. `ATS_EXPERT` |
| category | VARCHAR(30) | AchievementCategory enum (RESUME, INTERVIEW, JOB_TRACKER, STUDY_PLANNER, GITHUB, LEETCODE, PLACEMENT, CONSISTENCY, SPECIAL) |
| title / description | VARCHAR / TEXT | display text |
| icon | VARCHAR(20) | emoji badge icon |
| color | VARCHAR(20) | hex accent color |
| xp_reward | INT | XP granted on unlock |
| activity_type | VARCHAR(40) | ActivityType enum; NULL for cross-cutting badges (Power User) |
| target_value | INT | unlock threshold (e.g. 90 for ATS >= 90) |
| sort_order | INT | catalog display order |

## user_achievements

Per-user unlock records. Unique constraint `uk_user_achievements_user_achievement (user_id, achievement_id)` guarantees a badge can never be unlocked twice, even under concurrent consumers or re-delivered messages.

| Column | Type | Notes |
| --- | --- | --- |
| id | BIGINT PK | auto-increment |
| user_id | BIGINT FK → users.id | |
| achievement_id | BIGINT FK → achievement_definitions.id | |
| unlocked_at | DATETIME | when the badge was unlocked |

## xp_history

Append-only XP ledger; the user's total XP is the sum of their entries (single source of truth for the level calculation).

| Column | Type | Notes |
| --- | --- | --- |
| id | BIGINT PK | auto-increment |
| user_id | BIGINT FK → users.id | |
| amount | INT | always positive; XP is never deducted |
| reason | VARCHAR(40) | XpReason enum (activity type or ACHIEVEMENT_UNLOCKED) |
| description | VARCHAR(300) | human-readable award description |

---

# 9. Conclusion

The database design provides a normalized, scalable, and maintainable structure that supports all functional requirements of the DevLaunch application.
# DevLaunch - API Contract

## Project Information

| Field | Details |
|--------|----------|
| Project | DevLaunch |
| API Style | REST |
| Data Format | JSON |
| Authentication | JWT Bearer token (stateless) |
| Interactive reference | Swagger UI at `/swagger-ui.html` (springdoc) |

> **Note:** this document lists the endpoints **as actually implemented** in `backend/src/main/java/com/devlaunch/controller/`. Auth column: **Public** = no token, **User** = any authenticated user, **Admin** = `ROLE_ADMIN` only.

---

# 1. Auth — `/api/auth` (Public)

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/auth/register` | Register a student (BCrypt-hashed; no JWT issued) |
| POST | `/api/auth/login` | Login → `{ accessToken, tokenType: "Bearer", expiresIn, message }` |
| POST | `/api/auth/forgot-password` | Request a password-reset link (async email via RabbitMQ + SMTP) |
| POST | `/api/auth/reset-password` | Reset password with a single-use token |

# 2. Users — `/api/users` (User)

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/users/me` | Current profile (includes role) |
| PUT | `/api/users/me` | Update profile |
| PUT | `/api/users/change-password` | Change password (verifies current password) |
| PUT | `/api/users/me/github` | Link GitHub username (`{username}`) |
| DELETE | `/api/users/me/github` | Unlink GitHub username |
| PUT | `/api/users/me/leetcode` | Link LeetCode username |
| DELETE | `/api/users/me/leetcode` | Unlink LeetCode username |

# 3. Dashboard — `/api/dashboard` (User)

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/dashboard` | Aggregated dashboard (resume, jobs, study, GitHub, LeetCode, interviews, readiness) |

# 4. Resumes — `/api/resumes` (User)

| Method | Endpoint | Purpose |
|---|---|---|
| POST / GET | `/api/resumes` | Create / list own resumes |
| GET / PUT / DELETE | `/api/resumes/{id}` | Detail / update / delete |
| PUT | `/api/resumes/{resumeId}/template/{templateId}` | Assign a resume template |
| GET | `/api/resumes/{resumeId}/template` | Current template |
| GET | `/api/resumes/{resumeId}/pdf` | Download PDF (`?template=` optional) |
| POST / GET | `/api/resumes/{resumeId}/educations` (also `experiences`, `projects`, `skills`, `certifications`, `achievements`) | Create / list section items |
| GET / PUT / DELETE | `/api/resumes/{resumeId}/{sections}/{itemId}` | Section item detail / update / delete |

# 5. Resume Templates — `/api/resume-templates` (User)

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/resume-templates` | List templates (4 seeded) |
| GET | `/api/resume-templates/{templateId}` | Template detail |

# 6. Job Applications — `/api/job-applications` (User)

| Method | Endpoint | Purpose |
|---|---|---|
| POST / GET | `/api/job-applications` | Create / list own applications |
| GET | `/api/job-applications/analytics` | Aggregated stats (status counts, monthly trend, timeline) |
| GET / PUT / DELETE | `/api/job-applications/{id}` | Detail / update / delete |
| PUT | `/api/job-applications/{id}/status` | Status-only update (Kanban drag & drop) |
| GET | `/api/job-applications/{id}/timeline` | Milestone timeline |
| GET / POST | `/api/job-applications/{id}/interviews` | List / schedule interviews |
| PUT / DELETE | `/api/job-applications/interviews/{interviewId}` | Update / cancel interview |
| GET / POST | `/api/job-applications/{id}/notes` | List / add interview notes |
| DELETE | `/api/job-applications/{id}/notes/{noteId}` | Delete note |
| GET / POST | `/api/job-applications/{id}/attachments` | List / upload attachments (multipart) |
| GET | `/api/job-applications/{id}/attachments/{attachmentId}/download` | Download attachment |
| DELETE | `/api/job-applications/{id}/attachments/{attachmentId}` | Delete attachment |

# 7. Study Planner — `/api/study-planners` (User)

| Method | Endpoint | Purpose |
|---|---|---|
| POST / GET | `/api/study-planners` | Create / list study tasks |
| GET / PUT / DELETE | `/api/study-planners/{id}` | Detail / update (status → COMPLETED triggers streaks) / delete |

# 8. GitHub — `/api/github` (User)

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/github/{username}` | Live GitHub profile |
| GET | `/api/github/{username}/repositories` | Live repository list |
| GET | `/api/github/{username}/languages` | Language counts (computed from repos) |

# 9. LeetCode — `/api/leetcode` (User)

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/leetcode/{username}` | Live solved counts / difficulty breakdown / ranking |

# 10. AI — `/api/ai` (User)

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/ai/resume-review` | AI resume review / ATS analysis (`{resumeId, targetRole?}`) |
| POST | `/api/ai/mock-interview/questions` | Start interview → questions + `sessionId` |
| POST | `/api/ai/mock-interview/feedback` | Submit answers → scored feedback + report |
| GET | `/api/ai/mock-interview/history` | Interview history + analytics |
| GET | `/api/ai/mock-interview/categories` | Per-category bank size / attempts / best |
| DELETE | `/api/ai/mock-interview/history/{sessionId}` | Delete a session |
| POST | `/api/ai/transcribe` | Whisper speech-to-text (multipart audio) |

# 11. Achievements / Gamification — `/api/achievements` (User, Redis-cached)

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/achievements` | Badge catalog (17 definitions) |
| GET | `/api/achievements/user` | Unlocked badges, newest first |
| GET | `/api/achievements/summary` | Level, XP, progress, recent unlocks |
| GET | `/api/achievements/history` | XP ledger (top 50) |
| GET | `/api/achievements/progress` | Per-badge progress |

# 12. Notifications — `/api/notifications` (User)

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/notifications` | List (newest first) |
| GET | `/api/notifications/unread-count` | Unread count (cached) |
| PATCH | `/api/notifications/{id}/read` | Mark one read |
| PUT | `/api/notifications/read-all` | Mark all read |
| DELETE | `/api/notifications/{id}` | Delete |

# 13. Announcements & Feedback

| Method | Endpoint | Auth | Purpose |
|---|---|---|---|
| GET | `/api/announcements/active` | User | Active announcements |
| POST | `/api/feedback` | User | Submit feedback |

# 14. Admin — `/api/admin` (Admin)

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/admin/dashboard` | Platform stats + recent activity |
| GET | `/api/admin/users?search=&role=&active=&page=&size=` | Paged user list |
| GET | `/api/admin/users/{id}` | User detail |
| PUT | `/api/admin/users/{id}/status?active=` | Activate / deactivate |
| DELETE | `/api/admin/users/{id}` | Delete user |
| GET / DELETE | `/api/admin/resumes` (`/{id}`) | Resume moderation |
| GET / DELETE | `/api/admin/job-applications` (+ `/stats`) | Job application moderation |
| GET / DELETE | `/api/admin/study-plans` (`/{id}`) | Study plan moderation |
| GET | `/api/admin/ai/resume-reviews` | AI review history |
| GET / DELETE | `/api/admin/ai/interviews` (`/{id}`) | Mock interview history |
| GET / POST | `/api/admin/announcements` | List / create (fan-out to all users) |
| PUT / DELETE | `/api/admin/announcements/{id}` | Update / delete |
| GET / DELETE | `/api/admin/feedback` (`/{id}`) | Feedback moderation |

# 15. Misc

| Method | Endpoint | Auth | Purpose |
|---|---|---|---|
| GET | `/api/test` | User | Demo endpoint — `"JWT Authentication Successful!"` |

---

# 16. Response Formats

**Success** — module responses are plain DTOs (no wrapper). Examples:

`POST /api/auth/register` → `201 Created` with `UserResponse { id, firstName, lastName, email, phone, role, … }`

`POST /api/auth/login` → `200 OK`:

```json
{
  "accessToken": "JWT_TOKEN",
  "tokenType": "Bearer",
  "expiresIn": 86400000,
  "message": "Login successful"
}
```

**Error** — `GlobalExceptionHandler` returns:

```json
{
  "timestamp": "2026-01-01T00:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "human-readable message",
  "path": "/api/..."
}
```

---

# 17. HTTP Status Codes

| Code | Meaning |
|---|---|
| 200 OK | Success |
| 201 Created | Resource created |
| 400 Bad Request | Validation failure / bad argument / invalid or expired reset token |
| 401 Unauthorized | Bad credentials / missing or invalid JWT |
| 403 Forbidden | Authenticated but role insufficient |
| 404 Not Found | Resource or owner mismatch (also GitHub/LeetCode user not found) |
| 409 Conflict | Duplicate email |
| 502 Bad Gateway | Speech-to-text transcription failure |
| 500 Internal Server Error | Unexpected failure (generic message) |

---

# 18. Authentication

Protected APIs require:

```
Authorization: Bearer JWT_TOKEN
```

The full endpoint list is also documented with request/response details in `DEVLAUNCH_COMPLETE_TECHNICAL_DOCUMENTATION.md` (§24).

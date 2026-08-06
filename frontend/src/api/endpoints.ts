/**
 * Centralised API endpoint URL constants.
 *
 * Every backend REST endpoint is defined here to avoid hardcoded
 * URL strings in service modules. Organised by domain module to
 * mirror the backend controller structure.
 *
 * @see backend/src/main/java/com/devlaunch/controller/
 * @author DevLaunch
 */

export const AUTH = {
  LOGIN: '/api/auth/login',
  REGISTER: '/api/auth/register',
  /** POST — Request a password reset link for an email address. */
  FORGOT_PASSWORD: '/api/auth/forgot-password',
  /** POST — Complete a password reset with a one-time token. */
  RESET_PASSWORD: '/api/auth/reset-password',
} as const;

export const USERS = {
  ME: '/api/users/me',
  CHANGE_PASSWORD: '/api/users/change-password',
  /** PUT/DELETE — Link/unlink a GitHub username (body: { username }). */
  GITHUB_CONNECT: '/api/users/me/github',
  /** PUT/DELETE — Link/unlink a LeetCode username (body: { username }). */
  LEETCODE_CONNECT: '/api/users/me/leetcode',
} as const;

export const DASHBOARD = '/api/dashboard';

export const RESUMES = {
  BASE: '/api/resumes',
  BY_ID: (id: number) => `/api/resumes/${id}`,
  TEMPLATE: (resumeId: number) => `/api/resumes/${resumeId}/template`,
  ASSIGN_TEMPLATE: (resumeId: number, templateId: number) =>
    `/api/resumes/${resumeId}/template/${templateId}`,
  PDF: (resumeId: number) => `/api/resumes/${resumeId}/pdf`,
} as const;

export const RESUME_TEMPLATES = {
  BASE: '/api/resume-templates',
  BY_ID: (id: number) => `/api/resume-templates/${id}`,
} as const;

export const RESUME_SUB_RESOURCES = {
  EDUCATION: (resumeId: number) => `/api/resumes/${resumeId}/educations`,
  EDUCATION_BY_ID: (resumeId: number, eduId: number) =>
    `/api/resumes/${resumeId}/educations/${eduId}`,
  EXPERIENCE: (resumeId: number) => `/api/resumes/${resumeId}/experiences`,
  EXPERIENCE_BY_ID: (resumeId: number, expId: number) =>
    `/api/resumes/${resumeId}/experiences/${expId}`,
  SKILL: (resumeId: number) => `/api/resumes/${resumeId}/skills`,
  SKILL_BY_ID: (resumeId: number, skillId: number) =>
    `/api/resumes/${resumeId}/skills/${skillId}`,
  ACHIEVEMENT: (resumeId: number) => `/api/resumes/${resumeId}/achievements`,
  ACHIEVEMENT_BY_ID: (resumeId: number, achievementId: number) =>
    `/api/resumes/${resumeId}/achievements/${achievementId}`,
  CERTIFICATION: (resumeId: number) => `/api/resumes/${resumeId}/certifications`,
  CERTIFICATION_BY_ID: (resumeId: number, certId: number) =>
    `/api/resumes/${resumeId}/certifications/${certId}`,
  PROJECT: (resumeId: number) => `/api/resumes/${resumeId}/projects`,
  PROJECT_BY_ID: (resumeId: number, projectId: number) =>
    `/api/resumes/${resumeId}/projects/${projectId}`,
} as const;

export const JOB_APPLICATIONS = {
  BASE: '/api/job-applications',
  BY_ID: (id: number) => `/api/job-applications/${id}`,
  /** PUT — Update only the status (Kanban drag-and-drop). */
  STATUS: (id: number) => `/api/job-applications/${id}/status`,
  /** GET — Aggregated application analytics. */
  ANALYTICS: '/api/job-applications/analytics',
  /** GET — Application milestone timeline. */
  TIMELINE: (id: number) => `/api/job-applications/${id}/timeline`,
  /** GET/POST — Interview schedules for an application. */
  INTERVIEWS: (id: number) => `/api/job-applications/${id}/interviews`,
  /** PUT/DELETE — Update or cancel a single interview. */
  INTERVIEW_BY_ID: (interviewId: number) =>
    `/api/job-applications/interviews/${interviewId}`,
  /** GET/POST — Private interview notes. */
  NOTES: (id: number) => `/api/job-applications/${id}/notes`,
  /** DELETE — Remove a single interview note. */
  NOTE_BY_ID: (id: number, noteId: number) =>
    `/api/job-applications/${id}/notes/${noteId}`,
  /** GET/POST — Attachments for an application. */
  ATTACHMENTS: (id: number) => `/api/job-applications/${id}/attachments`,
  /** GET — Download a single attachment. */
  ATTACHMENT_DOWNLOAD: (id: number, attachmentId: number) =>
    `/api/job-applications/${id}/attachments/${attachmentId}/download`,
  /** DELETE — Remove a single attachment. */
  ATTACHMENT_BY_ID: (id: number, attachmentId: number) =>
    `/api/job-applications/${id}/attachments/${attachmentId}`,
} as const;

export const STUDY_PLANNERS = {
  BASE: '/api/study-planners',
  BY_ID: (id: number) => `/api/study-planners/${id}`,
} as const;

export const GITHUB = {
  PROFILE: (username: string) => `/api/github/${username}`,
  REPOSITORIES: (username: string) => `/api/github/${username}/repositories`,
  LANGUAGES: (username: string) => `/api/github/${username}/languages`,
} as const;

export const LEETCODE = {
  PROFILE: (username: string) => `/api/leetcode/${username}`,
} as const;

/**
 * AI module endpoint constants.
 *
 * These endpoints back the Mock Interview and Resume Review features
 * exposed by the backend AiController under the /api/ai prefix.
 */
export const AI = {
  /** POST — Start a new mock interview session. */
  START_INTERVIEW: '/api/ai/mock-interview/questions',
  /** POST — Submit answers for an interview session and receive feedback. */
  SUBMIT_INTERVIEW: '/api/ai/mock-interview/feedback',
  /** GET — Retrieve the authenticated user's interview history. */
  INTERVIEW_HISTORY: '/api/ai/mock-interview/history',
  /** GET — Per-category statistics for the landing page. */
  INTERVIEW_CATEGORIES: '/api/ai/mock-interview/categories',
  /** DELETE — Remove a single interview session from the history. */
  INTERVIEW_HISTORY_BY_SESSION: (sessionId: string) =>
    `/api/ai/mock-interview/history/${encodeURIComponent(sessionId)}`,
  /** POST — Submit a resume for AI-powered review. */
  REVIEW_RESUME: '/api/ai/resume-review',
  /** POST — Transcribe a recorded voice answer (multipart/form-data). */
  TRANSCRIBE: '/api/ai/transcribe',
} as const;

/**
 * Admin module endpoint constants.
 *
 * Every endpoint is protected by the ROLE_ADMIN authority on the backend;
 * normal users receive 403 Forbidden.
 *
 * @see backend/src/main/java/com/devlaunch/controller/AdminController.java
 */
export const ADMIN = {
  /** GET — Platform-wide statistics and recent activity. */
  DASHBOARD: '/api/admin/dashboard',
  /** GET — Paged, searchable, filterable user list. */
  USERS: '/api/admin/users',
  /** GET — Single user details. */
  USER_BY_ID: (id: number) => `/api/admin/users/${id}`,
  /** PUT — Activate/deactivate a user (?active=true|false). */
  USER_STATUS: (id: number) => `/api/admin/users/${id}/status`,
  /** GET/POST — Resume management. */
  RESUMES: '/api/admin/resumes',
  /** GET/DELETE — Single resume. */
  RESUME_BY_ID: (id: number) => `/api/admin/resumes/${id}`,
  /** GET — Paged job application list. */
  JOB_APPLICATIONS: '/api/admin/job-applications',
  /** GET — Job applications per status. */
  JOB_APPLICATION_STATS: '/api/admin/job-applications/stats',
  /** DELETE — Single job application. */
  JOB_APPLICATION_BY_ID: (id: number) => `/api/admin/job-applications/${id}`,
  /** GET — Paged study plan list. */
  STUDY_PLANS: '/api/admin/study-plans',
  /** DELETE — Single study plan. */
  STUDY_PLAN_BY_ID: (id: number) => `/api/admin/study-plans/${id}`,
  /** GET — Paged AI resume review history. */
  AI_RESUME_REVIEWS: '/api/admin/ai/resume-reviews',
  /** GET — Paged mock interview history. */
  AI_INTERVIEWS: '/api/admin/ai/interviews',
  /** DELETE — Single interview session. */
  AI_INTERVIEW_BY_ID: (id: number) => `/api/admin/ai/interviews/${id}`,
  /** GET/POST — Announcements. */
  ANNOUNCEMENTS: '/api/admin/announcements',
  /** PUT/DELETE — Single announcement. */
  ANNOUNCEMENT_BY_ID: (id: number) => `/api/admin/announcements/${id}`,
  /** GET — Paged feedback list. */
  FEEDBACK: '/api/admin/feedback',
  /** DELETE — Single feedback entry. */
  FEEDBACK_BY_ID: (id: number) => `/api/admin/feedback/${id}`,
} as const;

/**
 * Public feedback submission endpoint (any authenticated user).
 *
 * @see backend/src/main/java/com/devlaunch/controller/FeedbackController.java
 */
export const FEEDBACK = {
  /** POST — Submit platform feedback. */
  SUBMIT: '/api/feedback',
} as const;

/**
 * User-facing announcement endpoints (any authenticated user).
 *
 * @see backend/src/main/java/com/devlaunch/controller/AnnouncementController.java
 */
export const ANNOUNCEMENTS = {
  /** GET — Active announcements, newest first. */
  ACTIVE: '/api/announcements/active',
} as const;

/**
 * Notification center endpoints (any authenticated user).
 *
 * Every endpoint operates exclusively on the authenticated user's own
 * notifications.
 *
 * @see backend/src/main/java/com/devlaunch/controller/NotificationController.java
 */
export const NOTIFICATIONS = {
  /** GET — All notifications for the authenticated user, newest first. */
  BASE: '/api/notifications',
  /** GET — Unread notification count for the authenticated user. */
  UNREAD_COUNT: '/api/notifications/unread-count',
  /** PUT — Mark a single notification as read. */
  MARK_READ: (id: number) => `/api/notifications/${id}/read`,
  /** PUT — Mark all notifications as read. */
  READ_ALL: '/api/notifications/read-all',
  /** DELETE — Remove a single notification. */
  BY_ID: (id: number) => `/api/notifications/${id}`,
} as const;

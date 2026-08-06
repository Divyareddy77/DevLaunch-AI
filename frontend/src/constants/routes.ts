/**
 * Centralised route path constants.
 *
 * Every route path used by React Router is defined here to avoid
 * hardcoded strings scattered across the codebase. Use these
 * constants in route definitions, navigation links, and redirects.
 *
 * @author DevLaunch
 */

export const ROUTES = {
  // Public auth routes
  LOGIN: '/auth/login',
  REGISTER: '/auth/register',
  FORGOT_PASSWORD: '/auth/forgot-password',
  RESET_PASSWORD: '/auth/reset-password',

  // Protected application routes
  DASHBOARD: '/dashboard',
  RESUME_LIST: '/resumes',
  RESUME_CREATE: '/resumes/new',
  RESUME_DETAIL: (id: number | string) => `/resumes/${id}`,
  RESUME_EDIT: (id: number | string) => `/resumes/${id}/edit`,
  JOB_APPLICATION_LIST: '/job-applications',
  JOB_APPLICATION_CREATE: '/job-applications/new',
  JOB_APPLICATION_EDIT: (id: number | string) => `/job-applications/${id}/edit`,
  STUDY_PLANNER_LIST: '/study-planner',
  STUDY_PLANNER_CREATE: '/study-planner/new',
  STUDY_PLANNER_EDIT: (id: number | string) => `/study-planner/${id}/edit`,
  GITHUB_ANALYTICS: '/github',
  LEETCODE_TRACKER: '/leetcode',
  PROFILE: '/profile',
  NOTIFICATIONS: '/notifications',

  // AI module routes (future)
  MOCK_INTERVIEW: '/ai/mock-interview',
  RESUME_REVIEW: '/ai/resume-review',

  // Admin routes (ROLE_ADMIN only)
  ADMIN: '/admin',
  ADMIN_USERS: '/admin/users',
  ADMIN_RESUMES: '/admin/resumes',
  ADMIN_JOB_APPLICATIONS: '/admin/job-applications',
  ADMIN_STUDY_PLANS: '/admin/study-plans',
  ADMIN_REPORTS: '/admin/ai',
  ADMIN_ANNOUNCEMENTS: '/admin/announcements',
  ADMIN_FEEDBACK: '/admin/feedback',

  // Fallback
  NOT_FOUND: '/404',
} as const;

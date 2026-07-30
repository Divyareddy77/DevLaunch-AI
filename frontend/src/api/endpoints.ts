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
} as const;

export const USERS = {
  ME: '/api/users/me',
  CHANGE_PASSWORD: '/api/users/change-password',
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
 * These endpoints are prepared for future implementation. They
 * correspond to the Mock Interview and Resume Review features
 * described in the documentation.
 */
export const AI = {
  /** POST — Start a new mock interview session. */
  START_INTERVIEW: '/api/interviews/start',
  /** POST — Submit answers for an interview session and receive feedback. */
  SUBMIT_INTERVIEW: '/api/interviews/submit',
  /** GET — Retrieve the authenticated user's interview history. */
  INTERVIEW_HISTORY: '/api/interviews/history',
  /** POST — Submit a resume for AI-powered review. */
  REVIEW_RESUME: '/api/ai/resume-review',
} as const;

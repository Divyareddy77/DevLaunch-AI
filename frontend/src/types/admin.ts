/**
 * Type definitions for the Admin module.
 *
 * Mirrors the backend DTOs in com.devlaunch.dto.response.*
 * and com.devlaunch.dto.request.* for the admin API.
 *
 * @see backend/src/main/java/com/devlaunch/controller/AdminController.java
 * @author DevLaunch
 */

// ────────────────────────────────────────────────────
// Generic paged response
// ────────────────────────────────────────────────────

/** Paginated response envelope returned by admin list endpoints. */
export interface PagedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

// ────────────────────────────────────────────────────
// Dashboard
// ────────────────────────────────────────────────────

/** Platform-wide statistics for the admin dashboard. */
export interface AdminDashboardResponse {
  totalUsers: number;
  activeUsers: number;
  inactiveUsers: number;
  totalResumes: number;
  totalJobApplications: number;
  totalStudyPlans: number;
  totalInterviewSessions: number;
  totalResumeReviews: number;
  recentRegistrations: AdminUserResponse[];
  recentInterviews: AdminInterviewSessionResponse[];
}

// ────────────────────────────────────────────────────
// Users
// ────────────────────────────────────────────────────

/** A user as seen by an administrator. */
export interface AdminUserResponse {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  role: string;
  isActive: boolean;
  createdAt: string;
}

// ────────────────────────────────────────────────────
// Resumes
// ────────────────────────────────────────────────────

/** A resume as seen by an administrator. */
export interface AdminResumeResponse {
  id: number;
  headline: string;
  summary: string;
  linkedinUrl: string | null;
  githubUrl: string | null;
  portfolioUrl: string | null;
  userId: number;
  userEmail: string;
  userName: string;
  createdAt: string;
  updatedAt: string;
}

// ────────────────────────────────────────────────────
// Job applications
// ────────────────────────────────────────────────────

/** A job application as seen by an administrator. */
export interface AdminJobApplicationResponse {
  id: number;
  companyName: string;
  jobRole: string;
  companyLocation: string | null;
  jobType: string | null;
  salary: string | null;
  status: string;
  applicationDate: string | null;
  userId: number;
  userEmail: string;
  userName: string;
  createdAt: string;
}

/** Counts of job applications per status. */
export type AdminJobApplicationStats = Record<string, number>;

// ────────────────────────────────────────────────────
// Study plans
// ────────────────────────────────────────────────────

/** A study plan entry as seen by an administrator. */
export interface AdminStudyPlannerResponse {
  id: number;
  title: string;
  description: string | null;
  studyDate: string;
  startTime: string | null;
  endTime: string | null;
  priority: string;
  status: string;
  userId: number;
  userEmail: string;
  userName: string;
  createdAt: string;
}

// ────────────────────────────────────────────────────
// AI module monitoring
// ────────────────────────────────────────────────────

/** An AI resume review history record as seen by an administrator. */
export interface AdminResumeReviewResponse {
  id: number;
  resumeId: number;
  resumeTitle: string;
  targetRole: string | null;
  resumeScore: number;
  atsScore: number;
  userId: number;
  userEmail: string;
  userName: string;
  createdAt: string;
}

/** A mock interview session as seen by an administrator. */
export interface AdminInterviewSessionResponse {
  id: number;
  sessionId: string;
  interviewType: string;
  overallScore: number;
  questionCount: number;
  completedAt: string;
  userId: number;
  userEmail: string;
  userName: string;
}

// ────────────────────────────────────────────────────
// Announcements
// ────────────────────────────────────────────────────

/** An announcement as seen by an administrator. */
export interface AdminAnnouncementResponse {
  id: number;
  title: string;
  content: string;
  isActive: boolean;
  createdById: number;
  createdByEmail: string;
  createdByName: string;
  createdAt: string;
  updatedAt: string;
}

/** Payload for creating or updating an announcement. */
export interface AnnouncementRequest {
  title: string;
  content: string;
  isActive: boolean;
}

// ────────────────────────────────────────────────────
// Feedback
// ────────────────────────────────────────────────────

/** A user feedback entry as seen by an administrator. */
export interface AdminFeedbackResponse {
  id: number;
  message: string;
  userId: number;
  userEmail: string;
  userName: string;
  createdAt: string;
}

/** Payload for submitting platform feedback. */
export interface FeedbackSubmissionRequest {
  message: string;
}

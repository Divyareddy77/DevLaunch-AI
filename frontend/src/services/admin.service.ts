/**
 * Admin service.
 *
 * Provides methods for every backend admin endpoint. All requests carry
 * the JWT automatically; the backend rejects non-ADMIN callers with
 * HTTP 403 Forbidden.
 *
 * @see backend/src/main/java/com/devlaunch/controller/AdminController.java
 * @author DevLaunch
 */

import apiClient from '../api/client';
import { ADMIN } from '../api/endpoints';
import type {
  AdminAnnouncementResponse,
  AdminDashboardResponse,
  AdminFeedbackResponse,
  AdminInterviewSessionResponse,
  AdminJobApplicationResponse,
  AdminJobApplicationStats,
  AdminResumeResponse,
  AdminResumeReviewResponse,
  AdminStudyPlannerResponse,
  AdminUserResponse,
  AnnouncementRequest,
  PagedResponse,
} from '../types/admin';

/** Query parameters shared by paged admin list endpoints. */
interface PageParams {
  page: number;
  size: number;
}

export const adminService = {
  /**
   * Retrieves platform-wide statistics and recent activity.
   *
   * GET /api/admin/dashboard
   */
  getDashboard: () =>
    apiClient.get<AdminDashboardResponse>(ADMIN.DASHBOARD).then((res) => res.data),

  // ─── Users ────────────────────────────────────────────────────────────────

  /**
   * Returns a page of users with optional search, role, and status filters.
   *
   * GET /api/admin/users?search=&role=&active=&page=&size=
   */
  getUsers: (params: PageParams & { search?: string; role?: string; active?: boolean }) =>
    apiClient.get<PagedResponse<AdminUserResponse>>(ADMIN.USERS, { params }).then((res) => res.data),

  /**
   * Returns a single user's details.
   *
   * GET /api/admin/users/{id}
   */
  getUser: (id: number) =>
    apiClient.get<AdminUserResponse>(ADMIN.USER_BY_ID(id)).then((res) => res.data),

  /**
   * Activates or deactivates a user account.
   *
   * PUT /api/admin/users/{id}/status?active=true
   */
  setUserActive: (id: number, active: boolean) =>
    apiClient
      .put<AdminUserResponse>(ADMIN.USER_STATUS(id), null, { params: { active } })
      .then((res) => res.data),

  /**
   * Permanently deletes a user and all of their platform data.
   *
   * DELETE /api/admin/users/{id}
   */
  deleteUser: (id: number) => apiClient.delete(ADMIN.USER_BY_ID(id)),

  // ─── Resumes ───────────────────────────────────────────────────────────────

  /** GET /api/admin/resumes?page=&size= */
  getResumes: (params: PageParams) =>
    apiClient.get<PagedResponse<AdminResumeResponse>>(ADMIN.RESUMES, { params }).then((res) => res.data),

  /** GET /api/admin/resumes/{id} */
  getResume: (id: number) =>
    apiClient.get<AdminResumeResponse>(ADMIN.RESUME_BY_ID(id)).then((res) => res.data),

  /** DELETE /api/admin/resumes/{id} */
  deleteResume: (id: number) => apiClient.delete(ADMIN.RESUME_BY_ID(id)),

  // ─── Job applications ──────────────────────────────────────────────────────

  /** GET /api/admin/job-applications?page=&size= */
  getJobApplications: (params: PageParams) =>
    apiClient
      .get<PagedResponse<AdminJobApplicationResponse>>(ADMIN.JOB_APPLICATIONS, { params })
      .then((res) => res.data),

  /** GET /api/admin/job-applications/stats */
  getJobApplicationStats: () =>
    apiClient.get<AdminJobApplicationStats>(ADMIN.JOB_APPLICATION_STATS).then((res) => res.data),

  /** DELETE /api/admin/job-applications/{id} */
  deleteJobApplication: (id: number) => apiClient.delete(ADMIN.JOB_APPLICATION_BY_ID(id)),

  // ─── Study plans ───────────────────────────────────────────────────────────

  /** GET /api/admin/study-plans?page=&size= */
  getStudyPlans: (params: PageParams) =>
    apiClient.get<PagedResponse<AdminStudyPlannerResponse>>(ADMIN.STUDY_PLANS, { params }).then((res) => res.data),

  /** DELETE /api/admin/study-plans/{id} */
  deleteStudyPlan: (id: number) => apiClient.delete(ADMIN.STUDY_PLAN_BY_ID(id)),

  // ─── AI module monitoring ──────────────────────────────────────────────────

  /** GET /api/admin/ai/resume-reviews?page=&size= */
  getResumeReviews: (params: PageParams) =>
    apiClient
      .get<PagedResponse<AdminResumeReviewResponse>>(ADMIN.AI_RESUME_REVIEWS, { params })
      .then((res) => res.data),

  /** GET /api/admin/ai/interviews?page=&size= */
  getInterviewSessions: (params: PageParams) =>
    apiClient
      .get<PagedResponse<AdminInterviewSessionResponse>>(ADMIN.AI_INTERVIEWS, { params })
      .then((res) => res.data),

  /** DELETE /api/admin/ai/interviews/{id} */
  deleteInterviewSession: (id: number) => apiClient.delete(ADMIN.AI_INTERVIEW_BY_ID(id)),

  // ─── Announcements ─────────────────────────────────────────────────────────

  /** GET /api/admin/announcements */
  getAnnouncements: () =>
    apiClient.get<AdminAnnouncementResponse[]>(ADMIN.ANNOUNCEMENTS).then((res) => res.data),

  /** POST /api/admin/announcements */
  createAnnouncement: (data: AnnouncementRequest) =>
    apiClient.post<AdminAnnouncementResponse>(ADMIN.ANNOUNCEMENTS, data).then((res) => res.data),

  /** PUT /api/admin/announcements/{id} */
  updateAnnouncement: (id: number, data: AnnouncementRequest) =>
    apiClient
      .put<AdminAnnouncementResponse>(ADMIN.ANNOUNCEMENT_BY_ID(id), data)
      .then((res) => res.data),

  /** DELETE /api/admin/announcements/{id} */
  deleteAnnouncement: (id: number) => apiClient.delete(ADMIN.ANNOUNCEMENT_BY_ID(id)),

  // ─── Feedback ──────────────────────────────────────────────────────────────

  /** GET /api/admin/feedback?page=&size= */
  getFeedback: (params: PageParams) =>
    apiClient.get<PagedResponse<AdminFeedbackResponse>>(ADMIN.FEEDBACK, { params }).then((res) => res.data),

  /** DELETE /api/admin/feedback/{id} */
  deleteFeedback: (id: number) => apiClient.delete(ADMIN.FEEDBACK_BY_ID(id)),
};

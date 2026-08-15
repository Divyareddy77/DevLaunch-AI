/**
 * Job Application service — CRUD operations for job application tracking,
 * plus the placement management features: quick status moves (Kanban),
 * timeline, interview scheduling, private notes, attachments, and analytics.
 *
 * Communicates with the backend JobApplicationController.
 *
 * @see backend/src/main/java/com/devlaunch/controller/JobApplicationController.java
 * @author DevLaunch
 */

import apiClient from '../api/client';
import { JOB_APPLICATIONS } from '../api/endpoints';
import type {
  JobApplicationResponse,
  CreateJobApplicationRequest,
  UpdateJobApplicationRequest,
  UpdateApplicationStatusRequest,
  ScheduleInterviewRequest,
  UpdateInterviewScheduleRequest,
  ApplicationStatusEnum,
  TimelineEvent,
  InterviewSchedule,
  InterviewNote,
  ApplicationAttachment,
  AttachmentCategory,
  ApplicationAnalytics,
} from '../types/job-application';

export const jobApplicationService = {
  /** POST /api/job-applications — Create a new job application. */
  create: (data: CreateJobApplicationRequest) =>
    apiClient.post<JobApplicationResponse>(JOB_APPLICATIONS.BASE, data).then((r) => r.data),

  /** GET /api/job-applications — Get all job applications for the authenticated user. */
  getAll: () =>
    apiClient.get<JobApplicationResponse[]>(JOB_APPLICATIONS.BASE).then((r) => r.data),

  /** GET /api/job-applications/{id} — Get a single job application by ID. */
  getById: (id: number) =>
    apiClient.get<JobApplicationResponse>(JOB_APPLICATIONS.BY_ID(id)).then((r) => r.data),

  /** PUT /api/job-applications/{id} — Update a job application. */
  update: (id: number, data: UpdateJobApplicationRequest) =>
    apiClient.put<JobApplicationResponse>(JOB_APPLICATIONS.BY_ID(id), data).then((r) => r.data),

  /** PUT /api/job-applications/{id}/status — Update only the status (Kanban drag). */
  updateStatus: (id: number, status: ApplicationStatusEnum) =>
    apiClient
      .put<JobApplicationResponse>(JOB_APPLICATIONS.STATUS(id), {
        status,
      } satisfies UpdateApplicationStatusRequest)
      .then((r) => r.data),

  /** DELETE /api/job-applications/{id} — Delete a job application. */
  delete: (id: number) =>
    apiClient.delete<string>(JOB_APPLICATIONS.BY_ID(id)).then((r) => r.data),

  /** GET /api/job-applications/analytics — Aggregated application analytics. */
  getAnalytics: () =>
    apiClient.get<ApplicationAnalytics>(JOB_APPLICATIONS.ANALYTICS).then((r) => r.data),

  /** GET /api/job-applications/{id}/timeline — Application milestone timeline. */
  getTimeline: (id: number) =>
    apiClient.get<TimelineEvent[]>(JOB_APPLICATIONS.TIMELINE(id)).then((r) => r.data),

  /** GET /api/job-applications/{id}/interviews — Interview schedules. */
  getInterviews: (id: number) =>
    apiClient.get<InterviewSchedule[]>(JOB_APPLICATIONS.INTERVIEWS(id)).then((r) => r.data),

  /** POST /api/job-applications/{id}/interviews — Schedule an interview. */
  scheduleInterview: (id: number, data: ScheduleInterviewRequest) =>
    apiClient
      .post<InterviewSchedule>(JOB_APPLICATIONS.INTERVIEWS(id), data)
      .then((r) => r.data),

  /** PUT /api/job-applications/interviews/{interviewId} — Update an interview. */
  updateInterview: (interviewId: number, data: UpdateInterviewScheduleRequest) =>
    apiClient
      .put<InterviewSchedule>(JOB_APPLICATIONS.INTERVIEW_BY_ID(interviewId), data)
      .then((r) => r.data),

  /** DELETE /api/job-applications/interviews/{interviewId} — Cancel an interview. */
  cancelInterview: (interviewId: number) =>
    apiClient
      .delete<InterviewSchedule>(JOB_APPLICATIONS.INTERVIEW_BY_ID(interviewId))
      .then((r) => r.data),

  /** GET /api/job-applications/{id}/notes — Private interview notes. */
  getNotes: (id: number) =>
    apiClient.get<InterviewNote[]>(JOB_APPLICATIONS.NOTES(id)).then((r) => r.data),

  /** POST /api/job-applications/{id}/notes — Add a private interview note. */
  addNote: (id: number, content: string) =>
    apiClient
      .post<InterviewNote>(JOB_APPLICATIONS.NOTES(id), { content })
      .then((r) => r.data),

  /** DELETE /api/job-applications/{id}/notes/{noteId} — Delete a note. */
  deleteNote: (id: number, noteId: number) =>
    apiClient.delete<string>(JOB_APPLICATIONS.NOTE_BY_ID(id, noteId)).then((r) => r.data),

  /** GET /api/job-applications/{id}/attachments — Attached documents. */
  getAttachments: (id: number) =>
    apiClient
      .get<ApplicationAttachment[]>(JOB_APPLICATIONS.ATTACHMENTS(id))
      .then((r) => r.data),

  /** POST /api/job-applications/{id}/attachments — Upload an attachment. */
  uploadAttachment: (id: number, file: File, category: AttachmentCategory) => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('category', category);
    return apiClient
      .post<ApplicationAttachment>(JOB_APPLICATIONS.ATTACHMENTS(id), formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
        timeout: 60_000,
      })
      .then((r) => r.data);
  },

  /** DELETE /api/job-applications/{id}/attachments/{attachmentId} — Delete an attachment. */
  deleteAttachment: (id: number, attachmentId: number) =>
    apiClient
      .delete<string>(JOB_APPLICATIONS.ATTACHMENT_BY_ID(id, attachmentId))
      .then((r) => r.data),

  /**
   * GET /api/job-applications/{id}/attachments/{attachmentId}/download —
   * Downloads an attachment as a blob and triggers a browser save.
   */
  downloadAttachment: async (id: number, attachmentId: number, fileName: string) => {
    const response = await apiClient.get<Blob>(
      JOB_APPLICATIONS.ATTACHMENT_DOWNLOAD(id, attachmentId),
      { responseType: 'blob' },
    );
    const blobUrl = window.URL.createObjectURL(response.data);
    const link = document.createElement('a');
    link.href = blobUrl;
    link.download = fileName;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(blobUrl);
  },
};

/**
 * Type definitions for the Job Application Tracker module.
 *
 * Mirrors the backend DTOs in
 * com.devlaunch.dto.request.*,
 * com.devlaunch.dto.response.*, and
 * com.devlaunch.entity.enums.ApplicationStatus.
 *
 * @author DevLaunch
 */

/** Possible statuses for a job application within the hiring pipeline. */
export type ApplicationStatusEnum =
  | 'WISHLIST'
  | 'APPLIED'
  | 'ASSESSMENT'
  | 'INTERVIEW'
  | 'OFFER'
  | 'REJECTED';

/** Human-readable label for each status. */
export const APPLICATION_STATUS_LABELS: Record<ApplicationStatusEnum, string> = {
  WISHLIST: 'Wishlist',
  APPLIED: 'Applied',
  ASSESSMENT: 'Assessment',
  INTERVIEW: 'Interview',
  OFFER: 'Offer',
  REJECTED: 'Rejected',
};

/** All application status values as an array. */
export const APPLICATION_STATUSES: ApplicationStatusEnum[] = [
  'WISHLIST',
  'APPLIED',
  'ASSESSMENT',
  'INTERVIEW',
  'OFFER',
  'REJECTED',
];

// ────────────────────────────────────────────────────
// Response DTO
// ────────────────────────────────────────────────────

/** Job application response returned from GET/POST/PUT /api/job-applications. */
export interface JobApplicationResponse {
  id: number;
  companyName: string;
  jobRole: string;
  companyLocation: string | null;
  jobType: string | null;
  salary: string | null;
  applicationDate: string | null;
  status: ApplicationStatusEnum;
  jobUrl: string | null;
  notes: string | null;
  resumeId: number | null;
}

// ────────────────────────────────────────────────────
// Request DTOs
// ────────────────────────────────────────────────────

/** Payload for creating a job application (POST /api/job-applications). */
export interface CreateJobApplicationRequest {
  companyName: string;
  jobRole: string;
  companyLocation?: string;
  jobType?: string;
  salary?: string;
  applicationDate?: string;
  status: ApplicationStatusEnum;
  jobUrl?: string;
  notes?: string;
  resumeId?: number;
}

/** Payload for updating a job application (PUT /api/job-applications/{id}). */
export interface UpdateJobApplicationRequest {
  companyName: string;
  jobRole: string;
  companyLocation?: string;
  jobType?: string;
  salary?: string;
  applicationDate?: string;
  status: ApplicationStatusEnum;
  jobUrl?: string;
  notes?: string;
  resumeId?: number;
}

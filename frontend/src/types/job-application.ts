/**
 * Type definitions for the Job Application Tracker module.
 *
 * Mirrors the backend DTOs in
 * com.devlaunch.dto.request.*,
 * com.devlaunch.dto.response.*, and
 * com.devlaunch.entity.enums.*.
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

/** Work modes supported by the tracker. */
export type WorkModeEnum = 'REMOTE' | 'HYBRID' | 'ONSITE';

/** Human-readable label for each work mode. */
export const WORK_MODE_LABELS: Record<WorkModeEnum, string> = {
  REMOTE: 'Remote',
  HYBRID: 'Hybrid',
  ONSITE: 'Onsite',
};

/** All work modes as an array. */
export const WORK_MODES: WorkModeEnum[] = ['REMOTE', 'HYBRID', 'ONSITE'];

/** Priority levels a user can assign to an application. */
export type ApplicationPriorityEnum = 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';

/** Human-readable label for each priority level. */
export const APPLICATION_PRIORITY_LABELS: Record<ApplicationPriorityEnum, string> = {
  LOW: 'Low',
  MEDIUM: 'Medium',
  HIGH: 'High',
  URGENT: 'Urgent',
};

/** All priority levels as an array. */
export const APPLICATION_PRIORITIES: ApplicationPriorityEnum[] = [
  'LOW',
  'MEDIUM',
  'HIGH',
  'URGENT',
];

/** Categories of documents attached to an application. */
export type AttachmentCategory =
  | 'RESUME'
  | 'COVER_LETTER'
  | 'OFFER_LETTER'
  | 'ASSESSMENT'
  | 'INTERVIEW_FEEDBACK'
  | 'OTHER';

/** Human-readable label for each attachment category. */
export const ATTACHMENT_CATEGORY_LABELS: Record<AttachmentCategory, string> = {
  RESUME: 'Resume used',
  COVER_LETTER: 'Cover letter',
  OFFER_LETTER: 'Offer letter',
  ASSESSMENT: 'Assessment PDF',
  INTERVIEW_FEEDBACK: 'Interview feedback',
  OTHER: 'Other',
};

/** All attachment categories as an array. */
export const ATTACHMENT_CATEGORIES: AttachmentCategory[] = [
  'RESUME',
  'COVER_LETTER',
  'OFFER_LETTER',
  'ASSESSMENT',
  'INTERVIEW_FEEDBACK',
  'OTHER',
];

/** Categories of milestones recorded on an application timeline. */
export type TimelineEventType =
  | 'ADDED'
  | 'APPLIED'
  | 'ASSESSMENT'
  | 'INTERVIEW'
  | 'OFFER'
  | 'REJECTED'
  | 'STATUS_UPDATED'
  | 'INTERVIEW_SCHEDULED'
  | 'INTERVIEW_CANCELLED'
  | 'ATTACHMENT_ADDED';

// ────────────────────────────────────────────────────
// Placement management DTOs
// ────────────────────────────────────────────────────

/** A single milestone on an application timeline. */
export interface TimelineEvent {
  id: number;
  eventType: TimelineEventType;
  title: string;
  notes: string | null;
  /** ISO-8601 timestamp of when the milestone occurred. */
  occurredAt: string;
}

/** A scheduled interview on an application. */
export interface InterviewSchedule {
  id: number;
  title: string;
  round: string | null;
  scheduledDate: string;
  scheduledTime: string | null;
  meetingLink: string | null;
  interviewer: string | null;
  notes: string | null;
  cancelled: boolean;
}

/** A single private interview note entry. */
export interface InterviewNote {
  id: number;
  content: string;
  createdAt: string;
}

/** Metadata for a document attached to an application. */
export interface ApplicationAttachment {
  id: number;
  fileName: string;
  contentType: string | null;
  fileSize: number;
  category: AttachmentCategory;
  createdAt: string;
}

/** The number of applications submitted in one month (yyyy-MM). */
export interface MonthlyApplicationCount {
  yearMonth: string;
  count: number;
}

/** Aggregated analytics for the user's applications. */
export interface ApplicationAnalytics {
  totalApplications: number;
  statusCounts: Partial<Record<ApplicationStatusEnum, number>>;
  monthlyApplications: MonthlyApplicationCount[];
  interviewRate: number | null;
  offerRate: number | null;
  rejectionRate: number | null;
  successRate: number | null;
  averageResponseTimeDays: number | null;
  activeInterviews: number;
  upcomingInterviews: InterviewSchedule[];
}

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
  companyWebsite: string | null;
  recruiterName: string | null;
  recruiterEmail: string | null;
  referral: string | null;
  workMode: WorkModeEnum | null;
  priority: ApplicationPriorityEnum;
  technology: string | null;
  notes: string | null;
  resumeId: number | null;
  /** The milestone history of this application, oldest first. */
  timeline: TimelineEvent[];
  /** The scheduled interviews for this application, ordered by date. */
  interviews: InterviewSchedule[];
  /** The next upcoming (future, non-cancelled) interview, or null. */
  upcomingInterview: InterviewSchedule | null;
  /** The number of private interview note entries. */
  notesCount: number;
  /** The number of attached documents. */
  attachmentCount: number;
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
  companyWebsite?: string;
  recruiterName?: string;
  recruiterEmail?: string;
  referral?: string;
  workMode?: WorkModeEnum;
  priority?: ApplicationPriorityEnum;
  technology?: string;
  notes?: string;
  resumeId?: number;
}

/** Payload for updating a job application (PUT /api/job-applications/{id}). */
export interface UpdateJobApplicationRequest extends CreateJobApplicationRequest {}

/** Payload for updating only the status (PUT /api/job-applications/{id}/status). */
export interface UpdateApplicationStatusRequest {
  status: ApplicationStatusEnum;
}

/** Payload for scheduling an interview (POST /api/job-applications/{id}/interviews). */
export interface ScheduleInterviewRequest {
  title: string;
  round?: string;
  scheduledDate: string;
  scheduledTime?: string;
  meetingLink?: string;
  interviewer?: string;
  notes?: string;
}

/** Payload for updating/cancelling an interview (PUT /api/job-applications/interviews/{id}). */
export interface UpdateInterviewScheduleRequest extends ScheduleInterviewRequest {
  cancelled?: boolean;
}

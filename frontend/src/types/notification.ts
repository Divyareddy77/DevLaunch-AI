/**
 * Type definitions for the notification center.
 *
 * Mirrors the backend NotificationResponse DTO and NotificationType enum.
 *
 * @see backend/src/main/java/com/devlaunch/dto/response/NotificationResponse.java
 * @author DevLaunch
 */

/** The notification categories supported by the platform. */
export type NotificationTypeEnum =
  | 'ANNOUNCEMENT'
  | 'RESUME'
  | 'JOB'
  | 'STUDY'
  | 'MOCK_INTERVIEW'
  | 'RESUME_REVIEW'
  | 'READINESS'
  | 'ACHIEVEMENT'
  | 'SYSTEM';

/** A single notification delivered to the authenticated user. */
export interface AppNotification {
  id: number;
  /** The notification headline. */
  title: string;
  /** The notification body. */
  message: string;
  /** The notification category. */
  type: NotificationTypeEnum;
  /** Whether the user has read this notification. */
  isRead: boolean;
  /** ISO-8601 creation timestamp. */
  createdAt: string;
}

/** Human-readable labels for each notification category. */
export const NOTIFICATION_TYPE_LABELS: Record<NotificationTypeEnum, string> = {
  ANNOUNCEMENT: 'Announcement',
  RESUME: 'Resume',
  JOB: 'Job Tracker',
  STUDY: 'Study Planner',
  MOCK_INTERVIEW: 'Mock Interview',
  RESUME_REVIEW: 'Resume Review',
  READINESS: 'Readiness',
  ACHIEVEMENT: 'Achievement',
  SYSTEM: 'System',
};

/** All notification category keys, useful for filters and dropdowns. */
export const NOTIFICATION_TYPES: NotificationTypeEnum[] = [
  'ANNOUNCEMENT',
  'RESUME',
  'JOB',
  'STUDY',
  'MOCK_INTERVIEW',
  'RESUME_REVIEW',
  'READINESS',
  'ACHIEVEMENT',
  'SYSTEM',
];

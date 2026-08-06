/**
 * Shared helper functions for the job application tracker module.
 *
 * Pure, framework-agnostic helpers used across the list view, board view,
 * detail modal, and dashboard widget.
 *
 * @author DevLaunch
 */

import { parseISO, isValid, differenceInCalendarDays } from 'date-fns';
import type {
  InterviewSchedule,
  JobApplicationResponse,
  ApplicationStatusEnum,
  TimelineEventType,
} from '../types/job-application';

/**
 * Returns the next upcoming (future, non-cancelled) interview for an
 * application, or null when none is scheduled.
 */
export function nextUpcomingInterview(
  app: Pick<JobApplicationResponse, 'interviews'>,
): InterviewSchedule | null {
  const today = todayKey();
  return (
    app.interviews.find((interview) => {
      if (interview.cancelled) return false;
      return interview.scheduledDate >= today;
    }) ?? null
  );
}

/**
 * Returns a short countdown label for an interview date.
 * Examples: "Today", "Tomorrow", "In 3 days", or the formatted date.
 */
export function interviewCountdownLabel(
  schedule: Pick<InterviewSchedule, 'scheduledDate'>,
): string {
  const date = parseISO(schedule.scheduledDate);
  if (!isValid(date)) return schedule.scheduledDate;

  const days = differenceInCalendarDays(date, new Date());
  if (days === 0) return 'Today';
  if (days === 1) return 'Tomorrow';
  if (days > 1) return `In ${days} days`;
  if (days === -1) return 'Yesterday';
  return `${Math.abs(days)} days ago`;
}

/**
 * Returns the number of days until an interview date (0 = today,
 * negative = past), or null when the date is invalid.
 */
export function daysUntilInterview(schedule: Pick<InterviewSchedule, 'scheduledDate'>): number | null {
  const date = parseISO(schedule.scheduledDate);
  if (!isValid(date)) return null;
  return differenceInCalendarDays(date, new Date());
}

/**
 * Checks whether an interview is upcoming (today or later) and not cancelled.
 */
export function isUpcomingInterview(interview: InterviewSchedule): boolean {
  if (interview.cancelled) return false;
  return interview.scheduledDate >= todayKey();
}

/**
 * Maps an application status to the timeline event type that represents
 * reaching it.
 */
export function timelineEventTypeForStatus(status: ApplicationStatusEnum): TimelineEventType {
  if (status === 'WISHLIST') return 'STATUS_UPDATED';
  return status;
}

/**
 * Keyword search across every searchable application field: company, role,
 * location, notes, recruiter, and technology.
 */
export function applicationMatchesKeyword(
  app: JobApplicationResponse,
  query: string,
): boolean {
  const q = query.toLowerCase().trim();
  if (!q) return true;

  const haystack = [
    app.companyName,
    app.jobRole,
    app.companyLocation,
    app.notes,
    app.recruiterName,
    app.recruiterEmail,
    app.technology,
    app.referral,
  ]
    .filter((value): value is string => Boolean(value))
    .join(' ')
    .toLowerCase();

  return haystack.includes(q);
}

/** Returns today's date as a YYYY-MM-DD string. */
export function todayKey(): string {
  const now = new Date();
  const month = String(now.getMonth() + 1).padStart(2, '0');
  const day = String(now.getDate()).padStart(2, '0');
  return `${now.getFullYear()}-${month}-${day}`;
}

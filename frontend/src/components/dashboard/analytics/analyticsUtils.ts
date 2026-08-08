/**
 * Shared helpers for the dashboard Analytics & Insights section.
 *
 * Pure, memo-friendly utilities for deriving chart data, durations,
 * streaks, and labels from existing API responses. No business logic is
 * duplicated here — these are presentation-only transforms.
 *
 * @author DevLaunch
 */

import { addDays, format, isSameDay, isToday, parseISO, startOfDay, subDays } from 'date-fns';
import type { ApplicationStatusEnum } from '../../../types/job-application';

/** Converts a "HH:mm[:ss]" time string to fractional hours (0–24). */
export function timeToHours(time: string | null | undefined): number {
  if (!time) return 0;
  const [hourPart, minutePart] = time.split(':');
  const hour = Number(hourPart);
  if (Number.isNaN(hour)) return 0;
  const minute = Number(minutePart ?? '0');
  return hour + (Number.isNaN(minute) ? 0 : minute / 60);
}

/** Duration in hours between two "HH:mm[:ss]" strings (never negative). */
export function hoursBetween(
  start: string | null | undefined,
  end: string | null | undefined,
): number {
  return Math.max(0, timeToHours(end) - timeToHours(start));
}

/** Short weekday label, e.g. "Mon". */
export function shortDayLabel(date: Date): string {
  return format(date, 'EEE');
}

/** Short date label, e.g. "Aug 7". */
export function shortDateLabel(date: Date): string {
  return format(date, 'MMM d');
}

/** Parses an ISO date string into a local Date, or null when invalid. */
export function safeParseDate(iso: string | null | undefined): Date | null {
  if (!iso) return null;
  const date = parseISO(iso);
  return Number.isNaN(date.getTime()) ? null : date;
}

/** The last `count` days, oldest first, each normalized to start-of-day. */
export function lastNDays(count: number): Date[] {
  const today = startOfDay(new Date());
  return Array.from({ length: count }, (_, i) => subDays(today, count - 1 - i));
}

/** Formats a Date as "yyyy-MM-dd" for comparison against API date strings. */
export function toDateKey(date: Date): string {
  return format(date, 'yyyy-MM-dd');
}

/** Whether a Date matches a "yyyy-MM-dd" (or ISO) API date string. */
export function matchesDateKey(date: Date, iso: string | null | undefined): boolean {
  if (!iso) return false;
  const parsed = safeParseDate(iso);
  return parsed !== null && isSameDay(date, parsed);
}

/**
 * Counts the current streak of consecutive days (ending today or yesterday)
 * that have at least one entry in `activeDayKeys`.
 */
export function countStreak(activeDayKeys: Set<string>): number {
  const today = startOfDay(new Date());
  let cursor = today;
  // Allow the streak to start from today OR yesterday so a fresh day doesn't
  // reset the counter before the first activity of the day.
  if (!activeDayKeys.has(toDateKey(cursor))) {
    cursor = addDays(cursor, -1);
  }
  let streak = 0;
  while (activeDayKeys.has(toDateKey(cursor))) {
    streak += 1;
    cursor = addDays(cursor, -1);
  }
  return streak;
}

/**
 * Formats a last-updated ISO timestamp as "Today" when it matches the
 * current date, otherwise as a short date.
 */
export function lastUpdatedLabel(iso: string | null | undefined): string {
  if (!iso) return 'Not tracked yet';
  const date = parseISO(iso);
  if (Number.isNaN(date.getTime())) return '—';
  if (isToday(date)) return 'Today';
  return format(date, 'MMM d, yyyy');
}

/** Semantic colour per job application status (used by the donut + legend). */
export const JOB_STATUS_COLORS: Record<ApplicationStatusEnum, string> = {
  WISHLIST: '#9ca3af',
  APPLIED: '#6366f1',
  ASSESSMENT: '#f59e0b',
  INTERVIEW: '#3b82f6',
  OFFER: '#10b981',
  REJECTED: '#ef4444',
};

/** Display order for the job pipeline statuses in charts. */
export const JOB_STATUS_ORDER: ApplicationStatusEnum[] = [
  'APPLIED',
  'ASSESSMENT',
  'INTERVIEW',
  'OFFER',
  'REJECTED',
  'WISHLIST',
];

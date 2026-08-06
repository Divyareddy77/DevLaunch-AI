/**
 * Date and time utility functions.
 *
 * Provides consistent date formatting, parsing, and calculation
 * helpers used throughout the application. All date values from
 * the backend arrive as ISO-8601 strings (mapping to Java
 * LocalDate / LocalTime), and these utilities convert them into
 * human-readable formats.
 *
 * @author DevLaunch
 */

import { format, formatDistanceToNow, parseISO, isValid, isPast, isFuture, differenceInDays } from 'date-fns';

/**
 * Formats a time-of-day string (HH:mm[:ss]) into a 12-hour label.
 * Example: "14:00:00" → "2:00 PM"
 */
export function formatTime(timeString: string | null | undefined): string {
  if (!timeString) return '—';
  const [hourPart, minutePart] = timeString.split(':');
  const hour = Number(hourPart);
  if (Number.isNaN(hour)) return timeString;
  const minute = minutePart ?? '00';
  const period = hour >= 12 ? 'PM' : 'AM';
  const displayHour = hour % 12 === 0 ? 12 : hour % 12;
  return `${displayHour}:${minute} ${period}`;
}

/**
 * Formats an ISO date string into a human-readable format.
 * Example: "2024-03-15" → "Mar 15, 2024"
 */
export function formatDate(dateString: string | null | undefined): string {
  if (!dateString) return '—';
  const date = parseISO(dateString);
  if (!isValid(date)) return '—';
  return format(date, 'MMM d, yyyy');
}

/**
 * Formats an ISO date string into a short format suitable for tables.
 * Example: "2024-03-15" → "03/15/2024"
 */
export function formatDateShort(dateString: string | null | undefined): string {
  if (!dateString) return '—';
  const date = parseISO(dateString);
  if (!isValid(date)) return '—';
  return format(date, 'MM/dd/yyyy');
}

/**
 * Returns a relative time string like "2 days ago" or "in 3 hours".
 */
export function formatRelativeTime(dateString: string | null | undefined): string {
  if (!dateString) return '—';
  const date = parseISO(dateString);
  if (!isValid(date)) return '—';
  return formatDistanceToNow(date, { addSuffix: true });
}

/**
 * Formats an ISO date-only string into a year-only format.
 * Example: "2024-03-15" → "2024"
 */
export function formatYear(dateString: string | null | undefined): string {
  if (!dateString) return '—';
  const date = parseISO(dateString);
  if (!isValid(date)) return '—';
  return format(date, 'yyyy');
}

/**
 * Formats a date range as "Mar 2020 – Present" or "Mar 2020 – Jun 2024".
 */
export function formatDateRange(
  startDate: string | null | undefined,
  endDate: string | null | undefined,
  ongoingLabel = 'Present',
): string {
  const start = startDate ? format(parseISO(startDate), 'MMM yyyy') : '—';
  const end = endDate ? format(parseISO(endDate), 'MMM yyyy') : ongoingLabel;
  return `${start} – ${end}`;
}

/**
 * Checks whether a given ISO date string is in the past.
 */
export function isDatePast(dateString: string | null | undefined): boolean {
  if (!dateString) return false;
  const date = parseISO(dateString);
  return isValid(date) && isPast(date);
}

/**
 * Checks whether a given ISO date string is in the future.
 */
export function isDateFuture(dateString: string | null | undefined): boolean {
  if (!dateString) return false;
  const date = parseISO(dateString);
  return isValid(date) && isFuture(date);
}

/**
 * Calculates the number of days between now and the given date.
 * Returns a negative number if the date is in the past.
 */
export function daysUntil(dateString: string | null | undefined): number | null {
  if (!dateString) return null;
  const date = parseISO(dateString);
  if (!isValid(date)) return null;
  return differenceInDays(date, new Date());
}

/**
 * Returns the current date as a string in YYYY-MM-DD format,
 * suitable for setting default values on date input fields.
 */
export function todayString(): string {
  return format(new Date(), 'yyyy-MM-dd');
}

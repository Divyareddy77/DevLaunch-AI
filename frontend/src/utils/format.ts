/**
 * General formatting utility functions.
 *
 * Provides consistent helpers for formatting common data types
 * like strings, numbers, and URLs across the application.
 *
 * @author DevLaunch
 */

/**
 * Capitalises the first letter of a string.
 */
export function capitalize(str: string): string {
  if (!str) return str;
  return str.charAt(0).toUpperCase() + str.slice(1).toLowerCase();
}

/**
 * Converts a string to title case (each word capitalised).
 */
export function titleCase(str: string): string {
  if (!str) return str;
  return str
    .split(/[\s_-]+/)
    .map((word) => capitalize(word))
    .join(' ');
}

/**
 * Converts an enum-style string like "IN_PROGRESS" to
 * a human-readable label like "In Progress".
 */
export function enumToLabel(value: string | null | undefined): string {
  if (!value) return '—';
  return titleCase(value.replace(/_/g, ' '));
}

/**
 * Maps a 0–100 score to a Badge variant for consistent colour coding.
 * Scores of 70+ are success, 50–69 are warning, and below 50 are danger.
 */
export function getScoreBadgeVariant(score: number): 'success' | 'warning' | 'danger' {
  if (score >= 70) return 'success';
  if (score >= 50) return 'warning';
  return 'danger';
}

/**
 * Formats a duration in seconds as "12m 30s" (or just "45s" under a minute).
 */
export function formatDuration(totalSeconds: number | null | undefined): string {
  if (totalSeconds === null || totalSeconds === undefined) return '—';
  const seconds = Math.max(0, Math.round(totalSeconds));
  const minutes = Math.floor(seconds / 60);
  const remaining = seconds % 60;
  if (minutes === 0) return `${remaining}s`;
  if (remaining === 0) return `${minutes}m`;
  return `${minutes}m ${remaining}s`;
}

/**
 * Formats a duration in seconds as "MM:SS" for timers and countdowns.
 */
export function formatClock(totalSeconds: number): string {
  const seconds = Math.max(0, Math.round(totalSeconds));
  const minutes = Math.floor(seconds / 60);
  const remaining = seconds % 60;
  return `${String(minutes).padStart(2, '0')}:${String(remaining).padStart(2, '0')}`;
}

/**
 * Truncates a string to the given length, appending an ellipsis if needed.
 */
export function truncate(str: string, maxLength: number): string {
  if (!str || str.length <= maxLength) return str;
  return str.slice(0, maxLength).trimEnd() + '…';
}

/**
 * Formats a number with comma separators.
 * Example: 1234567 → "1,234,567"
 */
export function formatNumber(value: number | null | undefined): string {
  if (value === null || value === undefined) return '—';
  return value.toLocaleString();
}

/**
 * Formats a percentage value.
 * Example: 0.856 → "85.6%"
 */
export function formatPercentage(value: number | null | undefined, decimals = 1): string {
  if (value === null || value === undefined) return '—';
  return `${(value * 100).toFixed(decimals)}%`;
}

/**
 * Formats a salary string for display.
 * If the string already contains formatting, it is returned as-is.
 */
export function formatSalary(salary: string | null | undefined): string {
  if (!salary) return 'Not specified';
  return salary;
}

/**
 * Extracts the hostname from a URL for compact display.
 * Example: "https://github.com/user/repo" → "github.com"
 */
export function extractDomain(url: string | null | undefined): string {
  if (!url) return '';
  try {
    const parsed = new URL(url);
    return parsed.hostname.replace(/^www\./, '');
  } catch {
    return url;
  }
}

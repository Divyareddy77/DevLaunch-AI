/**
 * LocalStorage / SessionStorage key constants.
 *
 * All storage keys used across the application are defined here
 * to prevent typos and ensure a single source of truth for how
 * data is persisted in the browser.
 *
 * @author DevLaunch
 */

export const STORAGE_KEYS = {
  /** JWT access token saved after successful login. */
  AUTH_TOKEN: 'devlaunch_auth_token',

  /** User preference: dark mode enabled. */
  THEME_DARK_MODE: 'devlaunch_theme_dark',

  /** The sidebar collapsed/expanded state. */
  SIDEBAR_COLLAPSED: 'devlaunch_sidebar_collapsed',

  /** Previously selected resume ID (for quick navigation). */
  LAST_RESUME_ID: 'devlaunch_last_resume_id',

  /** Announcement IDs the user has dismissed (hidden on reload). */
  DISMISSED_ANNOUNCEMENTS: 'devlaunch_dismissed_announcements',
} as const;

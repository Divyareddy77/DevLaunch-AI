/**
 * General application-wide constants.
 *
 * Contains configuration values that control application behaviour
 * such as pagination, limits, and branding strings.
 *
 * @author DevLaunch
 */

export const APP = {
  /** The application display name. */
  NAME: 'DevLaunch',

  /** Short tagline used in branding contexts. */
  TAGLINE: 'AI-Powered Developer Career Hub',

  /** Default page size for paginated lists. */
  DEFAULT_PAGE_SIZE: 10,

  /** Maximum file size for resume uploads in bytes (5 MB). */
  MAX_FILE_SIZE_BYTES: 5 * 1024 * 1024,

  /** Allowed file extensions for resume uploads. */
  ALLOWED_FILE_EXTENSIONS: ['.pdf', '.docx', '.doc'] as readonly string[],

  /** Debounce delay for search inputs in milliseconds. */
  SEARCH_DEBOUNCE_MS: 300,

  /** Auto-dismiss duration for toast notifications in milliseconds. */
  TOAST_DURATION_MS: 4000,
} as const;

/**
 * Error handling utility functions.
 *
 * Provides a consistent helper for extracting human-readable messages
 * from API errors, preferring the backend's ErrorResponse.message.
 *
 * @author DevLaunch
 */

import axios from 'axios';

/**
 * Extracts a human-readable message from an Axios or generic error.
 *
 * For Axios errors the backend's ErrorResponse.message is surfaced
 * (e.g. "GitHub user 'abcxyz' not found") instead of the generic
 * "Request failed with status code 404".
 *
 * @param err the thrown error
 * @param fallback the message to use when no usable error message exists
 */
export function getErrorMessage(err: unknown, fallback: string): string {
  if (axios.isAxiosError(err)) {
    return (err.response?.data as { message?: string } | undefined)?.message ?? fallback;
  }
  return err instanceof Error ? err.message : fallback;
}

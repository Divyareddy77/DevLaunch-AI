/**
 * Error handling utility functions.
 *
 * Provides a consistent helper for extracting human-readable messages
 * from API errors, preferring the backend's ErrorResponse.message.
 *
 * @author DevLaunch
 */

import axios from 'axios';
import { MESSAGES } from '../constants/messages';

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

/**
 * Maps a registration API error to a user-friendly message.
 *
 * The backend returns HTTP 409 Conflict when the email is already
 * registered; every other failure (network error, server error, etc.)
 * falls back to a generic message. Raw Axios errors and HTTP status
 * codes are never surfaced to the user.
 *
 * @param err the thrown error
 */
export function getRegisterErrorMessage(err: unknown): string {
  if (axios.isAxiosError(err) && err.response?.status === 409) {
    return MESSAGES.REGISTER_EMAIL_EXISTS;
  }
  return MESSAGES.REGISTER_ERROR_GENERIC;
}

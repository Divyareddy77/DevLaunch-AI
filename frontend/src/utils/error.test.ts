/**
 * Unit tests for the registration error message mapping.
 *
 * Verifies that an HTTP 409 (email already registered) produces the
 * friendly email-exists message, while every other failure falls back
 * to a generic message — never exposing raw Axios errors or status codes.
 *
 * @author DevLaunch
 */

import { describe, it, expect } from 'vitest';
import axios from 'axios';
import type { AxiosError, InternalAxiosRequestConfig } from 'axios';
import { MESSAGES } from '../constants/messages';
import { getRegisterErrorMessage } from './error';

/** Builds an AxiosError carrying the given HTTP response status. */
function axiosErrorWithStatus(status: number): AxiosError {
  return new axios.AxiosError(
    `Request failed with status code ${status}`,
    undefined,
    undefined,
    undefined,
    {
      status,
      statusText: 'Error',
      headers: {},
      config: {} as InternalAxiosRequestConfig,
      data: {},
    },
  );
}

describe('getRegisterErrorMessage', () => {
  it('returns the friendly email-exists message for HTTP 409', () => {
    expect(getRegisterErrorMessage(axiosErrorWithStatus(409))).toBe(
      MESSAGES.REGISTER_EMAIL_EXISTS,
    );
  });

  it('returns a generic message for other HTTP error statuses', () => {
    expect(getRegisterErrorMessage(axiosErrorWithStatus(400))).toBe(
      MESSAGES.REGISTER_ERROR_GENERIC,
    );
    expect(getRegisterErrorMessage(axiosErrorWithStatus(500))).toBe(
      MESSAGES.REGISTER_ERROR_GENERIC,
    );
  });

  it('returns a generic message for network errors without a response', () => {
    const networkError = new axios.AxiosError('Network Error');
    expect(getRegisterErrorMessage(networkError)).toBe(MESSAGES.REGISTER_ERROR_GENERIC);
  });

  it('returns a generic message for non-Axios errors', () => {
    expect(getRegisterErrorMessage(new Error('boom'))).toBe(MESSAGES.REGISTER_ERROR_GENERIC);
    expect(getRegisterErrorMessage('something went wrong')).toBe(
      MESSAGES.REGISTER_ERROR_GENERIC,
    );
  });

  it('never exposes the raw axios message or HTTP status code', () => {
    const message = getRegisterErrorMessage(axiosErrorWithStatus(409));
    expect(message).not.toContain('Request failed');
    expect(message).not.toContain('409');
  });
});

/**
 * Centralised Axios HTTP client for the DevLaunch API.
 *
 * Provides a preconfigured Axios instance with:
 * - Base URL from environment variable (falls back to proxy /api)
 * - Request interceptor that attaches the JWT Bearer token
 * - Response interceptor that handles 401 unauthorised by clearing
 *   auth state and redirecting to login
 *
 * @author DevLaunch
 */

import axios from 'axios';
import { STORAGE_KEYS } from '../constants/storage';
import { ROUTES } from '../constants/routes';

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_URL ?? '',
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 15_000,
});

/**
 * Request interceptor — attaches the JWT access token from localStorage
 * to every outgoing request as a Bearer token.
 */
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem(STORAGE_KEYS.AUTH_TOKEN);
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error),
);

/**
 * Response interceptor — catches 401 responses and triggers logout
 * by clearing the stored token and redirecting to the login page.
 */
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (axios.isAxiosError(error) && error.response?.status === 401) {
      localStorage.removeItem(STORAGE_KEYS.AUTH_TOKEN);
      // Only redirect if not already on a public route
      const currentPath = window.location.pathname;
      if (!currentPath.startsWith('/auth/')) {
        window.location.href = ROUTES.LOGIN;
      }
    }
    return Promise.reject(error);
  },
);

export default apiClient;

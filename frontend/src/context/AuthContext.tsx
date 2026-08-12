/**
 * Authentication context and provider.
 *
 * Manages JWT-based authentication state globally. Handles login,
 * registration, logout, and automatic session restoration from
 * localStorage on page reload.
 *
 * @author DevLaunch
 */

import React, { createContext, useState, useCallback, useEffect, useMemo } from 'react';
import type { LoginRequest, RegisterRequest } from '../types/auth';
import type { UserResponse } from '../types/user';
import { authService } from '../services/auth.service';
import { userService } from '../services/user.service';
import { STORAGE_KEYS } from '../constants/storage';
import { getRegisterErrorMessage } from '../utils/error';
import axios from 'axios';

/** Result of a login attempt. */
export interface LoginResult {
  success: boolean;
  error?: string;
}

/** Result of a registration attempt. */
export interface RegisterResult {
  success: boolean;
  error?: string;
}

/** Shape of the authentication context value. */
export interface AuthContextValue {
  /** The currently authenticated user, or null if not logged in. */
  user: UserResponse | null;
  /** The JWT access token, or null if not logged in. */
  token: string | null;
  /** Whether the user is authenticated. */
  isAuthenticated: boolean;
  /** Whether the initial auth check is still in progress. */
  isLoading: boolean;
  /** Whether an auth operation (login/register) is in progress. */
  isSubmitting: boolean;
  /** Error message from the last failed auth operation. */
  error: string | null;
  /**
   * Logs in an existing user.
   * Stores the JWT in localStorage and fetches the user profile.
   */
  login: (data: LoginRequest) => Promise<LoginResult>;
  /**
   * Registers a new user.
   * Note: the backend registration endpoint returns the user profile
   * directly (no JWT is issued during registration — the user must
   * log in afterward).
   */
  register: (data: RegisterRequest) => Promise<RegisterResult>;
  /** Logs out the current user by clearing stored state. */
  logout: () => void;
  /** Clears any auth error. */
  clearError: () => void;
}

export const AuthContext = createContext<AuthContextValue | undefined>(undefined);

/** Props for the AuthProvider component. */
interface AuthProviderProps {
  children: React.ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [user, setUser] = useState<UserResponse | null>(null);
  const [token, setToken] = useState<string | null>(() =>
    localStorage.getItem(STORAGE_KEYS.AUTH_TOKEN),
  );
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  const isAuthenticated = !!token && !!user;

  // Clear any auth-related error
  const clearError = useCallback(() => setError(null), []);

  // On mount, if a token exists in localStorage, validate it by
  // fetching the current user's profile. If the request fails
  // (expired or invalid token), clear auth state.
  useEffect(() => {
    const storedToken = localStorage.getItem(STORAGE_KEYS.AUTH_TOKEN);
    if (!storedToken) {
      setIsLoading(false);
      return;
    }

    setToken(storedToken);

    userService
      .getCurrentUser()
      .then((userData) => {
        setUser(userData);
      })
      .catch(() => {
        // Token invalid or expired — clear everything
        localStorage.removeItem(STORAGE_KEYS.AUTH_TOKEN);
        setToken(null);
        setUser(null);
      })
      .finally(() => {
        setIsLoading(false);
      });
  }, []);

  // Login: POST /api/auth/login, store token, fetch profile
  const login = useCallback(async (data: LoginRequest): Promise<LoginResult> => {
    setIsSubmitting(true);
    setError(null);

    try {
      const authResponse = await authService.login(data);
      localStorage.setItem(STORAGE_KEYS.AUTH_TOKEN, authResponse.accessToken);
      setToken(authResponse.accessToken);

      // Fetch the user profile immediately after login
      const userData = await userService.getCurrentUser();
      setUser(userData);

      return { success: true };
    } catch (err: unknown) {
  let message = 'Login failed. Please try again.';

  if (axios.isAxiosError(err)) {
    message =
      (err.response?.data as { message?: string })?.message ??
      message;
  } else if (err instanceof Error) {
    message = err.message;
  }

  setError(message);

  return {
    success: false,
    error: message,
  };
} finally {
      setIsSubmitting(false);
    }
  }, []);

  // Register: POST /api/auth/register
  const register = useCallback(async (data: RegisterRequest): Promise<RegisterResult> => {
    setIsSubmitting(true);
    setError(null);

    try {
      const userData = await authService.register(data);
      setUser(userData);
      // Registration does not issue a JWT — the user must log in afterward
      return { success: true };
    } catch (err: unknown) {
      const message = getRegisterErrorMessage(err);
      setError(message);
      return { success: false, error: message };
    } finally {
      setIsSubmitting(false);
    }
  }, []);

  // Logout: clear stored token and user state
  const logout = useCallback(() => {
    localStorage.removeItem(STORAGE_KEYS.AUTH_TOKEN);
    setToken(null);
    setUser(null);
    setError(null);
  }, []);

  const value = useMemo<AuthContextValue>(
    () => ({
      user,
      token,
      isAuthenticated,
      isLoading,
      isSubmitting,
      error,
      login,
      register,
      logout,
      clearError,
    }),
    [user, token, isAuthenticated, isLoading, isSubmitting, error, login, register, logout, clearError],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

/**
 * Authentication service.
 *
 * Provides methods for user registration and login by communicating
 * with the backend AuthController endpoints.
 *
 * @see backend/src/main/java/com/devlaunch/controller/AuthController.java
 * @author DevLaunch
 */

import apiClient from '../api/client';
import { AUTH } from '../api/endpoints';
import type {
  LoginRequest,
  RegisterRequest,
  AuthResponse,
  ForgotPasswordRequest,
  ResetPasswordRequest,
  MessageResponse,
} from '../types/auth';
import type { UserResponse } from '../types/user';

export const authService = {
  /**
   * Authenticates an existing user and returns a JWT access token.
   *
   * POST /api/auth/login
   */
  login: (data: LoginRequest) =>
    apiClient.post<AuthResponse>(AUTH.LOGIN, data).then((res) => res.data),

  /**
   * Registers a new user account and returns the created user's profile.
   *
   * POST /api/auth/register
   */
  register: (data: RegisterRequest) =>
    apiClient.post<UserResponse>(AUTH.REGISTER, data).then((res) => res.data),

  /**
   * Requests a password reset link for the given email.
   * Always returns the same generic message whether or not the account exists.
   *
   * POST /api/auth/forgot-password
   */
  forgotPassword: (data: ForgotPasswordRequest) =>
    apiClient.post<MessageResponse>(AUTH.FORGOT_PASSWORD, data).then((res) => res.data),

  /**
   * Completes a password reset using the one-time token from the email.
   *
   * POST /api/auth/reset-password
   */
  resetPassword: (data: ResetPasswordRequest) =>
    apiClient.post<MessageResponse>(AUTH.RESET_PASSWORD, data).then((res) => res.data),
};

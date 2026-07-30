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
import type { LoginRequest, RegisterRequest, AuthResponse } from '../types/auth';
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
};

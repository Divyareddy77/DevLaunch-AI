/**
 * User profile service.
 *
 * Provides methods for retrieving and updating the authenticated
 * user's profile, as well as changing their password.
 *
 * @see backend/src/main/java/com/devlaunch/controller/UserController.java
 * @author DevLaunch
 */

import apiClient from '../api/client';
import { USERS } from '../api/endpoints';
import type { UserResponse, UpdateUserRequest, ChangePasswordRequest } from '../types/user';

export const userService = {
  /**
   * Retrieves the profile of the currently authenticated user.
   *
   * GET /api/users/me
   */
  getCurrentUser: () =>
    apiClient.get<UserResponse>(USERS.ME).then((res) => res.data),

  /**
   * Updates the profile of the currently authenticated user.
   *
   * PUT /api/users/me
   */
  updateCurrentUser: (data: UpdateUserRequest) =>
    apiClient.put<UserResponse>(USERS.ME, data).then((res) => res.data),

  /**
   * Changes the password of the currently authenticated user.
   *
   * PUT /api/users/change-password
   */
  changePassword: (data: ChangePasswordRequest) =>
    apiClient.put<string>(USERS.CHANGE_PASSWORD, data).then((res) => res.data),
};

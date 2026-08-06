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
import type {
  UserResponse,
  UpdateUserRequest,
  ChangePasswordRequest,
  LinkedAccountRequest,
} from '../types/user';

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
   * Links a GitHub username to the authenticated user.
   *
   * PUT /api/users/me/github
   */
  connectGitHub: (username: string) =>
    apiClient
      .put<UserResponse>(USERS.GITHUB_CONNECT, { username } as LinkedAccountRequest)
      .then((res) => res.data),

  /**
   * Removes the linked GitHub username from the authenticated user.
   *
   * DELETE /api/users/me/github
   */
  disconnectGitHub: () =>
    apiClient.delete<UserResponse>(USERS.GITHUB_CONNECT).then((res) => res.data),

  /**
   * Links a LeetCode username to the authenticated user.
   *
   * PUT /api/users/me/leetcode
   */
  connectLeetCode: (username: string) =>
    apiClient
      .put<UserResponse>(USERS.LEETCODE_CONNECT, { username } as LinkedAccountRequest)
      .then((res) => res.data),

  /**
   * Removes the linked LeetCode username from the authenticated user.
   *
   * DELETE /api/users/me/leetcode
   */
  disconnectLeetCode: () =>
    apiClient.delete<UserResponse>(USERS.LEETCODE_CONNECT).then((res) => res.data),

  /**
   * Changes the password of the currently authenticated user.
   *
   * PUT /api/users/change-password
   */
  changePassword: (data: ChangePasswordRequest) =>
    apiClient.put<string>(USERS.CHANGE_PASSWORD, data).then((res) => res.data),
};

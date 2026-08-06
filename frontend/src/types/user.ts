/**
 * Type definitions for user profile data.
 *
 * Mirrors the backend response DTO in
 * com.devlaunch.dto.response.UserResponse.
 *
 * @author DevLaunch
 */

/** User profile returned from profile and auth endpoints. */
export interface UserResponse {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  /** The GitHub username linked to this account, or null if none connected. */
  githubUsername: string | null;
  /** The LeetCode username linked to this account, or null if none connected. */
  leetcodeUsername: string | null;
  role: string;
  isActive: boolean;
}

/** Payload for updating the user's profile (PUT /api/users/me). */
export interface UpdateUserRequest {
  firstName: string;
  lastName: string;
  phone: string;
}

/** Payload for linking an external account username (PUT /api/users/me/github | /leetcode). */
export interface LinkedAccountRequest {
  /** The username to save (e.g. "octocat"). */
  username: string;
}

/** Payload for changing the user's password (PUT /api/users/change-password). */
export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}

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
  role: string;
  isActive: boolean;
}

/** Payload for updating the user's profile (PUT /api/users/me). */
export interface UpdateUserRequest {
  firstName: string;
  lastName: string;
  phone: string;
}

/** Payload for changing the user's password (PUT /api/users/change-password). */
export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}

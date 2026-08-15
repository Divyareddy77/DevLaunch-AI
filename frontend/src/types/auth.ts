/**
 * Type definitions for authentication.
 *
 * Mirrors the backend request/response DTOs in
 * com.devlaunch.dto.request.LoginRequest,
 * com.devlaunch.dto.request.RegisterRequest,
 * and com.devlaunch.dto.response.AuthResponse.
 *
 * @author DevLaunch
 */

/** Login credentials sent to POST /api/auth/login. */
export interface LoginRequest {
  email: string;
  password: string;
}

/** Registration payload sent to POST /api/auth/register. */
export interface RegisterRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  phone: string;
}

/** Response returned on successful login. */
export interface AuthResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  message: string;
}

/** Payload sent to POST /api/auth/forgot-password. */
export interface ForgotPasswordRequest {
  email: string;
}

/** Payload sent to POST /api/auth/reset-password. */
export interface ResetPasswordRequest {
  token: string;
  newPassword: string;
  confirmPassword: string;
}

/** Generic message body returned by the password reset endpoints. */
export interface MessageResponse {
  message: string;
}

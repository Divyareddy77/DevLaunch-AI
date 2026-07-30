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

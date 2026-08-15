package com.devlaunch.service.interfaces;

import com.devlaunch.dto.request.ForgotPasswordRequest;
import com.devlaunch.dto.request.LoginRequest;
import com.devlaunch.dto.request.RegisterRequest;
import com.devlaunch.dto.request.ResetPasswordRequest;
import com.devlaunch.dto.response.AuthResponse;
import com.devlaunch.dto.response.MessageResponse;
import com.devlaunch.dto.response.UserResponse;

/**
 * Service interface for authentication operations.
 * <p>
 * Defines the contract for user registration and login functionality,
 * including account creation with role assignment and JWT-based
 * authentication.
 * </p>
 *
 * @author DevLaunch
 */
public interface AuthService {

    /**
     * Registers a new user account.
     * <p>
     * Validates that the email is not already in use, assigns the
     * default STUDENT role, encodes the password, and persists the
     * new user. Returns the created user's profile information.
     * </p>
     *
     * @param request the registration details containing name, email, password, and phone
     * @return the newly created user's profile data
     * @throws com.devlaunch.exception.EmailAlreadyExistsException if the email is already registered
     * @throws com.devlaunch.exception.ResourceNotFoundException    if the STUDENT role is not found
     */
    UserResponse register(RegisterRequest request);

    /**
     * Authenticates an existing user and issues a JWT token.
     * <p>
     * Validates credentials via Spring Security's {@code AuthenticationManager},
     * loads the authenticated user's details, and generates a signed JWT
     * access token for subsequent authenticated requests.
     * </p>
     *
     * @param request the login credentials containing email and password
     * @return an authentication response containing the JWT access token and metadata
     * @throws com.devlaunch.exception.InvalidCredentialsException if the email or password is incorrect
     */
    AuthResponse login(LoginRequest request);

    /**
     * Initiates the password reset flow for the given email.
     * <p>
     * If an account exists, a single-use, time-limited reset token is
     * generated, persisted, and emailed to the user. The response is
     * identical whether or not the account exists so account existence
     * is never disclosed to the caller. Any previous unused token for
     * the user is invalidated.
     * </p>
     *
     * @param request the email address of the account requesting a reset
     * @return a generic confirmation message
     */
    MessageResponse forgotPassword(ForgotPasswordRequest request);

    /**
     * Completes a password reset using a one-time token.
     * <p>
     * Validates that the token exists, is unused, and has not expired,
     * that the two passwords match, and that the new password meets the
     * strength requirements. The new password is hashed with the existing
     * BCrypt configuration, the token is consumed immediately, and a
     * notification is created for the user.
     * </p>
     *
     * @param request the reset token and the new password
     * @return a confirmation message
     * @throws com.devlaunch.exception.InvalidPasswordResetTokenException if the
     *                                    token does not exist or has been used
     * @throws com.devlaunch.exception.PasswordResetTokenExpiredException if the
     *                                    token has expired
     * @throws IllegalArgumentException   if the passwords do not match
     */
    MessageResponse resetPassword(ResetPasswordRequest request);

}

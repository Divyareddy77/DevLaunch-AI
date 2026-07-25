package com.devlaunch.service.interfaces;

import com.devlaunch.dto.request.LoginRequest;
import com.devlaunch.dto.request.RegisterRequest;
import com.devlaunch.dto.response.AuthResponse;
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

}

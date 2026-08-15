package com.devlaunch.controller;

import com.devlaunch.dto.request.ForgotPasswordRequest;
import com.devlaunch.dto.request.LoginRequest;
import com.devlaunch.dto.request.RegisterRequest;
import com.devlaunch.dto.request.ResetPasswordRequest;
import com.devlaunch.dto.response.AuthResponse;
import com.devlaunch.dto.response.MessageResponse;
import com.devlaunch.dto.response.UserResponse;
import com.devlaunch.service.interfaces.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for user authentication operations.
 * <p>
 * Exposes endpoints for user registration and login, delegating all
 * business logic to {@link AuthService}. Registration creates a new
 * user account and returns the user's profile. Login authenticates
 * credentials and returns a JWT access token.
 * </p>
 *
 * @author DevLaunch
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Registers a new user account.
     * <p>
     * Accepts registration details, validates the input, delegates
     * account creation to {@link AuthService#register(RegisterRequest)},
     * and returns the newly created user's profile data.
     * </p>
     *
     * @param request the registration details containing name, email,
     *                password, and phone number
     * @return a {@link ResponseEntity} containing the created user's
     *         profile with HTTP status 201 (Created)
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Authenticates an existing user and issues a JWT token.
     * <p>
     * Accepts login credentials, validates the input, delegates
     * authentication to {@link AuthService#login(LoginRequest)},
     * and returns a JWT access token with metadata.
     * </p>
     *
     * @param request the login credentials containing email and password
     * @return a {@link ResponseEntity} containing the JWT access token
     *         and metadata with HTTP status 200 (OK)
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Initiates the password reset flow.
     * <p>
     * Accepts an email address and always returns the same generic message
     * so the response never reveals whether the account exists. When the
     * account does exist, a one-time reset link is emailed to the user.
     * </p>
     *
     * @param request the email address requesting a reset
     * @return a generic confirmation message with HTTP status 200 (OK)
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        MessageResponse response = authService.forgotPassword(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Completes a password reset with a one-time token.
     * <p>
     * Validates the token (existence, expiry, single use) and the new
     * password, then updates the user's password and consumes the token.
     * </p>
     *
     * @param request the reset token and new password
     * @return a confirmation message with HTTP status 200 (OK)
     */
    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        MessageResponse response = authService.resetPassword(request);
        return ResponseEntity.ok(response);
    }

}

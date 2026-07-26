package com.devlaunch.controller;

import com.devlaunch.dto.request.UpdateUserRequest;
import com.devlaunch.dto.response.UserResponse;
import com.devlaunch.service.interfaces.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for user profile management operations.
 * <p>
 * Exposes endpoints for retrieving and updating the currently
 * authenticated user's profile. All endpoints require a valid
 * JWT access token and operate exclusively on the authenticated
 * user's own data.
 * </p>
 *
 * @author DevLaunch
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Retrieves the profile of the currently authenticated user.
     * <p>
     * Delegates to {@link UserService#getCurrentUser()} to fetch
     * the authenticated user's data and returns a safe subset of
     * profile fields excluding sensitive information.
     * </p>
     *
     * @return a {@link ResponseEntity} containing the authenticated
     *         user's profile data with HTTP status 200 (OK)
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        UserResponse response = userService.getCurrentUser();
        return ResponseEntity.ok(response);
    }

    /**
     * Updates the profile of the currently authenticated user.
     * <p>
     * Accepts the updated profile fields, validates the input,
     * and delegates the update to {@link UserService#updateCurrentUser(UpdateUserRequest)}.
     * Returns the refreshed profile data after the update.
     * </p>
     *
     * @param request the update request containing the new values
     *                for firstName, lastName, and phone
     * @return a {@link ResponseEntity} containing the updated user's
     *         profile data with HTTP status 200 (OK)
     */
    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateCurrentUser(
            @Valid @RequestBody final UpdateUserRequest request) {
        UserResponse response = userService.updateCurrentUser(request);
        return ResponseEntity.ok(response);
    }

}

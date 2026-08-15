package com.devlaunch.service.interfaces;

import com.devlaunch.dto.request.ChangePasswordRequest;
import com.devlaunch.dto.request.UpdateUserRequest;
import com.devlaunch.dto.response.UserResponse;
import com.devlaunch.exception.ResourceNotFoundException;

/**
 * Service interface for user profile management operations.
 * <p>
 * Defines the contract for retrieving and updating the currently
 * authenticated user's profile information. All operations are
 * scoped to the authenticated user and do not allow accessing
 * or modifying profiles of other users.
 * </p>
 *
 * @author DevLaunch
 */
public interface UserService {

    /**
     * Retrieves the profile of the currently authenticated user.
     * <p>
     * Obtains the authenticated user's details from the security
     * context and returns a safe subset of profile data excluding
     * sensitive fields such as the password.
     * </p>
     *
     * @return the currently authenticated user's profile data
     * @throws ResourceNotFoundException if the authenticated user
     *                                   is not found in the database
     */
    UserResponse getCurrentUser();

    /**
     * Updates the profile of the currently authenticated user.
     * <p>
     * Applies the supplied changes to the user's editable fields
     * (firstName, lastName, phone) and persists the updated entity.
     * Returns the refreshed profile data after the update.
     * </p>
     *
     * @param request the update request containing the new values
     *                for the editable profile fields
     * @return the updated user's profile data
     * @throws ResourceNotFoundException if the authenticated user
     *                                   is not found in the database
     */
    UserResponse updateCurrentUser(UpdateUserRequest request);

    /**
     * Links a GitHub username to the currently authenticated user.
     * <p>
     * Stores the username on the user so the dashboard and GitHub
     * Analytics page reuse the same linked account. Creates a
     * notification confirming the connection.
     * </p>
     *
     * @param username the GitHub username to save (leading/trailing
     *                 whitespace is trimmed)
     * @return the updated user profile data
     * @throws ResourceNotFoundException if the authenticated user
     *                                   is not found in the database
     */
    UserResponse connectGitHub(String username);

    /**
     * Removes the linked GitHub username from the currently
     * authenticated user.
     * <p>
     * Clears the saved username so the dashboard returns to the
     * "no account connected" state. Creates a notification confirming
     * the disconnection.
     * </p>
     *
     * @return the updated user profile data
     * @throws ResourceNotFoundException if the authenticated user
     *                                   is not found in the database
     */
    UserResponse disconnectGitHub();

    /**
     * Links a LeetCode username to the currently authenticated user.
     * <p>
     * Stores the username on the user so the dashboard and LeetCode
     * Tracker page reuse the same linked account. Creates a
     * notification confirming the connection.
     * </p>
     *
     * @param username the LeetCode username to save (leading/trailing
     *                 whitespace is trimmed)
     * @return the updated user profile data
     * @throws ResourceNotFoundException if the authenticated user
     *                                   is not found in the database
     */
    UserResponse connectLeetCode(String username);

    /**
     * Removes the linked LeetCode username from the currently
     * authenticated user.
     * <p>
     * Clears the saved username so the dashboard returns to the
     * "no account connected" state. Creates a notification confirming
     * the disconnection.
     * </p>
     *
     * @return the updated user profile data
     * @throws ResourceNotFoundException if the authenticated user
     *                                   is not found in the database
     */
    UserResponse disconnectLeetCode();

    /**
     * Changes the password of the currently authenticated user.
     * <p>
     * Verifies the supplied current password against the stored hash.
     * If the passwords match, the new password is encoded and persisted.
     * If they do not match, an {@link IllegalArgumentException} is thrown.
     * </p>
     *
     * @param request the change password request containing the current
     *                and new passwords
     * @throws ResourceNotFoundException if the authenticated user
     *                                   is not found in the database
     * @throws IllegalArgumentException  if the current password is incorrect
     */
    void changePassword(ChangePasswordRequest request);

}

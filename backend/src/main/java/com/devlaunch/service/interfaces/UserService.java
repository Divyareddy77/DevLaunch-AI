package com.devlaunch.service.interfaces;

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

}

package com.devlaunch.service.interfaces;

import com.devlaunch.dto.response.LeetCodeProfileResponse;

/**
 * Service interface for LeetCode profile retrieval operations.
 * <p>
 * Defines the contract for fetching public LeetCode user profile statistics
 * directly from the LeetCode GraphQL API. This service does not persist any
 * data in the local database; it retrieves live information from LeetCode
 * on each request. No authentication is required for accessing public
 * LeetCode profile data.
 * </p>
 *
 * @author DevLaunch
 */
public interface LeetCodeService {

    /**
     * Retrieves the public LeetCode profile for the specified username.
     * <p>
     * Fetches profile statistics from the LeetCode GraphQL API, including the
     * total number of problems solved, breakdown by difficulty (easy, medium,
     * hard), and the user's global ranking. If the specified username does
     * not exist on LeetCode, a
     * {@link com.devlaunch.exception.ResourceNotFoundException} is thrown.
     * </p>
     *
     * @param username the LeetCode username to look up (e.g. "leetcode_user")
     * @return the LeetCode profile statistics
     * @throws com.devlaunch.exception.ResourceNotFoundException if the LeetCode
     *                                                           user is not found
     */
    LeetCodeProfileResponse getLeetCodeProfile(String username);

}

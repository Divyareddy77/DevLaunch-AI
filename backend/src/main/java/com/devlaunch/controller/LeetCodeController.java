package com.devlaunch.controller;

import com.devlaunch.dto.response.LeetCodeProfileResponse;
import com.devlaunch.service.interfaces.LeetCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for LeetCode profile retrieval operations.
 * <p>
 * Exposes an endpoint for fetching public LeetCode user profile statistics
 * from the LeetCode GraphQL API. This endpoint does not require
 * authentication and operates without persisting any data in the local
 * database. All data is fetched live from LeetCode on each request.
 * </p>
 *
 * @author DevLaunch
 */
@RestController
@RequestMapping("/api/leetcode")
@RequiredArgsConstructor
public class LeetCodeController {

    private final LeetCodeService leetCodeService;

    /**
     * Retrieves the public LeetCode profile statistics for the specified
     * username.
     * <p>
     * Delegates to {@link LeetCodeService#getLeetCodeProfile(String)} to
     * fetch profile data including the total number of problems solved,
     * breakdown by difficulty (easy, medium, hard), and the user's global
     * ranking.
     * </p>
     *
     * @param username the LeetCode username to look up (e.g. "leetcode_user")
     * @return a {@link ResponseEntity} containing the LeetCode profile
     *         statistics with HTTP status 200 (OK)
     */
    @GetMapping("/{username}")
    public ResponseEntity<LeetCodeProfileResponse> getLeetCodeProfile(
            @PathVariable final String username) {
        LeetCodeProfileResponse response = leetCodeService.getLeetCodeProfile(username);
        return ResponseEntity.ok(response);
    }

}

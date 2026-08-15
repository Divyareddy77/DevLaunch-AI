package com.devlaunch.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response DTO for LeetCode user profile information.
 * <p>
 * Exposes public LeetCode profile statistics fetched from the LeetCode
 * GraphQL API, including the total number of problems solved, breakdown
 * by difficulty (easy, medium, hard), and the user's global ranking.
 * This data is retrieved live from LeetCode on each request and is not
 * persisted in the local database.
 * </p>
 *
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeetCodeProfileResponse {

    /**
     * The LeetCode username.
     */
    private String username;

    /**
     * Total number of problems solved by the user.
     */
    private Integer totalSolved;

    /**
     * Number of easy-difficulty problems solved.
     */
    private Integer easySolved;

    /**
     * Number of medium-difficulty problems solved.
     */
    private Integer mediumSolved;

    /**
     * Number of hard-difficulty problems solved.
     */
    private Integer hardSolved;

    /**
     * The user's global ranking on LeetCode.
     */
    private Integer ranking;

    /**
     * The user's overall acceptance rate (accepted submissions divided by
     * total submissions) as a percentage, or {@code null} if the value
     * could not be determined.
     */
    private Double acceptanceRate;

}

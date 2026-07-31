/**
 * Type definitions for the LeetCode Tracker module.
 *
 * Mirrors the backend response DTO in
 * com.devlaunch.dto.response.LeetCodeProfileResponse.
 *
 * @see backend/src/main/java/com/devlaunch/controller/LeetCodeController.java
 * @author DevLaunch
 */

/**
 * LeetCode user profile response from GET /api/leetcode/{username}.
 *
 * Contains public profile statistics fetched live from the LeetCode
 * GraphQL API by the backend.
 */
export interface LeetCodeProfileResponse {
  /** The LeetCode username. */
  username: string;

  /** Total number of problems solved by the user. */
  totalSolved: number;

  /** Number of easy-difficulty problems solved. */
  easySolved: number;

  /** Number of medium-difficulty problems solved. */
  mediumSolved: number;

  /** Number of hard-difficulty problems solved. */
  hardSolved: number;

  /** The user's global ranking on LeetCode, or null if unavailable. */
  ranking: number | null;
}

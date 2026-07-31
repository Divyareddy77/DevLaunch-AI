/**
 * LeetCode Tracker service — retrieval of public LeetCode profile
 * statistics.
 *
 * Communicates with the backend LeetCodeController. All data is
 * fetched live from the LeetCode GraphQL API by the backend.
 *
 * @see backend/src/main/java/com/devlaunch/controller/LeetCodeController.java
 * @author DevLaunch
 */

import apiClient from '../api/client';
import { LEETCODE } from '../api/endpoints';
import type { LeetCodeProfileResponse } from '../types/leetcode';

export const leetCodeService = {
  /** GET /api/leetcode/{username} — Fetch a public LeetCode user profile. */
  getProfile: (username: string) =>
    apiClient.get<LeetCodeProfileResponse>(LEETCODE.PROFILE(username)).then((r) => r.data),
};

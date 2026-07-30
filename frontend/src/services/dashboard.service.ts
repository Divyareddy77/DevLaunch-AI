/**
 * Dashboard service.
 *
 * Provides methods for fetching the aggregated dashboard summary
 * for the currently authenticated user.
 *
 * @see backend/src/main/java/com/devlaunch/controller/DashboardController.java
 * @author DevLaunch
 */

import apiClient from '../api/client';
import { DASHBOARD } from '../api/endpoints';
import type { DashboardResponse } from '../types/dashboard';

export const dashboardService = {
  /**
   * Retrieves the full achievement dashboard summary for the
   * currently authenticated user.
   *
   * GET /api/dashboard
   */
  getDashboard: () =>
    apiClient.get<DashboardResponse>(DASHBOARD).then((res) => res.data),
};

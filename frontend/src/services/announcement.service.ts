/**
 * Announcement service.
 *
 * Provides the user-facing announcement methods backed by the
 * authenticated GET /api/announcements/active endpoint. Admin-side
 * announcement management lives in admin.service.ts.
 *
 * @see backend/src/main/java/com/devlaunch/controller/AnnouncementController.java
 * @author DevLaunch
 */

import apiClient from '../api/client';
import { ANNOUNCEMENTS } from '../api/endpoints';
import type { Announcement } from '../types/announcement';

export const announcementService = {
  /**
   * Retrieves the active announcements visible to the authenticated user,
   * ordered newest first.
   *
   * GET /api/announcements/active
   */
  getActive: () =>
    apiClient.get<Announcement[]>(ANNOUNCEMENTS.ACTIVE).then((res) => res.data),
};

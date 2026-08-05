/**
 * Type definitions for user-facing announcements.
 *
 * Mirrors the backend AdminAnnouncementResponse DTO returned by the
 * public GET /api/announcements/active endpoint.
 *
 * @see backend/src/main/java/com/devlaunch/dto/response/AdminAnnouncementResponse.java
 * @author DevLaunch
 */

/** A platform announcement visible to users. */
export interface Announcement {
  id: number;
  /** The announcement headline. */
  title: string;
  /** The announcement body. */
  content: string;
  /** Whether the announcement is currently published. */
  isActive: boolean;
  createdById: number;
  createdByEmail: string;
  createdByName: string;
  /** ISO-8601 publication timestamp. */
  createdAt: string;
  updatedAt: string;
}

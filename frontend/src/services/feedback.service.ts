/**
 * Feedback service.
 *
 * Provides the user-facing feedback submission method backed by the
 * public POST /api/feedback endpoint. Admin-side feedback management
 * lives in admin.service.ts.
 *
 * @see backend/src/main/java/com/devlaunch/controller/FeedbackController.java
 * @author DevLaunch
 */

import apiClient from '../api/client';
import { FEEDBACK } from '../api/endpoints';
import type { FeedbackSubmissionRequest } from '../types/admin';

export const feedbackService = {
  /**
   * Submits platform feedback on behalf of the authenticated user.
   *
   * POST /api/feedback
   */
  submit: (data: FeedbackSubmissionRequest) => apiClient.post(FEEDBACK.SUBMIT, data),
};

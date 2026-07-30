/**
 * Service module for AI-powered features.
 *
 * Provides methods for interacting with the AI mock interview system
 * and resume review functionality. All calls go through the shared
 * Axios instance which handles JWT authentication automatically.
 *
 * @author DevLaunch
 */

import apiClient from '../api/client';
import { AI } from '../api/endpoints';
import type {
  StartInterviewRequest,
  StartInterviewResponse,
  SubmitInterviewRequest,
  SubmitInterviewResponse,
  InterviewHistoryResponse,
  ResumeReviewRequest,
  ResumeReviewResponse,
} from '../types/ai';

export const aiService = {
  /**
   * Starts a new mock interview session for the given category.
   * The backend generates a set of questions for the user to answer.
   */
  startInterview: (data: StartInterviewRequest) =>
    apiClient
      .post<StartInterviewResponse>(AI.START_INTERVIEW, data)
      .then((res) => res.data),

  /**
   * Subjects the user's answers for a given interview session to
   * AI analysis and returns structured feedback with scores.
   */
  submitInterview: (data: SubmitInterviewRequest) =>
    apiClient
      .post<SubmitInterviewResponse>(AI.SUBMIT_INTERVIEW, data)
      .then((res) => res.data),

  /**
   * Retrieves the authenticated user's past interview history,
   * including scores and metadata for each session.
   */
  getInterviewHistory: () =>
    apiClient
      .get<InterviewHistoryResponse>(AI.INTERVIEW_HISTORY)
      .then((res) => res.data),

  /**
   * Submits a resume for AI-powered review and returns suggestions
   * for improvement, missing keywords, and an overall score.
   */
  reviewResume: (data: ResumeReviewRequest) =>
    apiClient
      .post<ResumeReviewResponse>(AI.REVIEW_RESUME, data)
      .then((res) => res.data),
};

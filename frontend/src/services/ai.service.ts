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
  InterviewCategoryStats,
  ResumeReviewRequest,
  ResumeReviewResponse,
  TranscribeResponse,
} from '../types/ai';

/** Derives a Whisper-friendly filename from the recorded blob's MIME type. */
function audioFilename(blob: Blob): string {
  const type = blob.type.toLowerCase();
  if (type.includes('mp4')) {
    return 'answer.mp4';
  }
  if (type.includes('ogg')) {
    return 'answer.ogg';
  }
  if (type.includes('wav')) {
    return 'answer.wav';
  }
  return 'answer.webm';
}

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
   * Retrieves per-category statistics (bank size, best score, last
   * attempt) for the mock interview landing page.
   */
  getInterviewCategories: () =>
    apiClient
      .get<InterviewCategoryStats[]>(AI.INTERVIEW_CATEGORIES)
      .then((res) => res.data),

  /**
   * Deletes a completed interview session from the user's history.
   */
  deleteInterviewSession: (sessionId: string) =>
    apiClient
      .delete<string>(AI.INTERVIEW_HISTORY_BY_SESSION(sessionId))
      .then((res) => res.data),

  /**
   * Submits a resume for AI-powered review and returns suggestions
   * for improvement, missing keywords, and an overall score.
   */
  reviewResume: (data: ResumeReviewRequest) =>
    apiClient
      .post<ResumeReviewResponse>(AI.REVIEW_RESUME, data)
      .then((res) => res.data),

  /**
   * Transcribes a recorded voice answer through the backend Whisper
   * endpoint. The audio blob is uploaded as multipart form data together
   * with the client-tracked recording duration; the OpenAI API key never
   * leaves the server.
   */
  transcribe: (blob: Blob, durationSeconds: number) => {
    const formData = new FormData();
    formData.append('file', blob, audioFilename(blob));
    formData.append('duration', String(Math.round(durationSeconds)));
    return apiClient
      .post<TranscribeResponse>(AI.TRANSCRIBE, formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
        // Whisper transcription of a several-minute answer can take a while.
        timeout: 120_000,
      })
      .then((res) => res.data);
  },
};

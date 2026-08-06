package com.devlaunch.service.interfaces;

import com.devlaunch.dto.request.MockInterviewStartRequest;
import com.devlaunch.dto.request.MockInterviewSubmitRequest;
import com.devlaunch.dto.request.ResumeReviewRequest;
import com.devlaunch.dto.response.MockInterviewCategoryResponse;
import com.devlaunch.dto.response.MockInterviewFeedbackResponse;
import com.devlaunch.dto.response.MockInterviewHistoryResponse;
import com.devlaunch.dto.response.MockInterviewStartResponse;
import com.devlaunch.dto.response.ResumeReviewResponse;
import com.devlaunch.dto.response.TranscribeResponse;
import com.devlaunch.exception.ResourceNotFoundException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Service interface for AI-powered features.
 * <p>
 * Defines the contract for AI-driven operations: resume review (analysing
 * an existing resume owned by the authenticated user) and mock interviews
 * (generating practice questions, evaluating the user's answers, returning
 * their practice history, exposing per-category statistics, and allowing
 * sessions to be removed from the history).
 * </p>
 *
 * @author DevLaunch
 */
public interface AiService {

    /**
     * Reviews an existing resume owned by the currently authenticated user.
     * <p>
     * Loads the identified resume together with all of its sections,
     * delegates the analysis to the configured AI provider (falling back
     * to deterministic analysis when no provider is configured or fails),
     * and returns the structured review result.
     * </p>
     *
     * @param request the review request containing the resume ID and an
     *                optional target role
     * @return the structured resume review result
     * @throws ResourceNotFoundException if the resume is not found or does
     *                                   not belong to the authenticated user
     */
    ResumeReviewResponse reviewResume(ResumeReviewRequest request);

    /**
     * Starts a new mock interview session for the given configuration.
     * <p>
     * Generates a set of interview questions using the configured AI
     * provider (falling back to the deterministic question bank) and
     * returns them together with a session identifier used to submit the
     * answers later.
     * </p>
     *
     * @param request the start request containing the interview category,
     *                difficulty, question length, and timed flag
     * @return the generated questions and session identifier
     */
    MockInterviewStartResponse startMockInterview(MockInterviewStartRequest request);

    /**
     * Submits the answers of a mock interview session for AI evaluation.
     * <p>
     * Evaluates each answer with the configured AI provider (falling back
     * to deterministic heuristics), persists the completed session with
     * its full report to the user's interview history, and returns the
     * structured feedback.
     * </p>
     *
     * @param request the submit request containing the session identifier,
     *                category, configuration, and question/answer pairs
     * @return the structured interview feedback
     */
    MockInterviewFeedbackResponse submitMockInterview(MockInterviewSubmitRequest request);

    /**
     * Retrieves the authenticated user's completed interview history,
     * ordered most recent first, together with the aggregate statistics
     * that power the landing page and analytics views.
     *
     * @return the interview history with summary statistics
     */
    MockInterviewHistoryResponse getMockInterviewHistory();

    /**
     * Retrieves per-category statistics for the landing page: the question
     * bank size together with the authenticated user's practice history
     * for each category.
     *
     * @return the per-category statistics
     */
    List<MockInterviewCategoryResponse> getMockInterviewCategories();

    /**
     * Deletes a completed interview session from the authenticated user's
     * history.
     *
     * @param sessionId the client-generated session identifier to delete
     * @throws ResourceNotFoundException if the session is not found or does
     *                                   not belong to the authenticated user
     */
    void deleteMockInterview(String sessionId);

    /**
     * Transcribes a recorded voice answer through the configured speech-to-
     * text provider (OpenAI Whisper).
     * <p>
     * Validates the uploaded audio and delegates the transcription to the
     * shared Whisper client so the API key never leaves the backend. Returns
     * the transcribed text together with the audio duration used for
     * speaking analytics.
     * </p>
     *
     * @param file                  the recorded audio file
     * @param clientDurationSeconds the recording duration tracked by the
     *                              client, used as a fallback duration
     * @return the transcript and audio duration
     */
    TranscribeResponse transcribe(MultipartFile file, Integer clientDurationSeconds);

}

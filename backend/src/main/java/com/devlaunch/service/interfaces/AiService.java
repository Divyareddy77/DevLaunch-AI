package com.devlaunch.service.interfaces;

import com.devlaunch.dto.request.MockInterviewStartRequest;
import com.devlaunch.dto.request.MockInterviewSubmitRequest;
import com.devlaunch.dto.request.ResumeReviewRequest;
import com.devlaunch.dto.response.MockInterviewFeedbackResponse;
import com.devlaunch.dto.response.MockInterviewHistoryResponse;
import com.devlaunch.dto.response.MockInterviewStartResponse;
import com.devlaunch.dto.response.ResumeReviewResponse;
import com.devlaunch.exception.ResourceNotFoundException;

/**
 * Service interface for AI-powered features.
 * <p>
 * Defines the contract for AI-driven operations: resume review (analysing
 * an existing resume owned by the authenticated user) and mock interviews
 * (generating practice questions, evaluating the user's answers, and
 * returning their practice history).
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
     * Starts a new mock interview session for the given category.
     * <p>
     * Generates a set of interview questions using the configured AI
     * provider (falling back to the deterministic question bank) and
     * returns them together with a session identifier used to submit the
     * answers later.
     * </p>
     *
     * @param request the start request containing the interview category
     * @return the generated questions and session identifier
     */
    MockInterviewStartResponse startMockInterview(MockInterviewStartRequest request);

    /**
     * Submits the answers of a mock interview session for AI evaluation.
     * <p>
     * Evaluates each answer with the configured AI provider (falling back
     * to deterministic heuristics), persists the completed session to the
     * user's interview history, and returns the structured feedback.
     * </p>
     *
     * @param request the submit request containing the session identifier,
     *                category, and question/answer pairs
     * @return the structured interview feedback
     */
    MockInterviewFeedbackResponse submitMockInterview(MockInterviewSubmitRequest request);

    /**
     * Retrieves the authenticated user's completed interview history,
     * ordered most recent first.
     *
     * @return the interview history with summary statistics
     */
    MockInterviewHistoryResponse getMockInterviewHistory();

}

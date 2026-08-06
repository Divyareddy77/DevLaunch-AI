package com.devlaunch.controller;

import com.devlaunch.dto.request.MockInterviewStartRequest;
import com.devlaunch.dto.request.MockInterviewSubmitRequest;
import com.devlaunch.dto.request.ResumeReviewRequest;
import com.devlaunch.dto.response.MockInterviewCategoryResponse;
import com.devlaunch.dto.response.MockInterviewFeedbackResponse;
import com.devlaunch.dto.response.MockInterviewHistoryResponse;
import com.devlaunch.dto.response.MockInterviewStartResponse;
import com.devlaunch.dto.response.ResumeReviewResponse;
import com.devlaunch.dto.response.TranscribeResponse;
import com.devlaunch.service.interfaces.AiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * REST controller for AI-powered features.
 * <p>
 * Exposes endpoints for the AI module, supporting AI-powered resume
 * review and AI-powered mock interviews. All endpoints require a valid
 * JWT access token and operate exclusively on the authenticated user's
 * own data.
 * </p>
 *
 * @author DevLaunch
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    /**
     * Reviews an existing resume owned by the currently authenticated user.
     * <p>
     * Accepts the resume ID and an optional target role, validates the
     * input, and delegates the analysis to
     * {@link AiService#reviewResume(ResumeReviewRequest)}. Returns the
     * structured review containing quality and ATS scores, strengths,
     * weaknesses, missing skills, and improvement suggestions.
     * </p>
     *
     * @param request the review request containing the resume ID and an
     *                optional target role
     * @return a {@link ResponseEntity} containing the resume review data
     *         with HTTP status 200 (OK)
     */
    @PostMapping("/resume-review")
    public ResponseEntity<ResumeReviewResponse> reviewResume(
            @Valid @RequestBody final ResumeReviewRequest request) {
        ResumeReviewResponse response = aiService.reviewResume(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Starts a new mock interview session for the requested category.
     * <p>
     * Accepts the interview category, validates the input, and delegates
     * to {@link AiService#startMockInterview(MockInterviewStartRequest)}.
     * Returns the generated session identifier, category, and questions.
     * </p>
     *
     * @param request the start request containing the interview category
     * @return a {@link ResponseEntity} containing the generated questions
     *         with HTTP status 200 (OK)
     */
    @PostMapping("/mock-interview/questions")
    public ResponseEntity<MockInterviewStartResponse> startMockInterview(
            @Valid @RequestBody final MockInterviewStartRequest request) {
        MockInterviewStartResponse response = aiService.startMockInterview(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Submits the answers of a mock interview session for AI evaluation.
     * <p>
     * Accepts the session identifier, category, and question/answer pairs,
     * validates the input, and delegates to
     * {@link AiService#submitMockInterview(MockInterviewSubmitRequest)}.
     * Returns the structured feedback with scores, strengths, and areas
     * for improvement.
     * </p>
     *
     * @param request the submit request containing the session and answers
     * @return a {@link ResponseEntity} containing the interview feedback
     *         with HTTP status 200 (OK)
     */
    @PostMapping("/mock-interview/feedback")
    public ResponseEntity<MockInterviewFeedbackResponse> submitMockInterview(
            @Valid @RequestBody final MockInterviewSubmitRequest request) {
        MockInterviewFeedbackResponse response = aiService.submitMockInterview(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves the authenticated user's mock interview history.
     * <p>
     * Delegates to
     * {@link AiService#getMockInterviewHistory()} and returns the
     * completed interview sessions together with summary statistics.
     * </p>
     *
     * @return a {@link ResponseEntity} containing the interview history
     *         with HTTP status 200 (OK)
     */
    @GetMapping("/mock-interview/history")
    public ResponseEntity<MockInterviewHistoryResponse> getMockInterviewHistory() {
        MockInterviewHistoryResponse response = aiService.getMockInterviewHistory();
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves per-category statistics for the mock interview landing
     * page: the question bank size together with the authenticated user's
     * practice history for each category.
     *
     * @return a {@link ResponseEntity} containing the per-category
     *         statistics with HTTP status 200 (OK)
     */
    @GetMapping("/mock-interview/categories")
    public ResponseEntity<List<MockInterviewCategoryResponse>> getMockInterviewCategories() {
        List<MockInterviewCategoryResponse> response = aiService.getMockInterviewCategories();
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a completed interview session from the authenticated user's
     * history.
     *
     * @param sessionId the client-generated session identifier to delete
     * @return a {@link ResponseEntity} containing a success message
     *         with HTTP status 200 (OK)
     */
    @DeleteMapping("/mock-interview/history/{sessionId}")
    public ResponseEntity<String> deleteMockInterview(@PathVariable final String sessionId) {
        aiService.deleteMockInterview(sessionId);
        return ResponseEntity.ok("Interview session deleted successfully.");
    }

    /**
     * Transcribes a recorded voice answer using the configured speech-to-
     * text provider (OpenAI Whisper).
     * <p>
     * Accepts the recorded audio as {@code multipart/form-data} together
     * with an optional client-tracked recording duration, and returns the
     * transcribed text and audio duration. The OpenAI API key is never
     * exposed to the client — all transcription goes through this backend
     * endpoint.
     * </p>
     *
     * @param file                  the recorded audio file (multipart part "file")
     * @param clientDurationSeconds the recording duration tracked by the
     *                              client in seconds (optional, part "duration")
     * @return a {@link ResponseEntity} containing the transcript and duration
     *         with HTTP status 200 (OK)
     */
    @PostMapping(value = "/transcribe", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TranscribeResponse> transcribe(
            @RequestParam("file") final MultipartFile file,
            @RequestParam(value = "duration", required = false) final Integer clientDurationSeconds) {
        TranscribeResponse response = aiService.transcribe(file, clientDurationSeconds);
        return ResponseEntity.ok(response);
    }

}

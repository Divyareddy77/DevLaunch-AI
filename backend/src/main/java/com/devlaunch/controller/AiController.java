package com.devlaunch.controller;

import com.devlaunch.dto.request.ResumeReviewRequest;
import com.devlaunch.dto.response.ResumeReviewResponse;
import com.devlaunch.service.interfaces.AiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for AI-powered features.
 * <p>
 * Exposes endpoints for the AI module, currently supporting AI-powered
 * resume review. All endpoints require a valid JWT access token and
 * operate exclusively on the authenticated user's own data.
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

}

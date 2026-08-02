package com.devlaunch.service.interfaces;

import com.devlaunch.dto.request.ResumeReviewRequest;
import com.devlaunch.dto.response.ResumeReviewResponse;
import com.devlaunch.exception.ResourceNotFoundException;

/**
 * Service interface for AI-powered features.
 * <p>
 * Defines the contract for AI-driven analysis operations. The first
 * supported capability is resume review: analysing an existing resume
 * owned by the authenticated user and returning quality, ATS
 * compatibility, strengths, weaknesses, missing skills, and actionable
 * suggestions.
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

}

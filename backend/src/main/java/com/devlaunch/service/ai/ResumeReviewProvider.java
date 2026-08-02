package com.devlaunch.service.ai;

/**
 * Strategy contract for AI-powered resume review providers.
 * <p>
 * Implementations analyse a {@link ResumeContent} snapshot and return
 * a structured {@link ResumeReviewAnalysis}. The service layer selects
 * a provider based on {@link #isConfigured()}, preferring a real LLM
 * provider (e.g. OpenAI) when configured and falling back to a
 * deterministic provider otherwise, so the application remains fully
 * functional without an AI key.
 * </p>
 *
 * @author DevLaunch
 */
public interface ResumeReviewProvider {

    /**
     * Indicates whether this provider has been configured and is ready
     * to perform real analysis.
     *
     * @return {@code true} if the provider is configured, {@code false} otherwise
     */
    boolean isConfigured();

    /**
     * Analyses the given resume content and returns the review result.
     *
     * @param content    the structured resume content to analyse
     * @param targetRole the optional target job role to tailor the review
     *                   towards, or {@code null} for a generic review
     * @return the structured analysis result
     */
    ResumeReviewAnalysis analyze(ResumeContent content, String targetRole);

}

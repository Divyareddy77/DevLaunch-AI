package com.devlaunch.service.ai;

import com.devlaunch.entity.enums.InterviewType;

import java.util.List;

/**
 * Strategy contract for AI-powered mock interview providers.
 * <p>
 * Implementations generate a set of interview questions for a given
 * {@link InterviewType} and evaluate the user's answers, returning a
 * structured {@link MockInterviewFeedback}. The service layer selects
 * a provider based on {@link #isConfigured()}, preferring a real LLM
 * provider (e.g. OpenAI) when configured and falling back to a
 * deterministic provider otherwise, so the application remains fully
 * functional without an AI key.
 * </p>
 *
 * @author DevLaunch
 */
public interface MockInterviewProvider {

    /**
     * Indicates whether this provider has been configured and is ready
     * to perform real question generation and evaluation.
     *
     * @return {@code true} if the provider is configured, {@code false} otherwise
     */
    boolean isConfigured();

    /**
     * Generates a set of interview questions for the given category.
     *
     * @param type the interview category to generate questions for
     * @return the list of generated questions
     */
    List<InterviewQuestion> generateQuestions(InterviewType type);

    /**
     * Evaluates the user's answers and returns structured feedback.
     *
     * @param type    the interview category that was practised
     * @param answers the question/answer pairs to evaluate
     * @return the structured evaluation result
     */
    MockInterviewFeedback evaluate(InterviewType type, List<InterviewAnswer> answers);

}

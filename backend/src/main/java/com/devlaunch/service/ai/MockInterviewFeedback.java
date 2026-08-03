package com.devlaunch.service.ai;

import java.util.List;

/**
 * Internal evaluation result produced by a {@link MockInterviewProvider}.
 * <p>
 * This record is the provider-agnostic outcome of a mock interview
 * submission and is translated into the public
 * {@code MockInterviewFeedbackResponse} DTO by the AI service layer.
 * </p>
 *
 * @param overallScore        the overall interview score (0–100)
 * @param feedback            the per-question feedback breakdown
 * @param strengths           the strengths identified across all answers
 * @param areasForImprovement the areas for improvement across all answers
 * @author DevLaunch
 */
public record MockInterviewFeedback(
        int overallScore,
        List<Item> feedback,
        List<String> strengths,
        List<String> areasForImprovement) {

    /**
     * The AI feedback for a single interview answer.
     *
     * @param questionId  the identifier of the question
     * @param question    the question text
     * @param answer      the user's answer
     * @param score       the per-answer score (0–100)
     * @param feedback    the written assessment
     * @param suggestions specific suggestions for improvement
     */
    public record Item(String questionId, String question, String answer,
                       int score, String feedback, List<String> suggestions) {
    }

}

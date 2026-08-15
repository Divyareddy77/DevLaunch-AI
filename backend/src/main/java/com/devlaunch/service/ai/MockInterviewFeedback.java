package com.devlaunch.service.ai;

import java.util.List;

/**
 * Internal evaluation result produced by a {@link MockInterviewProvider}.
 * <p>
 * This record is the provider-agnostic outcome of a mock interview
 * submission and is translated into the public
 * {@code MockInterviewFeedbackResponse} DTO by the AI service layer. The
 * per-dimension scores (technical, communication, confidence, problem
 * solving, clarity, vocabulary, professionalism), the personalised
 * suggestions, and the missed concepts are all derived by the shared
 * {@link InterviewFeedbackMetrics} utility so no scoring logic is
 * duplicated across providers.
 * </p>
 *
 * @param overallScore          the overall interview score (0–100)
 * @param technicalScore        the technical knowledge score (0–100)
 * @param communicationScore    the communication score (0–100)
 * @param confidenceScore       the confidence estimate (0–100)
 * @param problemSolvingScore   the problem-solving score (0–100)
 * @param clarityScore          the answer clarity score (0–100)
 * @param vocabularyScore       the vocabulary breadth score (0–100)
 * @param professionalismScore  the professionalism score (0–100)
 * @param feedback              the per-question feedback breakdown
 * @param strengths             the strengths identified across all answers
 * @param areasForImprovement   the areas for improvement across all answers
 * @param suggestions           personalised practice suggestions
 * @param missedConcepts        the key concepts the answers did not cover
 * @author DevLaunch
 */
public record MockInterviewFeedback(
        int overallScore,
        int technicalScore,
        int communicationScore,
        int confidenceScore,
        int problemSolvingScore,
        int clarityScore,
        int vocabularyScore,
        int professionalismScore,
        List<Item> feedback,
        List<String> strengths,
        List<String> areasForImprovement,
        List<String> suggestions,
        List<String> missedConcepts) {

    /**
     * The AI feedback for a single interview answer.
     *
     * @param questionId     the identifier of the question
     * @param question       the question text
     * @param answer         the user's answer
     * @param score          the per-answer score (0–100)
     * @param feedback       the written assessment
     * @param suggestions    specific suggestions for improvement
     * @param improvedAnswer a sample improved answer, or {@code null} when
     *                       the answer already covers the key concepts
     */
    public record Item(String questionId, String question, String answer,
                       int score, String feedback, List<String> suggestions,
                       String improvedAnswer) {
    }

}

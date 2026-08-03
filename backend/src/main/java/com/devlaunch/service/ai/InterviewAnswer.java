package com.devlaunch.service.ai;

/**
 * A single question and the user's answer to it, submitted for evaluation.
 *
 * @param questionId the identifier of the question within the session
 * @param question   the question text the user was asked
 * @param answer     the user's answer to the question
 * @author DevLaunch
 */
public record InterviewAnswer(String questionId, String question, String answer) {
}

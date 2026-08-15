package com.devlaunch.service.ai;

import com.devlaunch.entity.enums.Difficulty;

/**
 * A single question generated for a mock interview session.
 *
 * @param id         the unique identifier of the question within the session
 * @param question   the question text presented to the user
 * @param hint       an optional hint to help the user structure their answer,
 *                   or {@code null} when no hint is provided
 * @param difficulty the difficulty of the question, or {@code null} when the
 *                   provider does not expose per-question difficulty
 * @author DevLaunch
 */
public record InterviewQuestion(String id, String question, String hint,
                                Difficulty difficulty) {
}

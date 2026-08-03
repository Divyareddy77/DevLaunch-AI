package com.devlaunch.service.ai;

/**
 * A single question generated for a mock interview session.
 *
 * @param id       the unique identifier of the question within the session
 * @param question the question text presented to the user
 * @param hint     an optional hint to help the user structure their answer,
 *                 or {@code null} when no hint is provided
 * @author DevLaunch
 */
public record InterviewQuestion(String id, String question, String hint) {
}

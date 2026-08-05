package com.devlaunch.entity.enums;

/**
 * Enumeration of interview question difficulty levels.
 * <p>
 * Each question in the database-backed interview question bank carries
 * one of these levels, allowing interviews to be assembled from a
 * balanced mix of easy, medium, and hard questions.
 * </p>
 *
 * @author DevLaunch
 */
public enum Difficulty {

    /**
     * A foundational question covering a core concept or definition.
     */
    EASY,

    /**
     * A question requiring explanation, comparison, or application of a
     * concept.
     */
    MEDIUM,

    /**
     * A question requiring deeper understanding, trade-off analysis, or
     * design reasoning.
     */
    HARD

}

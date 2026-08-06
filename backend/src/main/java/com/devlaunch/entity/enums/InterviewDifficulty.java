package com.devlaunch.entity.enums;

/**
 * Enumeration of difficulty modes for a mock interview session.
 * <p>
 * {@code EASY}, {@code MEDIUM}, and {@code HARD} restrict the session to
 * questions of a single difficulty, while {@code MIXED} (the default)
 * assembles a balanced mix of easy, medium, and hard questions so every
 * interview stays challenging regardless of the choice.
 * </p>
 *
 * @author DevLaunch
 */
public enum InterviewDifficulty {

    /**
     * A session restricted to easy questions.
     */
    EASY,

    /**
     * A session restricted to medium questions.
     */
    MEDIUM,

    /**
     * A session restricted to hard questions.
     */
    HARD,

    /**
     * A session with a balanced mix of easy, medium, and hard questions.
     */
    MIXED

}

package com.devlaunch.entity.enums;

/**
 * Enumeration of the supported mock interview categories.
 * <p>
 * Each value corresponds to a distinct interview track that the AI
 * mock interview module can simulate, from behavioural (HR) rounds
 * through to technology-specific tracks.
 * </p>
 *
 * @author DevLaunch
 */
public enum InterviewType {

    /**
     * A behavioural / HR-style interview round.
     */
    HR,

    /**
     * A core Java programming interview round.
     */
    JAVA,

    /**
     * A Spring Boot framework interview round.
     */
    SPRING_BOOT,

    /**
     * A SQL / database interview round.
     */
    SQL,

    /**
     * A React front-end interview round.
     */
    REACT

}

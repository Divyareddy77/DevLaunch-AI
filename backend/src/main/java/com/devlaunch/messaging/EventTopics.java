package com.devlaunch.messaging;

/**
 * Central constants for the DevLaunch RabbitMQ messaging backbone.
 * <p>
 * Every event is published to the single {@link #EXCHANGE} topic exchange
 * with a per-flow routing key and lands in the dedicated queue bound with
 * that key. Each queue has exactly one consumer, so a flow can be consumed,
 * retried, and dead-lettered independently of the others. Messages that
 * exhaust their retry budget are parked on the {@link #DLQ} via the
 * {@link #DLX} fan-out exchange.
 * </p>
 *
 * @author DevLaunch
 */
public final class EventTopics {

    /**
     * The topic exchange all DevLaunch events are published to.
     */
    public static final String EXCHANGE = "devlaunch.events";

    /**
     * Fan-out exchange that receives rejected messages and routes them to
     * the dead-letter queue.
     */
    public static final String DLX = "devlaunch.events.dlx";

    /**
     * The dead-letter queue holding messages that failed permanently.
     */
    public static final String DLQ = "devlaunch.events.dlq";

    /** Queue for the forgot-password email flow. */
    public static final String FORGOT_PASSWORD_EMAIL_QUEUE = "devlaunch.forgot-password-email";

    /** Routing key for the forgot-password email flow. */
    public static final String FORGOT_PASSWORD_EMAIL_KEY = "auth.forgot-password-email";

    /** Queue for the password-reset-success notification flow. */
    public static final String PASSWORD_RESET_SUCCESS_QUEUE = "devlaunch.password-reset-success";

    /** Routing key for the password-reset-success notification flow. */
    public static final String PASSWORD_RESET_SUCCESS_KEY = "auth.password-reset-success";

    /** Queue for the resume-review-completed notification flow. */
    public static final String RESUME_REVIEW_COMPLETED_QUEUE = "devlaunch.resume-review-completed";

    /** Routing key for the resume-review-completed notification flow. */
    public static final String RESUME_REVIEW_COMPLETED_KEY = "ai.resume-review-completed";

    /** Queue for the interview-reminder notification flow. */
    public static final String INTERVIEW_REMINDER_QUEUE = "devlaunch.interview-reminder";

    /** Routing key for the interview-reminder notification flow. */
    public static final String INTERVIEW_REMINDER_KEY = "reminder.interview";

    /** Queue for the study-reminder notification flow. */
    public static final String STUDY_REMINDER_QUEUE = "devlaunch.study-reminder";

    /** Routing key for the study-reminder notification flow. */
    public static final String STUDY_REMINDER_KEY = "reminder.study";

    /** Queue for the job-application-reminder notification flow. */
    public static final String JOB_APPLICATION_REMINDER_QUEUE = "devlaunch.job-application-reminder";

    /** Routing key for the job-application-reminder notification flow. */
    public static final String JOB_APPLICATION_REMINDER_KEY = "reminder.job-application";

    /** Queue for the mock-interview-completed flow. */
    public static final String MOCK_INTERVIEW_COMPLETED_QUEUE = "devlaunch.mock-interview-completed";

    /** Routing key for the mock-interview-completed flow. */
    public static final String MOCK_INTERVIEW_COMPLETED_KEY = "ai.mock-interview-completed";

    /** Queue for the GitHub-account-connected notification flow. */
    public static final String GITHUB_CONNECTED_QUEUE = "devlaunch.github-connected";

    /** Routing key for the GitHub-account-connected notification flow. */
    public static final String GITHUB_CONNECTED_KEY = "account.github-connected";

    /** Queue for the LeetCode-account-connected notification flow. */
    public static final String LEETCODE_CONNECTED_QUEUE = "devlaunch.leetcode-connected";

    /** Routing key for the LeetCode-account-connected notification flow. */
    public static final String LEETCODE_CONNECTED_KEY = "account.leetcode-connected";

    /** Queue for the placement-readiness milestone notification flow. */
    public static final String READINESS_MILESTONE_QUEUE = "devlaunch.readiness-milestone";

    /** Routing key for the placement-readiness milestone notification flow. */
    public static final String READINESS_MILESTONE_KEY = "dashboard.readiness-milestone";

    /** Queue for the gamification activity flow (XP + achievement evaluation). */
    public static final String ACHIEVEMENT_ACTIVITY_QUEUE = "devlaunch.achievement-activity";

    /** Routing key for the gamification activity flow. */
    public static final String ACHIEVEMENT_ACTIVITY_KEY = "gamification.activity";

    private EventTopics() {
        // Constants holder — never instantiated.
    }

}

package com.devlaunch.entity.enums;

/**
 * Enumeration of the priority levels a user can assign to a job application.
 * <p>
 * Lets users flag which opportunities deserve the most attention so the
 * tracker can surface high-priority applications at a glance.
 * </p>
 */
public enum ApplicationPriority {

    /**
     * Low priority — casual interest.
     */
    LOW,

    /**
     * Medium priority — the default level for new applications.
     */
    MEDIUM,

    /**
     * High priority — a preferred opportunity.
     */
    HIGH,

    /**
     * Urgent priority — needs immediate attention (deadline, follow-up).
     */
    URGENT

}

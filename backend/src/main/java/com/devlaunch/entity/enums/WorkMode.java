package com.devlaunch.entity.enums;

/**
 * Enumeration of the work modes for a job position.
 * <p>
 * Describes where the work for the position takes place. Stored on the
 * job application so users can filter and compare opportunities by
 * remote, hybrid, or onsite arrangements.
 * </p>
 */
public enum WorkMode {

    /**
     * The position is performed fully remotely.
     */
    REMOTE,

    /**
     * The position mixes remote work with in-office days.
     */
    HYBRID,

    /**
     * The position requires working on site.
     */
    ONSITE

}

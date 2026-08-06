package com.devlaunch.entity.enums;

/**
 * Enumeration of the attachment categories a user can attach to a job
 * application.
 * <p>
 * Categorises uploaded documents (e.g. the resume used, the cover letter,
 * an offer letter, or an assessment PDF) so attachments can be labelled
 * and filtered at a glance.
 * </p>
 */
public enum AttachmentCategory {

    /**
     * The resume used when applying.
     */
    RESUME,

    /**
     * The cover letter submitted with the application.
     */
    COVER_LETTER,

    /**
     * A received offer letter.
     */
    OFFER_LETTER,

    /**
     * An assessment or coding test document.
     */
    ASSESSMENT,

    /**
     * Interview feedback received from the employer.
     */
    INTERVIEW_FEEDBACK,

    /**
     * Any other supporting document.
     */
    OTHER

}

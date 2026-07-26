package com.devlaunch.service.interfaces;

import com.devlaunch.exception.ResourceNotFoundException;

/**
 * Service interface for resume PDF generation operations.
 * <p>
 * Defines the contract for generating a PDF document from a resume
 * owned by the currently authenticated user. The generated PDF is
 * returned as a byte array suitable for streaming to the client.
 * </p>
 *
 * @author DevLaunch
 */
public interface ResumePdfService {

    /**
     * Generates a PDF document for the specified resume.
     * <p>
     * Verifies that the resume exists and belongs to the currently
     * authenticated user before generating the PDF. The returned
     * byte array contains the complete PDF content.
     * </p>
     *
     * @param resumeId the ID of the resume to generate a PDF for
     * @return a byte array containing the generated PDF content
     * @throws ResourceNotFoundException if the resume is not found
     *                                    or does not belong to the user
     */
    byte[] generatePdf(Long resumeId);

}

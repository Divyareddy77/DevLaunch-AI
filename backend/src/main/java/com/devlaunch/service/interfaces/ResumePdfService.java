package com.devlaunch.service.interfaces;

import com.devlaunch.exception.ResourceNotFoundException;
import com.devlaunch.service.ResumePdfDocument;
import com.devlaunch.service.ResumePdfTemplate;

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
     * Generates a PDF document for the specified resume using the given
     * layout template.
     * <p>
     * Verifies that the resume exists and belongs to the currently
     * authenticated user before generating the PDF. The returned
     * document contains the complete PDF content together with the
     * suggested download file name. The resume content is identical for
     * every template; only the presentation changes.
     * </p>
     *
     * @param resumeId the ID of the resume to generate a PDF for
     * @param template the PDF layout template to apply
     * @return the generated PDF document and its download file name
     * @throws ResourceNotFoundException if the resume is not found
     *                                    or does not belong to the user
     */
    ResumePdfDocument generatePdf(Long resumeId, ResumePdfTemplate template);

}

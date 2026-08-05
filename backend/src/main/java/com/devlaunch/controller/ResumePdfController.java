package com.devlaunch.controller;

import com.devlaunch.service.ResumePdfDocument;
import com.devlaunch.service.ResumePdfTemplate;
import com.devlaunch.service.interfaces.ResumePdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for resume PDF generation operations.
 * <p>
 * Exposes an endpoint for downloading a PDF version of the specified
 * resume. All endpoints require a valid JWT access token and verify
 * that the target resume belongs to the authenticated user before
 * generating the PDF.
 * </p>
 *
 * @author DevLaunch
 */
@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
public class ResumePdfController {

    private final ResumePdfService resumePdfService;

    /**
     * Generates and downloads a PDF document for the specified resume.
     * <p>
     * Delegates PDF generation to
     * {@link ResumePdfService#generatePdf(Long, ResumePdfTemplate)}. The
     * resume must belong to the authenticated user. The generated PDF is
     * returned as a downloadable file named after the owner's first name
     * (e.g. {@code Divya_Resume.pdf}) with the correct HTTP headers for
     * PDF content.
     * </p>
     *
     * @param resumeId the ID of the resume to generate a PDF for
     * @param template the PDF layout template to apply (defaults to
     *                 {@code CLASSIC_PROFESSIONAL}); unknown values are
     *                 rejected with HTTP 400
     * @return a {@link ResponseEntity} containing the PDF byte array
     *         with HTTP status 200 (OK) and appropriate PDF headers
     */
    @GetMapping("/{resumeId}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable final Long resumeId,
            @RequestParam(defaultValue = "CLASSIC_PROFESSIONAL") final String template) {
        final ResumePdfDocument pdf =
                resumePdfService.generatePdf(resumeId, ResumePdfTemplate.from(template));

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                ContentDisposition.attachment().filename(pdf.fileName()).build());

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdf.content());
    }

}

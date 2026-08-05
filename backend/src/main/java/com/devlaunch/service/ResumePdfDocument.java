package com.devlaunch.service;

/**
 * Result of a resume PDF generation request.
 * <p>
 * Carries the generated PDF bytes together with the suggested download
 * file name (e.g. {@code Divya_Resume.pdf}) so the controller can build
 * a user-friendly {@code Content-Disposition} header without fetching
 * the resume a second time.
 * </p>
 *
 * @param content  the generated PDF document as a byte array
 * @param fileName the file name the client should use when saving the PDF
 * @author DevLaunch
 */
public record ResumePdfDocument(byte[] content, String fileName) {

}

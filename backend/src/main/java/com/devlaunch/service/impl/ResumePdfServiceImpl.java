package com.devlaunch.service.impl;

import com.devlaunch.entity.Resume;
import com.devlaunch.entity.User;
import com.devlaunch.exception.ResourceNotFoundException;
import com.devlaunch.repository.ResumeRepository;
import com.devlaunch.repository.UserRepository;
import com.devlaunch.service.interfaces.ResumePdfService;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;

/**
 * Implementation of {@link ResumePdfService} that generates a PDF
 * document for a resume owned by the currently authenticated user.
 * <p>
 * Uses OpenPDF ({@code com.lowagie.text}) to create the PDF document.
 * Ownership verification follows the same pattern established in
 * {@link ResumeServiceImpl} to ensure that the requesting user can
 * only access their own resume data.
 * </p>
 *
 * @author DevLaunch
 */
@Service
public class ResumePdfServiceImpl implements ResumePdfService {

    private static final Logger log = LoggerFactory.getLogger(ResumePdfServiceImpl.class);

    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;

    /**
     * Constructs the PDF generation service with the required dependencies.
     *
     * @param resumeRepository repository for resume data access
     * @param userRepository   repository for user data access
     */
    public ResumePdfServiceImpl(final ResumeRepository resumeRepository,
                                final UserRepository userRepository) {
        this.resumeRepository = resumeRepository;
        this.userRepository = userRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public byte[] generatePdf(final Long resumeId) {
        // Verify the resume exists and belongs to the authenticated user
        final Resume resume = getResumeOwnedByAuthenticatedUser(resumeId);
        log.info("Generating PDF for resume id={} owned by user email={}",
                resume.getId(), resume.getUser().getEmail());

        return createPdfContent();
    }

    /**
     * Creates a simple PDF document containing the placeholder text.
     * <p>
     * This is the initial implementation that generates a minimal PDF.
     * Resume content and template-specific layouts will be added in
     * subsequent iterations.
     * </p>
     *
     * @return a byte array containing the PDF document
     */
    private byte[] createPdfContent() {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final Document document = new Document();

        try {
            PdfWriter.getInstance(document, baos);
            document.open();
            document.add(new Paragraph("Hello DevLaunch PDF"));
            document.close();
        } catch (final DocumentException e) {
            log.error("Failed to generate PDF document", e);
            throw new RuntimeException("Failed to generate PDF document", e);
        }

        return baos.toByteArray();
    }

    /**
     * Retrieves the currently authenticated user from the database.
     * <p>
     * Extracts the username (email) from the {@link SecurityContextHolder},
     * fetches the corresponding {@link User} entity from the repository,
     * and throws a {@link ResourceNotFoundException} if no matching user
     * is found.
     * </p>
     *
     * @return the authenticated {@link User} entity
     * @throws ResourceNotFoundException if the user is not found in the database
     */
    private User getAuthenticatedUser() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with email " + email + " not found"));
    }

    /**
     * Retrieves a resume by ID and verifies it belongs to the currently
     * authenticated user.
     * <p>
     * Fetches the authenticated user first, then looks up the resume by ID.
     * Throws a {@link ResourceNotFoundException} if the resume does not exist
     * or if it belongs to a different user.
     * </p>
     *
     * @param id the resume ID to retrieve
     * @return the {@link Resume} entity owned by the authenticated user
     * @throws ResourceNotFoundException if the resume is not found or does not
     *                                   belong to the authenticated user
     */
    private Resume getResumeOwnedByAuthenticatedUser(final Long id) {
        final User user = getAuthenticatedUser();
        final Resume resume = resumeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Resume with id " + id + " not found"));

        // Verify ownership: the resume must belong to the authenticated user
        if (!resume.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException(
                    "Resume with id " + id + " not found for the authenticated user");
        }

        return resume;
    }

}

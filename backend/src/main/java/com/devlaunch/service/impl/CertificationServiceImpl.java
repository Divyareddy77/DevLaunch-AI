package com.devlaunch.service.impl;

import com.devlaunch.dto.request.CreateCertificationRequest;
import com.devlaunch.dto.request.UpdateCertificationRequest;
import com.devlaunch.dto.response.CertificationResponse;
import com.devlaunch.entity.Certification;
import com.devlaunch.entity.Resume;
import com.devlaunch.entity.User;
import com.devlaunch.exception.ResourceNotFoundException;
import com.devlaunch.mapper.CertificationMapper;
import com.devlaunch.repository.CertificationRepository;
import com.devlaunch.repository.ResumeRepository;
import com.devlaunch.repository.UserRepository;
import com.devlaunch.service.interfaces.CertificationService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of {@link CertificationService} providing certification record
 * creation, retrieval, update, and deletion operations scoped to a
 * specific resume owned by the currently authenticated user.
 * <p>
 * Uses the Spring Security {@link SecurityContextHolder} to obtain
 * the authenticated user's email, then delegates persistence and
 * mapping to {@link CertificationRepository} and {@link CertificationMapper}
 * respectively. Every operation verifies that the target resume
 * belongs to the authenticated user and that the certification record
 * belongs to that resume.
 * </p>
 *
 * @author DevLaunch
 */
@Service
public class CertificationServiceImpl implements CertificationService {

    private final CertificationRepository certificationRepository;
    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final CertificationMapper certificationMapper;

    /**
     * Constructs the certification service with the required dependencies.
     *
     * @param certificationRepository repository for certification data access
     * @param resumeRepository        repository for resume data access
     * @param userRepository          repository for user data access
     * @param certificationMapper     mapper for DTO-entity conversions
     */
    public CertificationServiceImpl(final CertificationRepository certificationRepository,
                                    final ResumeRepository resumeRepository,
                                    final UserRepository userRepository,
                                    final CertificationMapper certificationMapper) {
        this.certificationRepository = certificationRepository;
        this.resumeRepository = resumeRepository;
        this.userRepository = userRepository;
        this.certificationMapper = certificationMapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public CertificationResponse createCertification(final Long resumeId,
                                                     final CreateCertificationRequest request) {
        final Resume resume = getResumeOwnedByAuthenticatedUser(resumeId);

        // Map request DTO to a new Certification entity
        final Certification certification = certificationMapper.toCertification(request);

        // Associate the certification record with the verified resume
        certification.setResume(resume);

        // Persist the new certification record
        final Certification savedCertification = certificationRepository.save(certification);

        // Return the certification record data
        return certificationMapper.toCertificationResponse(savedCertification);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<CertificationResponse> getAllCertifications(final Long resumeId) {
        final Resume resume = getResumeOwnedByAuthenticatedUser(resumeId);
        final List<Certification> certifications = certificationRepository.findByResume(resume);
        return certifications.stream()
                .map(certificationMapper::toCertificationResponse)
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public CertificationResponse getCertificationById(final Long resumeId, final Long certificationId) {
        final Certification certification = getCertificationOwnedByResume(resumeId, certificationId);
        return certificationMapper.toCertificationResponse(certification);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public CertificationResponse updateCertification(final Long resumeId,
                                                     final Long certificationId,
                                                     final UpdateCertificationRequest request) {
        final Certification certification = getCertificationOwnedByResume(resumeId, certificationId);

        // Update the editable fields
        certification.setCertificationName(request.getCertificationName());
        certification.setIssuingOrganization(request.getIssuingOrganization());
        certification.setIssueDate(request.getIssueDate());
        certification.setExpiryDate(request.getExpiryDate());
        certification.setCredentialId(request.getCredentialId());
        certification.setCredentialUrl(request.getCredentialUrl());

        // Persist the updated certification record
        final Certification savedCertification = certificationRepository.save(certification);

        // Return the updated certification record data
        return certificationMapper.toCertificationResponse(savedCertification);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteCertification(final Long resumeId, final Long certificationId) {
        final Certification certification = getCertificationOwnedByResume(resumeId, certificationId);
        certificationRepository.delete(certification);
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
     * @param resumeId the resume ID to retrieve
     * @return the {@link Resume} entity owned by the authenticated user
     * @throws ResourceNotFoundException if the resume is not found or does not
     *                                   belong to the authenticated user
     */
    private Resume getResumeOwnedByAuthenticatedUser(final Long resumeId) {
        final User user = getAuthenticatedUser();
        final Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Resume with id " + resumeId + " not found"));

        // Verify ownership: the resume must belong to the authenticated user
        if (!resume.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException(
                    "Resume with id " + resumeId + " not found for the authenticated user");
        }

        return resume;
    }

    /**
     * Retrieves a certification record by ID and verifies it belongs to the
     * specified resume, which must itself belong to the currently authenticated
     * user.
     * <p>
     * First verifies the resume ownership, then looks up the certification
     * record by ID. Throws a {@link ResourceNotFoundException} if the
     * certification record does not exist or if it does not belong to the
     * specified resume.
     * </p>
     *
     * @param resumeId         the resume ID to verify ownership of
     * @param certificationId  the certification record ID to retrieve
     * @return the {@link Certification} entity belonging to the specified resume
     * @throws ResourceNotFoundException if the certification record is not found
     *                                   or does not belong to the specified resume
     */
    private Certification getCertificationOwnedByResume(final Long resumeId, final Long certificationId) {
        // Verifies the resume exists and belongs to the authenticated user
        getResumeOwnedByAuthenticatedUser(resumeId);

        final Certification certification = certificationRepository.findById(certificationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Certification with id " + certificationId + " not found"));

        // Verify the certification record belongs to the specified resume
        if (!certification.getResume().getId().equals(resumeId)) {
            throw new ResourceNotFoundException(
                    "Certification with id " + certificationId
                            + " not found for resume with id " + resumeId);
        }

        return certification;
    }

}

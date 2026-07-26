package com.devlaunch.service.impl;

import com.devlaunch.dto.request.CreateEducationRequest;
import com.devlaunch.dto.request.UpdateEducationRequest;
import com.devlaunch.dto.response.EducationResponse;
import com.devlaunch.entity.Education;
import com.devlaunch.entity.Resume;
import com.devlaunch.entity.User;
import com.devlaunch.exception.ResourceNotFoundException;
import com.devlaunch.mapper.EducationMapper;
import com.devlaunch.repository.EducationRepository;
import com.devlaunch.repository.ResumeRepository;
import com.devlaunch.repository.UserRepository;
import com.devlaunch.service.interfaces.EducationService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of {@link EducationService} providing education record
 * creation, retrieval, update, and deletion operations scoped to a
 * specific resume owned by the currently authenticated user.
 * <p>
 * Uses the Spring Security {@link SecurityContextHolder} to obtain
 * the authenticated user's email, then delegates persistence and
 * mapping to {@link EducationRepository} and {@link EducationMapper}
 * respectively. Every operation verifies that the target resume
 * belongs to the authenticated user and that the education record
 * belongs to that resume.
 * </p>
 *
 * @author DevLaunch
 */
@Service
public class EducationServiceImpl implements EducationService {

    private final EducationRepository educationRepository;
    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final EducationMapper educationMapper;

    /**
     * Constructs the education service with the required dependencies.
     *
     * @param educationRepository repository for education data access
     * @param resumeRepository    repository for resume data access
     * @param userRepository      repository for user data access
     * @param educationMapper     mapper for DTO-entity conversions
     */
    public EducationServiceImpl(final EducationRepository educationRepository,
                                final ResumeRepository resumeRepository,
                                final UserRepository userRepository,
                                final EducationMapper educationMapper) {
        this.educationRepository = educationRepository;
        this.resumeRepository = resumeRepository;
        this.userRepository = userRepository;
        this.educationMapper = educationMapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public EducationResponse createEducation(final Long resumeId,
                                             final CreateEducationRequest request) {
        final Resume resume = getResumeOwnedByAuthenticatedUser(resumeId);

        // Map request DTO to a new Education entity
        final Education education = educationMapper.toEducation(request);

        // Associate the education record with the verified resume
        education.setResume(resume);

        // Persist the new education record
        final Education savedEducation = educationRepository.save(education);

        // Return the education record data
        return educationMapper.toEducationResponse(savedEducation);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<EducationResponse> getAllEducations(final Long resumeId) {
        final Resume resume = getResumeOwnedByAuthenticatedUser(resumeId);
        final List<Education> educations = educationRepository.findByResume(resume);
        return educations.stream()
                .map(educationMapper::toEducationResponse)
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public EducationResponse getEducationById(final Long resumeId, final Long educationId) {
        final Education education = getEducationOwnedByResume(resumeId, educationId);
        return educationMapper.toEducationResponse(education);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public EducationResponse updateEducation(final Long resumeId,
                                             final Long educationId,
                                             final UpdateEducationRequest request) {
        final Education education = getEducationOwnedByResume(resumeId, educationId);

        // Update the editable fields
        education.setInstitutionName(request.getInstitutionName());
        education.setDegree(request.getDegree());
        education.setFieldOfStudy(request.getFieldOfStudy());
        education.setGrade(request.getGrade());
        education.setStartDate(request.getStartDate());
        education.setEndDate(request.getEndDate());
        education.setCurrentlyStudying(request.getCurrentlyStudying());
        education.setDescription(request.getDescription());

        // Persist the updated education record
        final Education savedEducation = educationRepository.save(education);

        // Return the updated education record data
        return educationMapper.toEducationResponse(savedEducation);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteEducation(final Long resumeId, final Long educationId) {
        final Education education = getEducationOwnedByResume(resumeId, educationId);
        educationRepository.delete(education);
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
     * Retrieves an education record by ID and verifies it belongs to the
     * specified resume, which must itself belong to the currently authenticated
     * user.
     * <p>
     * First verifies the resume ownership, then looks up the education
     * record by ID. Throws a {@link ResourceNotFoundException} if the
     * education record does not exist or if it does not belong to the
     * specified resume.
     * </p>
     *
     * @param resumeId    the resume ID to verify ownership of
     * @param educationId the education record ID to retrieve
     * @return the {@link Education} entity belonging to the specified resume
     * @throws ResourceNotFoundException if the education record is not found
     *                                   or does not belong to the specified resume
     */
    private Education getEducationOwnedByResume(final Long resumeId, final Long educationId) {
        // Verifies the resume exists and belongs to the authenticated user
        getResumeOwnedByAuthenticatedUser(resumeId);

        final Education education = educationRepository.findById(educationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Education with id " + educationId + " not found"));

        // Verify the education record belongs to the specified resume
        if (!education.getResume().getId().equals(resumeId)) {
            throw new ResourceNotFoundException(
                    "Education with id " + educationId
                            + " not found for resume with id " + resumeId);
        }

        return education;
    }

}

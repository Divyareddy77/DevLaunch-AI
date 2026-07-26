package com.devlaunch.service.impl;

import com.devlaunch.dto.request.CreateExperienceRequest;
import com.devlaunch.dto.request.UpdateExperienceRequest;
import com.devlaunch.dto.response.ExperienceResponse;
import com.devlaunch.entity.Experience;
import com.devlaunch.entity.Resume;
import com.devlaunch.entity.User;
import com.devlaunch.exception.ResourceNotFoundException;
import com.devlaunch.mapper.ExperienceMapper;
import com.devlaunch.repository.ExperienceRepository;
import com.devlaunch.repository.ResumeRepository;
import com.devlaunch.repository.UserRepository;
import com.devlaunch.service.interfaces.ExperienceService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of {@link ExperienceService} providing experience record
 * creation, retrieval, update, and deletion operations scoped to a
 * specific resume owned by the currently authenticated user.
 * <p>
 * Uses the Spring Security {@link SecurityContextHolder} to obtain
 * the authenticated user's email, then delegates persistence and
 * mapping to {@link ExperienceRepository} and {@link ExperienceMapper}
 * respectively. Every operation verifies that the target resume
 * belongs to the authenticated user and that the experience record
 * belongs to that resume.
 * </p>
 *
 * @author DevLaunch
 */
@Service
public class ExperienceServiceImpl implements ExperienceService {

    private final ExperienceRepository experienceRepository;
    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final ExperienceMapper experienceMapper;

    /**
     * Constructs the experience service with the required dependencies.
     *
     * @param experienceRepository repository for experience data access
     * @param resumeRepository     repository for resume data access
     * @param userRepository       repository for user data access
     * @param experienceMapper     mapper for DTO-entity conversions
     */
    public ExperienceServiceImpl(final ExperienceRepository experienceRepository,
                                 final ResumeRepository resumeRepository,
                                 final UserRepository userRepository,
                                 final ExperienceMapper experienceMapper) {
        this.experienceRepository = experienceRepository;
        this.resumeRepository = resumeRepository;
        this.userRepository = userRepository;
        this.experienceMapper = experienceMapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public ExperienceResponse createExperience(final Long resumeId,
                                               final CreateExperienceRequest request) {
        final Resume resume = getResumeOwnedByAuthenticatedUser(resumeId);

        // Map request DTO to a new Experience entity
        final Experience experience = experienceMapper.toExperience(request);

        // Associate the experience record with the verified resume
        experience.setResume(resume);

        // Persist the new experience record
        final Experience savedExperience = experienceRepository.save(experience);

        // Return the experience record data
        return experienceMapper.toExperienceResponse(savedExperience);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<ExperienceResponse> getAllExperiences(final Long resumeId) {
        final Resume resume = getResumeOwnedByAuthenticatedUser(resumeId);
        final List<Experience> experiences = experienceRepository.findByResume(resume);
        return experiences.stream()
                .map(experienceMapper::toExperienceResponse)
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public ExperienceResponse getExperienceById(final Long resumeId, final Long experienceId) {
        final Experience experience = getExperienceOwnedByResume(resumeId, experienceId);
        return experienceMapper.toExperienceResponse(experience);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public ExperienceResponse updateExperience(final Long resumeId,
                                               final Long experienceId,
                                               final UpdateExperienceRequest request) {
        final Experience experience = getExperienceOwnedByResume(resumeId, experienceId);

        // Update the editable fields
        experience.setCompanyName(request.getCompanyName());
        experience.setJobTitle(request.getJobTitle());
        experience.setEmploymentType(request.getEmploymentType());
        experience.setLocation(request.getLocation());
        experience.setStartDate(request.getStartDate());
        experience.setEndDate(request.getEndDate());
        experience.setCurrentlyWorking(request.getCurrentlyWorking());
        experience.setDescription(request.getDescription());

        // Persist the updated experience record
        final Experience savedExperience = experienceRepository.save(experience);

        // Return the updated experience record data
        return experienceMapper.toExperienceResponse(savedExperience);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteExperience(final Long resumeId, final Long experienceId) {
        final Experience experience = getExperienceOwnedByResume(resumeId, experienceId);
        experienceRepository.delete(experience);
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
     * Retrieves an experience record by ID and verifies it belongs to the
     * specified resume, which must itself belong to the currently authenticated
     * user.
     * <p>
     * First verifies the resume ownership, then looks up the experience
     * record by ID. Throws a {@link ResourceNotFoundException} if the
     * experience record does not exist or if it does not belong to the
     * specified resume.
     * </p>
     *
     * @param resumeId     the resume ID to verify ownership of
     * @param experienceId the experience record ID to retrieve
     * @return the {@link Experience} entity belonging to the specified resume
     * @throws ResourceNotFoundException if the experience record is not found
     *                                   or does not belong to the specified resume
     */
    private Experience getExperienceOwnedByResume(final Long resumeId, final Long experienceId) {
        // Verifies the resume exists and belongs to the authenticated user
        getResumeOwnedByAuthenticatedUser(resumeId);

        final Experience experience = experienceRepository.findById(experienceId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Experience with id " + experienceId + " not found"));

        // Verify the experience record belongs to the specified resume
        if (!experience.getResume().getId().equals(resumeId)) {
            throw new ResourceNotFoundException(
                    "Experience with id " + experienceId
                            + " not found for resume with id " + resumeId);
        }

        return experience;
    }

}

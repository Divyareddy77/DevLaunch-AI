package com.devlaunch.service.impl;

import com.devlaunch.dto.request.CreateResumeRequest;
import com.devlaunch.dto.request.UpdateResumeRequest;
import com.devlaunch.dto.response.ResumeResponse;
import com.devlaunch.dto.response.ResumeTemplateResponse;
import com.devlaunch.entity.Resume;
import com.devlaunch.entity.ResumeTemplate;
import com.devlaunch.entity.User;
import com.devlaunch.exception.ResourceNotFoundException;
import com.devlaunch.mapper.ResumeMapper;
import com.devlaunch.repository.ResumeRepository;
import com.devlaunch.repository.ResumeTemplateRepository;
import com.devlaunch.repository.UserRepository;
import com.devlaunch.service.interfaces.ResumeService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of {@link ResumeService} providing resume creation,
 * retrieval, update, deletion, and template assignment operations
 * for the currently authenticated user.
 * <p>
 * Uses the Spring Security {@link SecurityContextHolder} to obtain
 * the authenticated user's email, then delegates persistence and
 * mapping to {@link ResumeRepository} and {@link ResumeMapper}
 * respectively. A user may create multiple resumes. Update,
 * delete, and template assignment operations verify that the resume
 * belongs to the authenticated user.
 * </p>
 *
 * @author DevLaunch
 */
@Service
public class ResumeServiceImpl implements ResumeService {

    private final ResumeRepository resumeRepository;
    private final ResumeTemplateRepository resumeTemplateRepository;
    private final UserRepository userRepository;
    private final ResumeMapper resumeMapper;

    /**
     * Constructs the resume service with the required dependencies.
     *
     * @param resumeRepository         repository for resume data access
     * @param resumeTemplateRepository repository for resume template data access
     * @param userRepository           repository for user data access
     * @param resumeMapper             mapper for DTO-entity conversions
     */
    public ResumeServiceImpl(final ResumeRepository resumeRepository,
                             final ResumeTemplateRepository resumeTemplateRepository,
                             final UserRepository userRepository,
                             final ResumeMapper resumeMapper) {
        this.resumeRepository = resumeRepository;
        this.resumeTemplateRepository = resumeTemplateRepository;
        this.userRepository = userRepository;
        this.resumeMapper = resumeMapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public ResumeResponse createResume(final CreateResumeRequest request) {
        final User user = getAuthenticatedUser();

        // Map request DTO to a new Resume entity
        final Resume resume = resumeMapper.toResume(request);

        // Associate the resume with the authenticated user
        resume.setUser(user);

        // Persist the new resume
        final Resume savedResume = resumeRepository.save(resume);

        // Return the resume data
        return resumeMapper.toResumeResponse(savedResume);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<ResumeResponse> getAllResumes() {
        final User user = getAuthenticatedUser();
        final List<Resume> resumes = resumeRepository.findByUser(user);
        return resumes.stream()
                .map(resumeMapper::toResumeResponse)
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public ResumeResponse getResumeById(final Long id) {
        final Resume resume = getResumeOwnedByAuthenticatedUser(id);
        return resumeMapper.toResumeResponse(resume);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public ResumeResponse updateResume(final Long id, final UpdateResumeRequest request) {
        final Resume resume = getResumeOwnedByAuthenticatedUser(id);

        // Update the editable fields
        resume.setHeadline(request.getHeadline());
        resume.setSummary(request.getSummary());
        resume.setLinkedinUrl(request.getLinkedinUrl());
        resume.setGithubUrl(request.getGithubUrl());
        resume.setPortfolioUrl(request.getPortfolioUrl());

        // Persist the updated resume
        final Resume savedResume = resumeRepository.save(resume);

        // Return the updated resume data
        return resumeMapper.toResumeResponse(savedResume);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteResume(final Long id) {
        final Resume resume = getResumeOwnedByAuthenticatedUser(id);
        resumeRepository.delete(resume);
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
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public ResumeResponse assignTemplate(final Long resumeId, final Long templateId) {
        final Resume resume = getResumeOwnedByAuthenticatedUser(resumeId);

        // Fetch and verify the template exists
        final ResumeTemplate template = resumeTemplateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Resume template with id " + templateId + " not found"));

        // Assign the template to the resume
        resume.setTemplate(template);

        // Persist the updated resume
        final Resume savedResume = resumeRepository.save(resume);

        // Return the updated resume data
        return resumeMapper.toResumeResponse(savedResume);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public ResumeTemplateResponse getResumeTemplate(final Long resumeId) {
        final Resume resume = getResumeOwnedByAuthenticatedUser(resumeId);

        // Retrieve the template assigned to the resume, if any
        final ResumeTemplate template = resume.getTemplate();

        if (template == null) {
            return null;
        }

        return resumeMapper.toResumeTemplateResponse(template);
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

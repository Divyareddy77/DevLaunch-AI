package com.devlaunch.service.impl;

import com.devlaunch.dto.request.CreateProjectRequest;
import com.devlaunch.dto.request.UpdateProjectRequest;
import com.devlaunch.dto.response.ProjectResponse;
import com.devlaunch.entity.Project;
import com.devlaunch.entity.Resume;
import com.devlaunch.entity.User;
import com.devlaunch.exception.ResourceNotFoundException;
import com.devlaunch.mapper.ProjectMapper;
import com.devlaunch.repository.ProjectRepository;
import com.devlaunch.repository.ResumeRepository;
import com.devlaunch.repository.UserRepository;
import com.devlaunch.service.interfaces.ProjectService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of {@link ProjectService} providing project record
 * creation, retrieval, update, and deletion operations scoped to a
 * specific resume owned by the currently authenticated user.
 * <p>
 * Uses the Spring Security {@link SecurityContextHolder} to obtain
 * the authenticated user's email, then delegates persistence and
 * mapping to {@link ProjectRepository} and {@link ProjectMapper}
 * respectively. Every operation verifies that the target resume
 * belongs to the authenticated user and that the project record
 * belongs to that resume.
 * </p>
 *
 * @author DevLaunch
 */
@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final ProjectMapper projectMapper;

    /**
     * Constructs the project service with the required dependencies.
     *
     * @param projectRepository repository for project data access
     * @param resumeRepository  repository for resume data access
     * @param userRepository    repository for user data access
     * @param projectMapper     mapper for DTO-entity conversions
     */
    public ProjectServiceImpl(final ProjectRepository projectRepository,
                              final ResumeRepository resumeRepository,
                              final UserRepository userRepository,
                              final ProjectMapper projectMapper) {
        this.projectRepository = projectRepository;
        this.resumeRepository = resumeRepository;
        this.userRepository = userRepository;
        this.projectMapper = projectMapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public ProjectResponse createProject(final Long resumeId,
                                         final CreateProjectRequest request) {
        final Resume resume = getResumeOwnedByAuthenticatedUser(resumeId);

        // Map request DTO to a new Project entity
        final Project project = projectMapper.toProject(request);

        // Associate the project record with the verified resume
        project.setResume(resume);

        // Persist the new project record
        final Project savedProject = projectRepository.save(project);

        // Return the project record data
        return projectMapper.toProjectResponse(savedProject);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponse> getAllProjects(final Long resumeId) {
        final Resume resume = getResumeOwnedByAuthenticatedUser(resumeId);
        final List<Project> projects = projectRepository.findByResume(resume);
        return projects.stream()
                .map(projectMapper::toProjectResponse)
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(final Long resumeId, final Long projectId) {
        final Project project = getProjectOwnedByResume(resumeId, projectId);
        return projectMapper.toProjectResponse(project);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public ProjectResponse updateProject(final Long resumeId,
                                         final Long projectId,
                                         final UpdateProjectRequest request) {
        final Project project = getProjectOwnedByResume(resumeId, projectId);

        // Update the editable fields
        project.setProjectName(request.getProjectName());
        project.setDescription(request.getDescription());
        project.setTechnologies(request.getTechnologies());
        project.setGithubUrl(request.getGithubUrl());
        project.setLiveUrl(request.getLiveUrl());
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        project.setCurrentlyWorking(request.getCurrentlyWorking());

        // Persist the updated project record
        final Project savedProject = projectRepository.save(project);

        // Return the updated project record data
        return projectMapper.toProjectResponse(savedProject);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteProject(final Long resumeId, final Long projectId) {
        final Project project = getProjectOwnedByResume(resumeId, projectId);
        projectRepository.delete(project);
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
     * Retrieves a project record by ID and verifies it belongs to the
     * specified resume, which must itself belong to the currently authenticated
     * user.
     * <p>
     * First verifies the resume ownership, then looks up the project
     * record by ID. Throws a {@link ResourceNotFoundException} if the
     * project record does not exist or if it does not belong to the
     * specified resume.
     * </p>
     *
     * @param resumeId  the resume ID to verify ownership of
     * @param projectId the project record ID to retrieve
     * @return the {@link Project} entity belonging to the specified resume
     * @throws ResourceNotFoundException if the project record is not found
     *                                   or does not belong to the specified resume
     */
    private Project getProjectOwnedByResume(final Long resumeId, final Long projectId) {
        // Verifies the resume exists and belongs to the authenticated user
        getResumeOwnedByAuthenticatedUser(resumeId);

        final Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Project with id " + projectId + " not found"));

        // Verify the project record belongs to the specified resume
        if (!project.getResume().getId().equals(resumeId)) {
            throw new ResourceNotFoundException(
                    "Project with id " + projectId
                            + " not found for resume with id " + resumeId);
        }

        return project;
    }

}

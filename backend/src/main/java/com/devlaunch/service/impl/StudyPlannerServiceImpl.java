package com.devlaunch.service.impl;

import com.devlaunch.dto.request.CreateStudyPlannerRequest;
import com.devlaunch.dto.request.UpdateStudyPlannerRequest;
import com.devlaunch.dto.response.StudyPlannerResponse;
import com.devlaunch.entity.StudyPlanner;
import com.devlaunch.entity.User;
import com.devlaunch.exception.ResourceNotFoundException;
import com.devlaunch.mapper.StudyPlannerMapper;
import com.devlaunch.repository.StudyPlannerRepository;
import com.devlaunch.repository.UserRepository;
import com.devlaunch.service.interfaces.StudyPlannerService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of {@link StudyPlannerService} providing study planner
 * creation, retrieval, update, and deletion operations for the currently
 * authenticated user.
 * <p>
 * Uses the Spring Security {@link SecurityContextHolder} to obtain
 * the authenticated user's email, then delegates persistence and
 * mapping to {@link StudyPlannerRepository} and {@link StudyPlannerMapper}
 * respectively. A user may have many study planner entries. Update and
 * delete operations verify that the entry belongs to the authenticated user.
 * </p>
 *
 * @author DevLaunch
 */
@Service
public class StudyPlannerServiceImpl implements StudyPlannerService {

    private final StudyPlannerRepository studyPlannerRepository;
    private final UserRepository userRepository;
    private final StudyPlannerMapper studyPlannerMapper;

    /**
     * Constructs the study planner service with the required dependencies.
     *
     * @param studyPlannerRepository repository for study planner data access
     * @param userRepository         repository for user data access
     * @param studyPlannerMapper     mapper for DTO-entity conversions
     */
    public StudyPlannerServiceImpl(final StudyPlannerRepository studyPlannerRepository,
                                   final UserRepository userRepository,
                                   final StudyPlannerMapper studyPlannerMapper) {
        this.studyPlannerRepository = studyPlannerRepository;
        this.userRepository = userRepository;
        this.studyPlannerMapper = studyPlannerMapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public StudyPlannerResponse createStudyPlanner(final CreateStudyPlannerRequest request) {
        final User user = getAuthenticatedUser();

        // Map request DTO to a new StudyPlanner entity
        final StudyPlanner studyPlanner = studyPlannerMapper.toStudyPlanner(request);

        // Associate the study planner entry with the authenticated user
        studyPlanner.setUser(user);

        // Persist the new study planner entry
        final StudyPlanner savedStudyPlanner = studyPlannerRepository.save(studyPlanner);

        // Return the study planner data
        return studyPlannerMapper.toStudyPlannerResponse(savedStudyPlanner);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<StudyPlannerResponse> getAllStudyPlanners() {
        final User user = getAuthenticatedUser();
        final List<StudyPlanner> studyPlanners = studyPlannerRepository.findByUser(user);
        return studyPlanners.stream()
                .map(studyPlannerMapper::toStudyPlannerResponse)
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public StudyPlannerResponse getStudyPlannerById(final Long id) {
        final StudyPlanner studyPlanner = getStudyPlannerOwnedByAuthenticatedUser(id);
        return studyPlannerMapper.toStudyPlannerResponse(studyPlanner);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public StudyPlannerResponse updateStudyPlanner(final Long id,
                                                   final UpdateStudyPlannerRequest request) {
        final StudyPlanner studyPlanner = getStudyPlannerOwnedByAuthenticatedUser(id);

        // Update the editable fields
        studyPlanner.setTitle(request.getTitle());
        studyPlanner.setDescription(request.getDescription());
        studyPlanner.setStudyDate(request.getStudyDate());
        studyPlanner.setStartTime(request.getStartTime());
        studyPlanner.setEndTime(request.getEndTime());
        studyPlanner.setPriority(request.getPriority());
        studyPlanner.setStatus(request.getStatus());

        // Persist the updated study planner entry
        final StudyPlanner savedStudyPlanner = studyPlannerRepository.save(studyPlanner);

        // Return the updated study planner data
        return studyPlannerMapper.toStudyPlannerResponse(savedStudyPlanner);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteStudyPlanner(final Long id) {
        final StudyPlanner studyPlanner = getStudyPlannerOwnedByAuthenticatedUser(id);
        studyPlannerRepository.delete(studyPlanner);
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
     * Retrieves a study planner entry by ID and verifies it belongs to the currently
     * authenticated user.
     * <p>
     * Fetches the authenticated user first, then looks up the study planner entry by ID.
     * Throws a {@link ResourceNotFoundException} if the study planner entry does not exist
     * or if it belongs to a different user.
     * </p>
     *
     * @param id the study planner entry ID to retrieve
     * @return the {@link StudyPlanner} entity owned by the authenticated user
     * @throws ResourceNotFoundException if the study planner entry is not found or does not
     *                                   belong to the authenticated user
     */
    private StudyPlanner getStudyPlannerOwnedByAuthenticatedUser(final Long id) {
        final User user = getAuthenticatedUser();
        final StudyPlanner studyPlanner = studyPlannerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Study planner with id " + id + " not found"));

        // Verify ownership: the study planner entry must belong to the authenticated user
        if (!studyPlanner.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException(
                    "Study planner with id " + id + " not found for the authenticated user");
        }

        return studyPlanner;
    }

}

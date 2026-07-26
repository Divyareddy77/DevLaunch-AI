package com.devlaunch.service.impl;

import com.devlaunch.dto.request.CreateAchievementRequest;
import com.devlaunch.dto.request.UpdateAchievementRequest;
import com.devlaunch.dto.response.AchievementResponse;
import com.devlaunch.entity.Achievement;
import com.devlaunch.entity.Resume;
import com.devlaunch.entity.User;
import com.devlaunch.exception.ResourceNotFoundException;
import com.devlaunch.mapper.AchievementMapper;
import com.devlaunch.repository.AchievementRepository;
import com.devlaunch.repository.ResumeRepository;
import com.devlaunch.repository.UserRepository;
import com.devlaunch.service.interfaces.AchievementService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of {@link AchievementService} providing achievement record
 * creation, retrieval, update, and deletion operations scoped to a
 * specific resume owned by the currently authenticated user.
 * <p>
 * Uses the Spring Security {@link SecurityContextHolder} to obtain
 * the authenticated user's email, then delegates persistence and
 * mapping to {@link AchievementRepository} and {@link AchievementMapper}
 * respectively. Every operation verifies that the target resume
 * belongs to the authenticated user and that the achievement record
 * belongs to that resume.
 * </p>
 *
 * @author DevLaunch
 */
@Service
public class AchievementServiceImpl implements AchievementService {

    private final AchievementRepository achievementRepository;
    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final AchievementMapper achievementMapper;

    /**
     * Constructs the achievement service with the required dependencies.
     *
     * @param achievementRepository repository for achievement data access
     * @param resumeRepository      repository for resume data access
     * @param userRepository        repository for user data access
     * @param achievementMapper     mapper for DTO-entity conversions
     */
    public AchievementServiceImpl(final AchievementRepository achievementRepository,
                                  final ResumeRepository resumeRepository,
                                  final UserRepository userRepository,
                                  final AchievementMapper achievementMapper) {
        this.achievementRepository = achievementRepository;
        this.resumeRepository = resumeRepository;
        this.userRepository = userRepository;
        this.achievementMapper = achievementMapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public AchievementResponse createAchievement(final Long resumeId,
                                                 final CreateAchievementRequest request) {
        final Resume resume = getResumeOwnedByAuthenticatedUser(resumeId);

        // Map request DTO to a new Achievement entity
        final Achievement achievement = achievementMapper.toAchievement(request);

        // Associate the achievement record with the verified resume
        achievement.setResume(resume);

        // Persist the new achievement record
        final Achievement savedAchievement = achievementRepository.save(achievement);

        // Return the achievement record data
        return achievementMapper.toAchievementResponse(savedAchievement);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<AchievementResponse> getAllAchievements(final Long resumeId) {
        final Resume resume = getResumeOwnedByAuthenticatedUser(resumeId);
        final List<Achievement> achievements = achievementRepository.findByResume(resume);
        return achievements.stream()
                .map(achievementMapper::toAchievementResponse)
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public AchievementResponse getAchievementById(final Long resumeId, final Long achievementId) {
        final Achievement achievement = getAchievementOwnedByResume(resumeId, achievementId);
        return achievementMapper.toAchievementResponse(achievement);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public AchievementResponse updateAchievement(final Long resumeId,
                                                 final Long achievementId,
                                                 final UpdateAchievementRequest request) {
        final Achievement achievement = getAchievementOwnedByResume(resumeId, achievementId);

        // Update the editable fields
        achievement.setTitle(request.getTitle());
        achievement.setDescription(request.getDescription());
        achievement.setDateAchieved(request.getDateAchieved());

        // Persist the updated achievement record
        final Achievement savedAchievement = achievementRepository.save(achievement);

        // Return the updated achievement record data
        return achievementMapper.toAchievementResponse(savedAchievement);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteAchievement(final Long resumeId, final Long achievementId) {
        final Achievement achievement = getAchievementOwnedByResume(resumeId, achievementId);
        achievementRepository.delete(achievement);
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
     * Retrieves an achievement record by ID and verifies it belongs to the
     * specified resume, which must itself belong to the currently authenticated
     * user.
     * <p>
     * First verifies the resume ownership, then looks up the achievement
     * record by ID. Throws a {@link ResourceNotFoundException} if the
     * achievement record does not exist or if it does not belong to the
     * specified resume.
     * </p>
     *
     * @param resumeId       the resume ID to verify ownership of
     * @param achievementId  the achievement record ID to retrieve
     * @return the {@link Achievement} entity belonging to the specified resume
     * @throws ResourceNotFoundException if the achievement record is not found
     *                                   or does not belong to the specified resume
     */
    private Achievement getAchievementOwnedByResume(final Long resumeId, final Long achievementId) {
        // Verifies the resume exists and belongs to the authenticated user
        getResumeOwnedByAuthenticatedUser(resumeId);

        final Achievement achievement = achievementRepository.findById(achievementId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Achievement with id " + achievementId + " not found"));

        // Verify the achievement record belongs to the specified resume
        if (!achievement.getResume().getId().equals(resumeId)) {
            throw new ResourceNotFoundException(
                    "Achievement with id " + achievementId
                            + " not found for resume with id " + resumeId);
        }

        return achievement;
    }

}

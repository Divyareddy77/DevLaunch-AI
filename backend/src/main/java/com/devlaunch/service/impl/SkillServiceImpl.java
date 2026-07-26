package com.devlaunch.service.impl;

import com.devlaunch.dto.request.CreateSkillRequest;
import com.devlaunch.dto.request.UpdateSkillRequest;
import com.devlaunch.dto.response.SkillResponse;
import com.devlaunch.entity.Resume;
import com.devlaunch.entity.Skill;
import com.devlaunch.entity.User;
import com.devlaunch.exception.ResourceNotFoundException;
import com.devlaunch.mapper.SkillMapper;
import com.devlaunch.repository.ResumeRepository;
import com.devlaunch.repository.SkillRepository;
import com.devlaunch.repository.UserRepository;
import com.devlaunch.service.interfaces.SkillService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of {@link SkillService} providing skill record
 * creation, retrieval, update, and deletion operations scoped to a
 * specific resume owned by the currently authenticated user.
 * <p>
 * Uses the Spring Security {@link SecurityContextHolder} to obtain
 * the authenticated user's email, then delegates persistence and
 * mapping to {@link SkillRepository} and {@link SkillMapper}
 * respectively. Every operation verifies that the target resume
 * belongs to the authenticated user and that the skill record
 * belongs to that resume.
 * </p>
 *
 * @author DevLaunch
 */
@Service
public class SkillServiceImpl implements SkillService {

    private final SkillRepository skillRepository;
    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final SkillMapper skillMapper;

    /**
     * Constructs the skill service with the required dependencies.
     *
     * @param skillRepository repository for skill data access
     * @param resumeRepository repository for resume data access
     * @param userRepository   repository for user data access
     * @param skillMapper      mapper for DTO-entity conversions
     */
    public SkillServiceImpl(final SkillRepository skillRepository,
                            final ResumeRepository resumeRepository,
                            final UserRepository userRepository,
                            final SkillMapper skillMapper) {
        this.skillRepository = skillRepository;
        this.resumeRepository = resumeRepository;
        this.userRepository = userRepository;
        this.skillMapper = skillMapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public SkillResponse createSkill(final Long resumeId,
                                     final CreateSkillRequest request) {
        final Resume resume = getResumeOwnedByAuthenticatedUser(resumeId);

        // Map request DTO to a new Skill entity
        final Skill skill = skillMapper.toSkill(request);

        // Associate the skill record with the verified resume
        skill.setResume(resume);

        // Persist the new skill record
        final Skill savedSkill = skillRepository.save(skill);

        // Return the skill record data
        return skillMapper.toSkillResponse(savedSkill);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<SkillResponse> getAllSkills(final Long resumeId) {
        final Resume resume = getResumeOwnedByAuthenticatedUser(resumeId);
        final List<Skill> skills = skillRepository.findByResume(resume);
        return skills.stream()
                .map(skillMapper::toSkillResponse)
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public SkillResponse getSkillById(final Long resumeId, final Long skillId) {
        final Skill skill = getSkillOwnedByResume(resumeId, skillId);
        return skillMapper.toSkillResponse(skill);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public SkillResponse updateSkill(final Long resumeId,
                                     final Long skillId,
                                     final UpdateSkillRequest request) {
        final Skill skill = getSkillOwnedByResume(resumeId, skillId);

        // Update the editable fields
        skill.setSkillName(request.getSkillName());
        skill.setProficiency(request.getProficiency());

        // Persist the updated skill record
        final Skill savedSkill = skillRepository.save(skill);

        // Return the updated skill record data
        return skillMapper.toSkillResponse(savedSkill);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteSkill(final Long resumeId, final Long skillId) {
        final Skill skill = getSkillOwnedByResume(resumeId, skillId);
        skillRepository.delete(skill);
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
     * Retrieves a skill record by ID and verifies it belongs to the
     * specified resume, which must itself belong to the currently authenticated
     * user.
     * <p>
     * First verifies the resume ownership, then looks up the skill
     * record by ID. Throws a {@link ResourceNotFoundException} if the
     * skill record does not exist or if it does not belong to the
     * specified resume.
     * </p>
     *
     * @param resumeId the resume ID to verify ownership of
     * @param skillId  the skill record ID to retrieve
     * @return the {@link Skill} entity belonging to the specified resume
     * @throws ResourceNotFoundException if the skill record is not found
     *                                   or does not belong to the specified resume
     */
    private Skill getSkillOwnedByResume(final Long resumeId, final Long skillId) {
        // Verifies the resume exists and belongs to the authenticated user
        getResumeOwnedByAuthenticatedUser(resumeId);

        final Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Skill with id " + skillId + " not found"));

        // Verify the skill record belongs to the specified resume
        if (!skill.getResume().getId().equals(resumeId)) {
            throw new ResourceNotFoundException(
                    "Skill with id " + skillId
                            + " not found for resume with id " + resumeId);
        }

        return skill;
    }

}

package com.devlaunch.service.impl;

import com.devlaunch.dto.request.ChangePasswordRequest;
import com.devlaunch.dto.request.UpdateUserRequest;
import com.devlaunch.dto.response.UserResponse;
import com.devlaunch.cache.CacheNames;
import com.devlaunch.entity.User;
import com.devlaunch.entity.enums.ActivityType;
import com.devlaunch.entity.enums.NotificationType;
import com.devlaunch.exception.ResourceNotFoundException;
import com.devlaunch.mapper.AuthMapper;
import com.devlaunch.messaging.EventPublisher;
import com.devlaunch.messaging.EventTopics;
import com.devlaunch.messaging.event.ActivityEvent;
import com.devlaunch.messaging.event.NotificationEvent;
import com.devlaunch.repository.UserRepository;
import com.devlaunch.service.interfaces.UserService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Implementation of {@link UserService} providing user profile
 * retrieval, update, and password change operations for the
 * currently authenticated user.
 * <p>
 * Uses the Spring Security {@link SecurityContextHolder} to obtain
 * the authenticated user's email, then delegates persistence and
 * mapping to {@link UserRepository} and {@link AuthMapper}
 * respectively.
 * </p>
 *
 * @author DevLaunch
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AuthMapper authMapper;
    private final PasswordEncoder passwordEncoder;
    private final EventPublisher eventPublisher;

    /**
     * Constructs the user service with the required dependencies.
     *
     * @param userRepository  repository for user data access
     * @param authMapper      mapper for entity-to-DTO conversion
     * @param passwordEncoder encoder for hashing user passwords
     * @param eventPublisher  publisher for the messaging backbone
     */
    public UserServiceImpl(final UserRepository userRepository,
                           final AuthMapper authMapper,
                           final PasswordEncoder passwordEncoder,
                           final EventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.authMapper = authMapper;
        this.passwordEncoder = passwordEncoder;
        this.eventPublisher = eventPublisher;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        final User user = getAuthenticatedUser();
        return authMapper.toUserResponse(user);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public UserResponse updateCurrentUser(final UpdateUserRequest request) {
        final User user = getAuthenticatedUser();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        final User savedUser = userRepository.save(user);
        return authMapper.toUserResponse(savedUser);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.DASHBOARD),
            // GitHub/LeetCode profiles are cached by profile username, so the
            // freshly connected account's entry is dropped for immediate freshness.
            @CacheEvict(cacheNames = CacheNames.GITHUB, key = "#username.trim()")
    })
    public UserResponse connectGitHub(final String username) {
        final User user = getAuthenticatedUser();
        user.setGithubUsername(username.trim());
        final User savedUser = userRepository.save(user);

        eventPublisher.publish(EventTopics.GITHUB_CONNECTED_KEY,
                new NotificationEvent(user.getId(), NotificationType.SYSTEM,
                        "GitHub Account Connected",
                        "GitHub account connected successfully."));

        // Publish the gamification activity; the consumer awards XP and
        // evaluates the GitHub achievements asynchronously.
        eventPublisher.publish(EventTopics.ACHIEVEMENT_ACTIVITY_KEY,
                new ActivityEvent(user.getId(), ActivityType.GITHUB_CONNECTED,
                        null, LocalDateTime.now()));

        return authMapper.toUserResponse(savedUser);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    @CacheEvict(cacheNames = CacheNames.DASHBOARD)
    public UserResponse disconnectGitHub() {
        // The disconnected account's profile entries are keyed by that account's
        // username and simply expire via TTL; the user has no linked account, so
        // no stale data is ever displayed.
        final User user = getAuthenticatedUser();
        user.setGithubUsername(null);
        final User savedUser = userRepository.save(user);

        eventPublisher.publish(EventTopics.GITHUB_CONNECTED_KEY,
                new NotificationEvent(user.getId(), NotificationType.SYSTEM,
                        "GitHub Account Disconnected",
                        "GitHub account disconnected."));

        return authMapper.toUserResponse(savedUser);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.DASHBOARD),
            @CacheEvict(cacheNames = CacheNames.LEETCODE, key = "#username.trim()")
    })
    public UserResponse connectLeetCode(final String username) {
        final User user = getAuthenticatedUser();
        user.setLeetcodeUsername(username.trim());
        final User savedUser = userRepository.save(user);

        eventPublisher.publish(EventTopics.LEETCODE_CONNECTED_KEY,
                new NotificationEvent(user.getId(), NotificationType.SYSTEM,
                        "LeetCode Account Connected",
                        "LeetCode account connected successfully."));

        // Publish the gamification activity; the consumer awards XP and
        // evaluates the LeetCode achievements asynchronously.
        eventPublisher.publish(EventTopics.ACHIEVEMENT_ACTIVITY_KEY,
                new ActivityEvent(user.getId(), ActivityType.LEETCODE_SYNCED,
                        null, LocalDateTime.now()));

        return authMapper.toUserResponse(savedUser);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    @CacheEvict(cacheNames = CacheNames.DASHBOARD)
    public UserResponse disconnectLeetCode() {
        // See disconnectGitHub — orphaned profile entries expire via TTL.
        final User user = getAuthenticatedUser();
        user.setLeetcodeUsername(null);
        final User savedUser = userRepository.save(user);

        eventPublisher.publish(EventTopics.LEETCODE_CONNECTED_KEY,
                new NotificationEvent(user.getId(), NotificationType.SYSTEM,
                        "LeetCode Account Disconnected",
                        "LeetCode account disconnected."));

        return authMapper.toUserResponse(savedUser);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void changePassword(final ChangePasswordRequest request) {
        final User user = getAuthenticatedUser();

        // Verify the current password against the stored hash
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new IllegalArgumentException(
                    "New password must be different from the current password");
        }

        // Encode and persist the new password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
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

}

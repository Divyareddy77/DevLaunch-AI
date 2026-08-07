package com.devlaunch.messaging;

import com.devlaunch.entity.User;
import com.devlaunch.entity.enums.ActivityType;
import com.devlaunch.messaging.event.ActivityEvent;
import com.devlaunch.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Publishes a gamification activity whenever a linked external account is
 * freshly synced (GitHub profile or LeetCode profile fetched).
 * <p>
 * The GitHub and LeetCode services fetch public profiles on behalf of the
 * authenticated user. Each successful <em>fresh</em> fetch (the profile
 * reads are Redis-cached, so the method body only runs on a cache miss)
 * is a "sync" — this publisher turns it into an {@link ActivityEvent} so
 * the gamification engine re-evaluates the GitHub/LeetCode badges with the
 * latest repository / solved-problem counts. Without this, badges such as
 * "LeetCode Master" could never unlock for users who grow their profile
 * after the initial account connection.
 * </p>
 * <p>
 * Best effort and strictly guarded: the event is only published when there
 * is an authenticated user whose linked account username matches the
 * fetched username. On messaging threads (no security context) and for
 * arbitrary public lookups nothing is published, and any failure is logged
 * and swallowed so a sync event never breaks the profile fetch.
 * </p>
 *
 * @author DevLaunch
 */
@Component
public class AccountSyncEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(AccountSyncEventPublisher.class);

    private final EventPublisher eventPublisher;
    private final UserRepository userRepository;

    /**
     * Constructs the publisher.
     *
     * @param eventPublisher  the messaging backbone publisher
     * @param userRepository  repository for resolving the authenticated user
     */
    public AccountSyncEventPublisher(final EventPublisher eventPublisher,
                                     final UserRepository userRepository) {
        this.eventPublisher = eventPublisher;
        this.userRepository = userRepository;
    }

    /**
     * Publishes a sync activity for the authenticated user when the fetched
     * username matches the user's linked account.
     *
     * @param username the external username that was just fetched
     * @param type     the account activity type (GITHUB_CONNECTED / LEETCODE_SYNCED)
     * @param value    the synced metric (public repositories / solved problems),
     *                 or {@code null}
     */
    public void publishIfLinked(final String username, final ActivityType type,
                                final Integer value) {
        try {
            final Authentication authentication =
                    SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()
                    || authentication.getName() == null) {
                return;
            }
            final User user = userRepository.findByEmail(authentication.getName()).orElse(null);
            if (user == null || !linkedUsernameMatches(user, type, username)) {
                return;
            }
            eventPublisher.publish(EventTopics.ACHIEVEMENT_ACTIVITY_KEY,
                    new ActivityEvent(user.getId(), type, value, LocalDateTime.now()));
            log.info("Account sync activity published for user id={}, type={}, value={}",
                    user.getId(), type, value);
        } catch (final RuntimeException e) {
            // Sync events are best-effort — never break the profile fetch.
            log.warn("Could not publish account sync activity for username '{}': {}",
                    username, e.getMessage());
        }
    }

    /**
     * Checks that the fetched username matches the user's linked account
     * for the given activity type.
     *
     * @param user     the authenticated user
     * @param type     the activity type
     * @param username the fetched username
     * @return {@code true} when the fetched account is the user's linked account
     */
    private boolean linkedUsernameMatches(final User user, final ActivityType type,
                                          final String username) {
        final String linked = type == ActivityType.LEETCODE_SYNCED
                ? user.getLeetcodeUsername()
                : user.getGithubUsername();
        return linked != null && linked.equalsIgnoreCase(username == null ? "" : username.trim());
    }

}

package com.devlaunch.service.impl;

import com.devlaunch.dto.response.UserResponse;
import com.devlaunch.entity.User;
import com.devlaunch.entity.enums.NotificationType;
import com.devlaunch.mapper.AuthMapper;
import com.devlaunch.repository.UserRepository;
import com.devlaunch.service.interfaces.NotificationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the account linking operations in {@link UserServiceImpl}.
 * <p>
 * Verifies that connecting an external account (GitHub or LeetCode)
 * persists the trimmed username and raises a confirmation notification,
 * while disconnecting clears the saved username and notifies the user.
 * </p>
 *
 * @author DevLaunch
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    private static final String USER_EMAIL = "dev@example.com";

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthMapper authMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private NotificationService notificationService;

    private UserServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UserServiceImpl(
                userRepository, authMapper, passwordEncoder, notificationService);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticate() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(USER_EMAIL, null, List.of()));
    }

    private User user() {
        final User user = User.builder().email(USER_EMAIL).build();
        user.setId(1L);
        return user;
    }

    private void stubPersistence(final User user) {
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(authMapper.toUserResponse(any(User.class)))
                .thenReturn(UserResponse.builder().email(USER_EMAIL).build());
    }

    @Test
    @DisplayName("connecting a GitHub account saves the trimmed username and notifies")
    void connectGitHubSavesUsernameAndNotifies() {
        authenticate();
        final User user = user();
        stubPersistence(user);

        final UserResponse response = service.connectGitHub("  octocat  ");

        assertEquals(USER_EMAIL, response.getEmail());
        assertEquals("octocat", user.getGithubUsername());
        verify(notificationService).createNotification(
                eq(user), eq(NotificationType.SYSTEM),
                eq("GitHub Account Connected"),
                eq("GitHub account connected successfully."));
    }

    @Test
    @DisplayName("disconnecting a GitHub account clears the username and notifies")
    void disconnectGitHubClearsUsernameAndNotifies() {
        authenticate();
        final User user = user();
        user.setGithubUsername("octocat");
        stubPersistence(user);

        service.disconnectGitHub();

        assertNull(user.getGithubUsername());
        verify(notificationService).createNotification(
                eq(user), eq(NotificationType.SYSTEM),
                eq("GitHub Account Disconnected"),
                eq("GitHub account disconnected."));
    }

    @Test
    @DisplayName("connecting a LeetCode account saves the trimmed username and notifies")
    void connectLeetCodeSavesUsernameAndNotifies() {
        authenticate();
        final User user = user();
        stubPersistence(user);

        service.connectLeetCode("  leetcode_user  ");

        assertEquals("leetcode_user", user.getLeetcodeUsername());
        verify(notificationService).createNotification(
                eq(user), eq(NotificationType.SYSTEM),
                eq("LeetCode Account Connected"),
                eq("LeetCode account connected successfully."));
    }

    @Test
    @DisplayName("disconnecting a LeetCode account clears the username and notifies")
    void disconnectLeetCodeClearsUsernameAndNotifies() {
        authenticate();
        final User user = user();
        user.setLeetcodeUsername("leetcode_user");
        stubPersistence(user);

        service.disconnectLeetCode();

        assertNull(user.getLeetcodeUsername());
        verify(notificationService).createNotification(
                eq(user), eq(NotificationType.SYSTEM),
                eq("LeetCode Account Disconnected"),
                eq("LeetCode account disconnected."));
    }

}

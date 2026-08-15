package com.devlaunch.cache;

import com.devlaunch.entity.User;
import com.devlaunch.security.CustomUserDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link UserCacheKeyGenerator}.
 * <p>
 * Verifies that cache keys are scoped to the authenticated user's ID so no
 * shared entry is ever created across users, and that unauthenticated or
 * anonymous contexts fall back to a stable placeholder key.
 * </p>
 *
 * @author DevLaunch
 */
class UserCacheKeyGeneratorTest {

    private final UserCacheKeyGenerator generator = new UserCacheKeyGenerator();

    @Test
    @DisplayName("derives the key from the authenticated user's id")
    void derivesKeyFromAuthenticatedUser() {
        final User user = User.builder().email("dev@example.com").build();
        user.setId(42L);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        new CustomUserDetails(user), null, List.of()));
        try {
            assertEquals(42L, generator.generate(null, null));
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    @DisplayName("uses the anonymous placeholder when no authentication is present")
    void usesAnonymousKeyWithoutAuthentication() {
        SecurityContextHolder.clearContext();
        assertEquals("anonymous", generator.generate(null, null));
    }

    @Test
    @DisplayName("uses the anonymous placeholder for an anonymous principal")
    void usesAnonymousKeyForAnonymousPrincipal() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("anonymousUser", null, List.of()));
        try {
            assertEquals("anonymous", generator.generate(null, null));
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

}

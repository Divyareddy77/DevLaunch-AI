package com.devlaunch.cache;

import com.devlaunch.entity.User;
import com.devlaunch.security.CustomUserDetails;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Method;

/**
 * Default {@link KeyGenerator} for every cached read in DevLaunch.
 * <p>
 * All cached data is per-user, so the cache key is derived from the
 * authenticated user's ID rather than from method arguments (which carry
 * DTOs or nothing at all). Keys are stable across {@code @Cacheable} and
 * {@code @CacheEvict} on the same user: the writes that evict caches run on
 * the same request thread and resolve the same key. Consumers on messaging
 * threads (which have no security context) always pass an explicit
 * {@code key} expression instead, so they never use this generator.
 * </p>
 *
 * @author DevLaunch
 */
public class UserCacheKeyGenerator implements KeyGenerator {

    /**
     * Fallback key used when no authenticated user can be resolved, so a
     * single shared entry is never created for different users.
     */
    private static final String ANONYMOUS_KEY = "anonymous";

    @Override
    public Object generate(final Object target, final Method method, final Object... params) {
        final Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ANONYMOUS_KEY;
        }

        final Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails details) {
            return details.getUser().getId();
        }
        if (principal instanceof User user) {
            return user.getId();
        }
        return ANONYMOUS_KEY;
    }

}

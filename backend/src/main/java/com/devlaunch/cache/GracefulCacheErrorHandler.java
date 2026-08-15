package com.devlaunch.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;

/**
 * Cache error handler that degrades gracefully when Redis is unavailable.
 * <p>
 * A cache failure must never break a business read: the exception is logged
 * and the intercepted method simply executes against the database, exactly
 * as if the cache were disabled. This is what keeps the application fully
 * functional when Redis is down, misconfigured, or unreachable.
 * </p>
 *
 * @author DevLaunch
 */
public class GracefulCacheErrorHandler implements CacheErrorHandler {

    private static final Logger log = LoggerFactory.getLogger(GracefulCacheErrorHandler.class);

    @Override
    public void handleCacheGetError(final RuntimeException exception,
                                    final Cache cache, final Object key) {
        log.warn("Cache read failed (falling back to database): cache={}, key={}, error={}",
                cache.getName(), key, exception.getMessage());
    }

    @Override
    public void handleCachePutError(final RuntimeException exception,
                                    final Cache cache, final Object key, final Object value) {
        log.warn("Cache write failed (value not cached): cache={}, key={}, error={}",
                cache.getName(), key, exception.getMessage());
    }

    @Override
    public void handleCacheEvictError(final RuntimeException exception,
                                      final Cache cache, final Object key) {
        log.warn("Cache eviction failed: cache={}, key={}, error={}",
                cache.getName(), key, exception.getMessage());
    }

    @Override
    public void handleCacheClearError(final RuntimeException exception, final Cache cache) {
        log.warn("Cache clear failed: cache={}, error={}", cache.getName(), exception.getMessage());
    }

}

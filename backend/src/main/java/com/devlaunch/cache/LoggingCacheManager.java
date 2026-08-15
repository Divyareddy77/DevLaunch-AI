package com.devlaunch.cache;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Collection;

/**
 * {@link CacheManager} decorator that wraps every cache handed out in a
 * {@link LoggingCache}, giving debug-level visibility into cache hits,
 * misses, puts, and evictions without changing cache behavior.
 *
 * @author DevLaunch
 */
public class LoggingCacheManager implements CacheManager {

    private final CacheManager delegate;

    /**
     * Constructs the decorator around the real cache manager.
     *
     * @param delegate the cache manager to decorate
     */
    public LoggingCacheManager(final CacheManager delegate) {
        this.delegate = delegate;
    }

    @Override
    public Cache getCache(final String name) {
        final Cache cache = delegate.getCache(name);
        return cache == null ? null : new LoggingCache(cache);
    }

    @Override
    public Collection<String> getCacheNames() {
        return delegate.getCacheNames();
    }

    /**
     * Returns the underlying cache for the given name, bypassing the logging
     * decorator, so configuration tests can inspect the native Redis cache
     * (TTLs, serializers) without a live connection.
     *
     * @param name the cache name
     * @return the underlying cache, or {@code null} if unknown
     */
    Cache unwrapCache(final String name) {
        return delegate.getCache(name);
    }

}

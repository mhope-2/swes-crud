package com.michaelhope.cache;

import com.github.benmanes.caffeine.cache.stats.CacheStats;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Objects;

import com.michaelhope.dto.SoftwareEngineerResponse;

@Component
@RequiredArgsConstructor
public class EngineerResponseCache {

    private final CacheManager cacheManager;

    public void putAfterCommit(Integer id, SoftwareEngineerResponse response) {
        afterCommit(() -> cache().put(id, response));
    }

    public void evictAfterCommit(Integer id) {
        afterCommit(() -> cache().evict(id));
    }

    public CacheStats stats() {
        return nativeCache().stats();
    }

    public long estimatedSize() {
        return nativeCache().estimatedSize();
    }

    private Cache cache() {
        return Objects.requireNonNull(
            cacheManager.getCache(CacheNames.ENGINEER_BY_ID),
            "Engineer cache is not configured"
        );
    }

    @SuppressWarnings("unchecked")
    private com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache() {
        Cache cache = cache();
        if (!(cache instanceof CaffeineCache caffeineCache)) {
            throw new IllegalStateException("Engineer cache must use Caffeine");
        }
        return (com.github.benmanes.caffeine.cache.Cache<Object, Object>) caffeineCache.getNativeCache();
    }

    private void afterCommit(Runnable action) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            action.run();
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                action.run();
            }
        });
    }
}

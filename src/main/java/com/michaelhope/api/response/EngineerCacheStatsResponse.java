package com.michaelhope.api.response;

public record EngineerCacheStatsResponse(
    String cacheName,
    long estimatedSize,
    long hitCount,
    long missCount,
    double hitRate,
    double missRate,
    long loadSuccessCount,
    long loadFailureCount,
    long totalLoadTimeNanos,
    long evictionCount
) {
}

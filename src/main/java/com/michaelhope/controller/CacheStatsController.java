package com.michaelhope.controller;

import com.github.benmanes.caffeine.cache.stats.CacheStats;
import com.michaelhope.cache.CacheNames;
import com.michaelhope.cache.EngineerCacheStatsResponse;
import com.michaelhope.cache.EngineerResponseCache;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/cache")
@RequiredArgsConstructor
public class CacheStatsController {

    private final EngineerResponseCache engineerResponseCache;

    @GetMapping("engineer-by-id/stats")
    public ResponseEntity<EngineerCacheStatsResponse> getEngineerCacheStats() {
        CacheStats stats = engineerResponseCache.stats();
        return ResponseEntity.ok(new EngineerCacheStatsResponse(
            CacheNames.ENGINEER_BY_ID,
            engineerResponseCache.estimatedSize(),
            stats.hitCount(),
            stats.missCount(),
            stats.hitRate(),
            stats.missRate(),
            stats.loadSuccessCount(),
            stats.loadFailureCount(),
            stats.totalLoadTime(),
            stats.evictionCount()
        ));
    }
}

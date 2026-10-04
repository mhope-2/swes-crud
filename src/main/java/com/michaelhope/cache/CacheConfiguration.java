package com.michaelhope.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfiguration {

    @Bean
    public CacheManager cacheManager(
            @Value("${app.cache.engineer-by-id.maximum-size:500}") long maximumSize,
            @Value("${app.cache.engineer-by-id.expire-after-write:10m}") Duration expireAfterWrite) {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(CacheNames.ENGINEER_BY_ID);
        cacheManager.setCaffeine(Caffeine.newBuilder()
            .maximumSize(maximumSize)
            .expireAfterWrite(expireAfterWrite)
            .recordStats());
        return cacheManager;
    }
}

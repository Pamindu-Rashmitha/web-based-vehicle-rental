package com.example.web_based_vehicle_rental.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.concurrent.TimeUnit;

/**
 * Cache configuration using Caffeine
 * Improves performance by caching frequently accessed data
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Configure Caffeine cache manager with multiple caches
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                "vehicles", // Individual vehicle cache
                "availableVehicles", // Available vehicles list
                "reviewStatistics", // Review stats per vehicle
                "userReservations" // User's reservations
        );

        cacheManager.setCaffeine(caffeineCacheBuilder());
        return cacheManager;
    }

    /**
     * Default Caffeine cache configuration
     * - Maximum 1000 entries per cache
     * - Expire after 5 minutes of write
     * - Expire after 10 minutes of access
     */
    private Caffeine<Object, Object> caffeineCacheBuilder() {
        return Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .recordStats(); // Enable cache statistics
    }
}

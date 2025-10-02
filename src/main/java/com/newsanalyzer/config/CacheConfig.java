package com.newsanalyzer.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Cache configuration for the News Analyzer application.
 *
 * Configures Redis-based caching with different TTL values
 * for different types of cached data.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Configure Redis cache manager with custom TTL for different caches.
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1)) // Default TTL: 1 hour
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();

        // Configure specific TTL for different cache types
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // Source reliability cache - 1 hour TTL
        cacheConfigurations.put("source-reliability",
                defaultConfig.entryTtl(Duration.ofHours(1)));

        // Claim validation cache - 24 hours TTL
        cacheConfigurations.put("claim-validation",
                defaultConfig.entryTtl(Duration.ofHours(24)));

        // Bias analysis cache - 6 hours TTL
        cacheConfigurations.put("bias-analysis",
                defaultConfig.entryTtl(Duration.ofHours(6)));

        // Source lookup caches - 2 hours TTL
        cacheConfigurations.put("source-by-id",
                defaultConfig.entryTtl(Duration.ofHours(2)));

        cacheConfigurations.put("source-by-domain",
                defaultConfig.entryTtl(Duration.ofHours(2)));

        // Statistics cache - 15 minutes TTL
        cacheConfigurations.put("reliability-statistics",
                defaultConfig.entryTtl(Duration.ofMinutes(15)));

        // Quick analysis cache - 30 minutes TTL
        cacheConfigurations.put("quick-analysis",
                defaultConfig.entryTtl(Duration.ofMinutes(30)));

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}
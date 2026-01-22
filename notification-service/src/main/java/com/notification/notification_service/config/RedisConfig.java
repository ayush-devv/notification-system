package com.notification.notification_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    // Configure RedisTemplate bean for Redis operations
    // RedisConnectionFactory is auto-configured by Spring Boot from application.yml
    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        // Create RedisTemplate instance with String types for both key and value
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        
        // Set the connection factory (manages connections to Redis server)
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        // Set serializers for keys and values
        // StringRedisSerializer converts String to bytes for Redis storage
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());

        return redisTemplate;
    }
}

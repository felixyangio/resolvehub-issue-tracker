package com.resolvehub.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Redis configuration.
 *
 * <p>Provides a {@link StringRedisTemplate} bean for use by rate limiters
 * and future caching layers. The template uses String serialisers by
 * default — ideal for counters, tokens, and simple key-value data.</p>
 */
@Configuration
public class RedisConfig {

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }
}

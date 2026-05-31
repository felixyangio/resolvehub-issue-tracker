package com.resolvehub.service;

import com.resolvehub.exception.TooManyRequestsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Sliding-window rate limiter for login attempts, backed by Redis.
 *
 * <p>Uses a Lua script for atomicity: increment the counter and set TTL in one
 * round-trip. This prevents race conditions that would arise from separate
 * INCR + EXPIRE calls (cache击穿-style split-second gaps).</p>
 *
 * <p>If Redis is unavailable the limiter degrades gracefully — login is
 * allowed so the system stays usable even without Redis.</p>
 */
@Slf4j
@Service
public class LoginRateLimiter {

    private static final String KEY_PREFIX = "rate:login:";

    /**
     * Lua script: atomically INCR and conditionally set EXPIRE.
     *
     * <ul>
     *   <li>KEYS[1] — the rate-limit key (e.g. "rate:login:bob@tenant.io")</li>
     *   <li>ARGV[1] — TTL in seconds (the sliding window)</li>
     * </ul>
     *
     * Returns the new counter value after increment.
     */
    private static final String LUA_SCRIPT = """
            local current = redis.call('INCR', KEYS[1])
            if current == 1 then
                redis.call('EXPIRE', KEYS[1], ARGV[1])
            end
            return current
            """;

    private final StringRedisTemplate redisTemplate;
    private final DefaultRedisScript<Long> script;
    private final int maxAttempts;
    private final int windowSeconds;

    public LoginRateLimiter(
            StringRedisTemplate redisTemplate,
            @Value("${app.rate-limit.login.max-attempts:5}") int maxAttempts,
            @Value("${app.rate-limit.login.window-seconds:300}") int windowSeconds
    ) {
        this.redisTemplate = redisTemplate;
        this.maxAttempts = maxAttempts;
        this.windowSeconds = windowSeconds;

        this.script = new DefaultRedisScript<>();
        this.script.setScriptText(LUA_SCRIPT);
        this.script.setResultType(Long.class);
    }

    /**
     * Check and record a login attempt for the given email.
     *
     * @throws TooManyRequestsException if the email has exceeded maxAttempts
     *                                   within the current window
     */
    public void checkRateLimit(String email) {
        try {
            String key = KEY_PREFIX + email.toLowerCase();
            Long attempts = redisTemplate.execute(
                    script,
                    Collections.singletonList(key),
                    String.valueOf(windowSeconds)
            );

            if (attempts != null && attempts > maxAttempts) {
                long ttl = remainingSeconds(key);
                throw new TooManyRequestsException(
                        "Too many login attempts. Please try again in " + ttl + " seconds."
                );
            }
        } catch (TooManyRequestsException e) {
            throw e; // re-throw our own exception
        } catch (Exception e) {
            // Redis down — degrade gracefully, allow login
            log.warn("Redis unavailable for rate limiting, allowing request: {}", e.getMessage());
        }
    }

    /**
     * Clear the rate-limit counter after a successful login.
     */
    public void resetCounter(String email) {
        try {
            redisTemplate.delete(KEY_PREFIX + email.toLowerCase());
        } catch (Exception e) {
            log.warn("Failed to reset rate limit counter: {}", e.getMessage());
        }
    }

    private long remainingSeconds(String key) {
        try {
            Long ttl = redisTemplate.getExpire(key);
            return (ttl != null && ttl > 0) ? ttl : windowSeconds;
        } catch (Exception e) {
            return windowSeconds;
        }
    }
}

package com.resolvehub.service;

import com.resolvehub.exception.TooManyRequestsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for the Redis-based login rate limiter.
 *
 * <p>Key scenarios:</p>
 * <ol>
 *   <li>Under the limit → allow</li>
 *   <li>At the limit → allow (boundary)</li>
 *   <li>Over the limit → block with 429</li>
 *   <li>Redis down → degrade gracefully (allow)</li>
 *   <li>Successful login → counter reset</li>
 * </ol>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LoginRateLimiter — sliding window rate limiting")
class LoginRateLimiterTest {

    private static final int MAX_ATTEMPTS = 5;
    private static final int WINDOW_SECONDS = 300;

    @Mock
    private StringRedisTemplate redisTemplate;

    private LoginRateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        rateLimiter = new LoginRateLimiter(redisTemplate, MAX_ATTEMPTS, WINDOW_SECONDS);
    }

    @Test
    @DisplayName("should allow requests under the rate limit")
    void allowsRequestsUnderLimit() {
        when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), any()))
                .thenReturn(3L); // 3rd attempt, under 5

        assertDoesNotThrow(() -> rateLimiter.checkRateLimit("user@test.com"));
    }

    @Test
    @DisplayName("should allow the request exactly at the limit (boundary)")
    void allowsRequestAtExactLimit() {
        when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), any()))
                .thenReturn(5L); // exactly at limit

        assertDoesNotThrow(() -> rateLimiter.checkRateLimit("user@test.com"));
    }

    @Test
    @DisplayName("should block requests exceeding the rate limit with TooManyRequestsException")
    void blocksRequestsOverLimit() {
        when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), any()))
                .thenReturn(6L); // over limit
        when(redisTemplate.getExpire("rate:login:user@test.com"))
                .thenReturn(120L);

        TooManyRequestsException ex = assertThrows(
                TooManyRequestsException.class,
                () -> rateLimiter.checkRateLimit("user@test.com"));

        assertTrue(ex.getMessage().contains("Too many login attempts"));
        assertTrue(ex.getMessage().contains("120 seconds"));
    }

    @Test
    @DisplayName("should degrade gracefully when Redis is unavailable")
    void degradesGracefullyWhenRedisDown() {
        when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), any()))
                .thenThrow(new RedisConnectionFailureException("Connection refused"));

        // Should NOT throw — login allowed when Redis is down
        assertDoesNotThrow(() -> rateLimiter.checkRateLimit("user@test.com"));
    }

    @Test
    @DisplayName("should normalise email to lowercase for consistent key matching")
    void normalisesEmailToLowercase() {
        when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), any()))
                .thenReturn(1L);

        rateLimiter.checkRateLimit("User@Test.COM");

        verify(redisTemplate).execute(
                any(DefaultRedisScript.class),
                org.mockito.ArgumentMatchers.eq(Collections.singletonList("rate:login:user@test.com")),
                any());
    }

    @Test
    @DisplayName("should delete the rate-limit key on successful login reset")
    void resetsCounterAfterSuccessfulLogin() {
        rateLimiter.resetCounter("user@test.com");

        verify(redisTemplate).delete("rate:login:user@test.com");
    }
}

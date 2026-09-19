package com.zamtrust.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RateLimiterTest {

    private RateLimiter limiter;

    @BeforeEach
    void setUp() {
        limiter = new RateLimiter();
    }

    @Test
    @DisplayName("Allows up to max requests within the window")
    void allowsUpToMax() {
        for (int i = 0; i < 5; i++) {
            assertTrue(limiter.tryConsume("key", 5, Duration.ofMinutes(1)));
        }
    }

    @Test
    @DisplayName("Blocks the request after max is exceeded")
    void blocksAfterMax() {
        for (int i = 0; i < 5; i++) {
            limiter.tryConsume("key", 5, Duration.ofMinutes(1));
        }
        assertFalse(limiter.tryConsume("key", 5, Duration.ofMinutes(1)));
    }

    @Test
    @DisplayName("Different keys have independent buckets")
    void independentKeys() {
        for (int i = 0; i < 5; i++) {
            limiter.tryConsume("a", 5, Duration.ofMinutes(1));
        }
        assertFalse(limiter.tryConsume("a", 5, Duration.ofMinutes(1)));
        assertTrue(limiter.tryConsume("b", 5, Duration.ofMinutes(1)));
    }
}

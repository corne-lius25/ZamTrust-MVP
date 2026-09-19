package com.zamtrust.security;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple in-memory sliding-window rate limiter.
 *
 * Resolves finding #5 (no rate limiting on auth endpoints, enables
 * username enumeration at scale and credential stuffing).
 *
 * Thread-safe. Per-key (per-IP or per-username). Auto-evicts stale entries
 * on access. Not distributed — if we run multiple instances later, we'll
 * move to Redis or Bucket4j.
 */
@Component
public class RateLimiter {

    private static final int MAX_KEYS = 10_000;

    private final Map<String, Deque<Long>> buckets = new ConcurrentHashMap<>();

    public boolean tryConsume(String key, int maxRequests, Duration window) {
        long now = System.currentTimeMillis();
        long windowMs = window.toMillis();
        long cutoff = now - windowMs;

        if (buckets.size() > MAX_KEYS) {
            buckets.entrySet().removeIf(e -> {
                Deque<Long> d = e.getValue();
                return d.isEmpty() || (d.peekLast() != null && d.peekLast() < cutoff);
            });
        }

        Deque<Long> bucket = buckets.computeIfAbsent(key, k -> new ArrayDeque<>());

        synchronized (bucket) {
            while (!bucket.isEmpty() && bucket.peekFirst() < cutoff) {
                bucket.pollFirst();
            }
            if (bucket.size() >= maxRequests) {
                return false;
            }
            bucket.addLast(now);
            return true;
        }
    }

    void clear() {
        buckets.clear();
    }
}

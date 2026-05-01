package com.company.cs.infra.ratelimit;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ChatRateLimiter {

    private final int requestsPerWindow;
    private final int windowSeconds;
    private final ConcurrentHashMap<String, WindowCounter> userCounters = new ConcurrentHashMap<>();

    public ChatRateLimiter(
            @Value("${app.rate-limit.requests-per-window:20}") int requestsPerWindow,
            @Value("${app.rate-limit.window-seconds:1}") int windowSeconds
    ) {
        this.requestsPerWindow = requestsPerWindow;
        this.windowSeconds = windowSeconds;
    }

    public void checkAndIncrement(String userId) {
        WindowCounter counter = userCounters.computeIfAbsent(userId, key -> new WindowCounter(nowEpochSecond()));
        long now = nowEpochSecond();
        if (now - counter.windowStart >= windowSeconds) {
            counter.windowStart = now;
            counter.count.set(0);
        }
        int current = counter.count.incrementAndGet();
        if (current > requestsPerWindow) {
            throw new RateLimitExceededException("Too many requests for user: " + userId);
        }
    }

    private long nowEpochSecond() {
        return Instant.now().getEpochSecond();
    }

    private static final class WindowCounter {
        private volatile long windowStart;
        private final AtomicInteger count = new AtomicInteger(0);

        private WindowCounter(long windowStart) {
            this.windowStart = windowStart;
        }
    }
}

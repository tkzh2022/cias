package com.company.cs.infra.cache;

import com.company.cs.api.dto.ChatResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class SemanticCacheService {

    private static final Duration TTL = Duration.ofMinutes(5);
    private final Map<String, CacheValue> cache = new ConcurrentHashMap<>();

    public Optional<ChatResponse> get(String key) {
        CacheValue value = cache.get(key);
        if (value == null) {
            return Optional.empty();
        }
        if (Instant.now().isAfter(value.expireAt)) {
            cache.remove(key);
            return Optional.empty();
        }
        return Optional.of(value.response);
    }

    public void put(String key, ChatResponse response) {
        cache.put(key, new CacheValue(response, Instant.now().plus(TTL)));
    }

    private record CacheValue(ChatResponse response, Instant expireAt) {
    }
}

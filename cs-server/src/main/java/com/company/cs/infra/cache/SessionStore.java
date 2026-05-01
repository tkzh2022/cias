package com.company.cs.infra.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class SessionStore {

    private final Map<String, List<String>> sessionMessages = new ConcurrentHashMap<>();

    public void append(String sessionId, String role, String content) {
        String line = role + ": " + content;
        sessionMessages.computeIfAbsent(sessionId, key -> new ArrayList<>()).add(line);
    }

    public List<String> latest(String sessionId, int limit) {
        List<String> all = sessionMessages.getOrDefault(sessionId, List.of());
        int start = Math.max(all.size() - limit, 0);
        return all.subList(start, all.size());
    }
}

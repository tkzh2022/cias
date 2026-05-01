package com.company.cs.api.dto;

import java.util.List;

public record ChatResponse(
        String requestId,
        String sessionId,
        String answer,
        String route,
        boolean handoff,
        List<KnowledgeSource> sources
) {
}

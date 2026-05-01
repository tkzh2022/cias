package com.company.cs.api.dto;

public record KnowledgeSource(
        String id,
        String title,
        String snippet,
        double score
) {
}

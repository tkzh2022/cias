package com.company.cs.domain;

import java.util.List;

public record RagContext(
        List<KnowledgeSnippet> snippets,
        double confidence
) {
}

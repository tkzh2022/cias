package com.company.cs.infra.retrieval;

import com.company.cs.domain.KnowledgeSnippet;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class HybridRetriever {

    private final KeywordRetriever keywordRetriever;
    private final VectorRetriever vectorRetriever;

    public HybridRetriever(KeywordRetriever keywordRetriever, VectorRetriever vectorRetriever) {
        this.keywordRetriever = keywordRetriever;
        this.vectorRetriever = vectorRetriever;
    }

    public List<KnowledgeSnippet> search(String query, int topN) {
        List<KnowledgeSnippet> merged = new ArrayList<>();
        merged.addAll(keywordRetriever.search(query, topN));
        merged.addAll(vectorRetriever.search(query, topN));
        return merged.stream()
                .sorted(Comparator.comparingDouble(KnowledgeSnippet::score).reversed())
                .limit(topN)
                .toList();
    }
}

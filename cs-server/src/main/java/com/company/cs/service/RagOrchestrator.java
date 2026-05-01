package com.company.cs.service;

import com.company.cs.domain.KnowledgeSnippet;
import com.company.cs.domain.RagContext;
import com.company.cs.infra.retrieval.HybridRetriever;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class RagOrchestrator {

    private final HybridRetriever hybridRetriever;

    public RagOrchestrator(HybridRetriever hybridRetriever) {
        this.hybridRetriever = hybridRetriever;
    }

    public RagContext retrieve(String message) {
        List<KnowledgeSnippet> snippets = hybridRetriever.search(message, 4);
        double confidence = snippets.stream().mapToDouble(KnowledgeSnippet::score).average().orElse(0.4);
        return new RagContext(snippets, confidence);
    }

    public String buildPrompt(String question, List<String> history, RagContext context) {
        StringBuilder builder = new StringBuilder();
        builder.append("你是企业智能客服助手，优先使用给定知识回答。\n");
        builder.append("历史对话:\n");
        for (String line : history) {
            builder.append("- ").append(line).append('\n');
        }
        builder.append("知识片段:\n");
        for (KnowledgeSnippet snippet : context.snippets()) {
            builder.append("- [").append(snippet.id()).append("] ")
                    .append(snippet.title()).append(": ")
                    .append(snippet.snippet()).append('\n');
        }
        builder.append("用户问题: ").append(question).append('\n');
        builder.append("要求: 使用简洁中文回答，并在不确定时说明原因。");
        return builder.toString();
    }
}

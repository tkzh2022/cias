package com.company.cs.infra.retrieval;

import com.company.cs.domain.KnowledgeSnippet;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class VectorRetriever {

    private static final List<KnowledgeSnippet> KB = List.of(
            new KnowledgeSnippet("kb-1", "售后流程", "申请售后需要订单号和问题描述。", 0.81),
            new KnowledgeSnippet("kb-2", "会员权益", "黑金会员享受专属客服与运费券。", 0.83),
            new KnowledgeSnippet("kb-3", "账号安全", "异地登录将触发二次验证。", 0.86)
    );

    public List<KnowledgeSnippet> search(String query, int topN) {
        String normalized = query.toLowerCase(Locale.ROOT);
        return KB.stream()
                .map(item -> new KnowledgeSnippet(item.id(), item.title(), item.snippet(), semanticScore(normalized, item.snippet())))
                .sorted((a, b) -> Double.compare(b.score(), a.score()))
                .limit(topN)
                .toList();
    }

    private double semanticScore(String query, String text) {
        int matches = 0;
        for (String token : query.split("\\s+")) {
            if (!token.isBlank() && text.contains(token)) {
                matches++;
            }
        }
        return Math.min(0.95, 0.5 + matches * 0.08);
    }
}

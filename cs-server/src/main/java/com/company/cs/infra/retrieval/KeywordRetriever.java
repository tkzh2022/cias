package com.company.cs.infra.retrieval;

import com.company.cs.domain.KnowledgeSnippet;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class KeywordRetriever {

    private static final List<KnowledgeSnippet> FAQ = List.of(
            new KnowledgeSnippet("faq-1", "退款政策", "7天无理由退款，原路返回。", 0.92),
            new KnowledgeSnippet("faq-2", "发票开具", "支持电子发票，订单完成后可申请。", 0.88),
            new KnowledgeSnippet("faq-3", "物流时效", "默认48小时内发货，偏远地区顺延。", 0.85)
    );

    public List<KnowledgeSnippet> search(String query, int topN) {
        String normalized = query.toLowerCase(Locale.ROOT);
        return FAQ.stream()
                .filter(item -> normalized.contains("退款") && item.id().equals("faq-1")
                        || normalized.contains("发票") && item.id().equals("faq-2")
                        || normalized.contains("物流") && item.id().equals("faq-3"))
                .limit(topN)
                .toList();
    }
}

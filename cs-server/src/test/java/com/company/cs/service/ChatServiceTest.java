package com.company.cs.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import com.company.cs.api.dto.ChatRequest;
import com.company.cs.api.dto.ChatResponse;
import com.company.cs.domain.KnowledgeSnippet;
import com.company.cs.domain.RagContext;
import com.company.cs.infra.cache.SemanticCacheService;
import com.company.cs.infra.cache.SessionStore;
import com.company.cs.infra.queue.KafkaProducer;
import com.company.cs.infra.ratelimit.ChatRateLimiter;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ChatServiceTest {

    @Test
    void shouldReturnHandoffWhenConfidenceLow() {
        ChatRateLimiter limiter = Mockito.mock(ChatRateLimiter.class);
        SemanticCacheService cache = Mockito.mock(SemanticCacheService.class);
        SessionStore sessionStore = Mockito.mock(SessionStore.class);
        RagOrchestrator rag = Mockito.mock(RagOrchestrator.class);
        ModelRouter router = Mockito.mock(ModelRouter.class);
        LlmGateway llm = Mockito.mock(LlmGateway.class);
        HumanHandoffService handoff = Mockito.mock(HumanHandoffService.class);
        KafkaProducer producer = Mockito.mock(KafkaProducer.class);

        ChatRequest request = new ChatRequest("s1", "u1", "我想投诉", "web");
        RagContext context = new RagContext(List.of(new KnowledgeSnippet("k1", "t", "s", 0.3)), 0.3);

        doNothing().when(limiter).checkAndIncrement("u1");
        when(cache.get("u1|我想投诉")).thenReturn(Optional.empty());
        when(rag.retrieve("我想投诉")).thenReturn(context);
        when(handoff.shouldHandoff("我想投诉", context)).thenReturn(true);

        ChatService service = new ChatService(limiter, cache, sessionStore, rag, router, llm, handoff, producer);
        ChatResponse response = service.chat(request);

        assertThat(response.handoff()).isTrue();
        assertThat(response.route()).isEqualTo("HANDOFF");
    }
}

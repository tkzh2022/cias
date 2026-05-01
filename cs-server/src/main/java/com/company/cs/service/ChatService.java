package com.company.cs.service;

import com.company.cs.api.dto.ChatRequest;
import com.company.cs.api.dto.ChatResponse;
import com.company.cs.api.dto.KnowledgeSource;
import com.company.cs.domain.KnowledgeSnippet;
import com.company.cs.domain.RagContext;
import com.company.cs.infra.cache.SemanticCacheService;
import com.company.cs.infra.cache.SessionStore;
import com.company.cs.infra.queue.KafkaProducer;
import com.company.cs.infra.ratelimit.ChatRateLimiter;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class ChatService {

    private final ChatRateLimiter rateLimiter;
    private final SemanticCacheService semanticCache;
    private final SessionStore sessionStore;
    private final RagOrchestrator ragOrchestrator;
    private final ModelRouter modelRouter;
    private final LlmGateway llmGateway;
    private final HumanHandoffService handoffService;
    private final KafkaProducer kafkaProducer;

    public ChatService(
            ChatRateLimiter rateLimiter,
            SemanticCacheService semanticCache,
            SessionStore sessionStore,
            RagOrchestrator ragOrchestrator,
            ModelRouter modelRouter,
            LlmGateway llmGateway,
            HumanHandoffService handoffService,
            KafkaProducer kafkaProducer
    ) {
        this.rateLimiter = rateLimiter;
        this.semanticCache = semanticCache;
        this.sessionStore = sessionStore;
        this.ragOrchestrator = ragOrchestrator;
        this.modelRouter = modelRouter;
        this.llmGateway = llmGateway;
        this.handoffService = handoffService;
        this.kafkaProducer = kafkaProducer;
    }

    public ChatResponse chat(ChatRequest request) {
        rateLimiter.checkAndIncrement(request.userId());
        String cacheKey = normalizedKey(request.userId(), request.message());
        return semanticCache.get(cacheKey).orElseGet(() -> processAndCache(request, cacheKey));
    }

    public SseEmitter stream(ChatRequest request) {
        SseEmitter emitter = new SseEmitter(15_000L);
        ChatResponse response = chat(request);
        try {
            emitter.send(SseEmitter.event().name("meta").data(response));
            String[] tokens = response.answer().split("");
            StringBuilder chunkBuffer = new StringBuilder();
            for (int i = 0; i < tokens.length; i++) {
                chunkBuffer.append(tokens[i]);
                if (i % 8 == 0 || i == tokens.length - 1) {
                    emitter.send(SseEmitter.event().name("chunk").data(chunkBuffer.toString()));
                    chunkBuffer.setLength(0);
                }
            }
            emitter.send(SseEmitter.event().name("done").data("ok"));
            emitter.complete();
        } catch (IOException e) {
            emitter.completeWithError(e);
        }
        return emitter;
    }

    private ChatResponse processAndCache(ChatRequest request, String cacheKey) {
        String requestId = UUID.randomUUID().toString();
        RagContext context = ragOrchestrator.retrieve(request.message());
        if (handoffService.shouldHandoff(request.message(), context)) {
            ChatResponse handoffResponse = new ChatResponse(
                    requestId,
                    request.sessionId(),
                    "当前问题较复杂，已为你转接人工客服，请稍候。",
                    "HANDOFF",
                    true,
                    sources(context.snippets())
            );
            semanticCache.put(cacheKey, handoffResponse);
            publishEvent(request, handoffResponse);
            return handoffResponse;
        }

        ModelRouter.ModelTier tier = modelRouter.route(request.message(), context);
        String prompt = ragOrchestrator.buildPrompt(request.message(), sessionStore.latest(request.sessionId(), 6), context);
        String answer = llmGateway.generate(tier, prompt);
        ChatResponse response = new ChatResponse(
                requestId,
                request.sessionId(),
                answer,
                tier.name(),
                false,
                sources(context.snippets())
        );
        sessionStore.append(request.sessionId(), "user", request.message());
        sessionStore.append(request.sessionId(), "assistant", answer);
        semanticCache.put(cacheKey, response);
        publishEvent(request, response);
        return response;
    }

    private void publishEvent(ChatRequest request, ChatResponse response) {
        String event = """
                {"sessionId":"%s","userId":"%s","route":"%s","handoff":%s}
                """.formatted(request.sessionId(), request.userId(), response.route(), response.handoff());
        kafkaProducer.publishChatEvent(event.replaceAll("\\s+", ""));
    }

    private String normalizedKey(String userId, String message) {
        return userId + "|" + message.trim().toLowerCase();
    }

    private List<KnowledgeSource> sources(List<KnowledgeSnippet> snippets) {
        return snippets.stream()
                .map(item -> new KnowledgeSource(item.id(), item.title(), item.snippet(), item.score()))
                .collect(Collectors.toList());
    }
}

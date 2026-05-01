package com.company.cs.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class LlmGateway {

    private static final Logger log = LoggerFactory.getLogger(LlmGateway.class);

    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
    private final boolean cloudEnabled;
    private final String apiUrl;
    private final String apiKey;
    private final String fastModel;
    private final String strongModel;

    public LlmGateway(
            @Value("${app.llm.cloud-enabled:false}") boolean cloudEnabled,
            @Value("${app.llm.api-url:}") String apiUrl,
            @Value("${app.llm.api-key:}") String apiKey,
            @Value("${app.llm.fast-model:gpt-4o-mini}") String fastModel,
            @Value("${app.llm.strong-model:gpt-4.1}") String strongModel
    ) {
        this.cloudEnabled = cloudEnabled;
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
        this.fastModel = fastModel;
        this.strongModel = strongModel;
    }

    @CircuitBreaker(name = "llm", fallbackMethod = "fallbackGenerate")
    @Retry(name = "llm")
    public String generate(ModelRouter.ModelTier tier, String prompt) {
        if (!cloudEnabled || apiUrl == null || apiUrl.isBlank()) {
            return mockGenerate(tier, prompt);
        }
        String model = tier == ModelRouter.ModelTier.FAST ? fastModel : strongModel;
        String body = """
                {"model":"%s","input":"%s"}
                """.formatted(model, escape(prompt));
        HttpRequest request = HttpRequest.newBuilder(URI.create(apiUrl))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(4))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new IOException("Cloud LLM API error status=" + response.statusCode());
            }
            return response.body();
        } catch (IOException e) {
            log.error("Cloud LLM I/O error", e);
            throw new IllegalStateException("Cloud LLM I/O failure", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("LLM request interrupted", e);
        }
    }

    private String fallbackGenerate(ModelRouter.ModelTier tier, String prompt, Throwable throwable) {
        log.warn("LLM fallback triggered: {}", throwable.getMessage());
        return mockGenerate(tier, prompt);
    }

    private String mockGenerate(ModelRouter.ModelTier tier, String prompt) {
        String style = tier == ModelRouter.ModelTier.FAST ? "快速答复" : "深度答复";
        return style + "：根据知识库，" + extractUserQuestion(prompt);
    }

    private String extractUserQuestion(String prompt) {
        int index = prompt.lastIndexOf("用户问题:");
        if (index < 0) {
            return "请提供更多细节以便我更准确回答。";
        }
        return prompt.substring(index + 5).trim();
    }

    private String escape(String raw) {
        return raw.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n");
    }
}

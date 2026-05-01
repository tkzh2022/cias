package com.company.cs.service;

import com.company.cs.domain.RagContext;
import org.springframework.stereotype.Service;

@Service
public class ModelRouter {

    public ModelTier route(String question, RagContext context) {
        boolean shortQuestion = question.length() < 40;
        boolean highConfidence = context.confidence() >= 0.8;
        if (shortQuestion && highConfidence) {
            return ModelTier.FAST;
        }
        return ModelTier.STRONG;
    }

    public enum ModelTier {
        FAST,
        STRONG
    }
}

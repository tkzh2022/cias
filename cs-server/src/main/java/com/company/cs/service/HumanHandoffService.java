package com.company.cs.service;

import com.company.cs.domain.RagContext;
import org.springframework.stereotype.Service;

@Service
public class HumanHandoffService {

    public boolean shouldHandoff(String question, RagContext context) {
        boolean lowConfidence = context.confidence() < 0.65;
        boolean sensitive = question.contains("投诉") || question.contains("法律") || question.contains("隐私");
        return lowConfidence || sensitive;
    }
}

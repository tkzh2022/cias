package com.company.cs.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatRequest(
        @NotBlank String sessionId,
        @NotBlank String userId,
        @NotBlank @Size(max = 2000) String message,
        @NotBlank String channel
) {
}

package com.agies.did.model;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;

public record TrustEvaluationRequest(
    @NotBlank String agentId,
    @NotBlank String prompt,
    List<String> requestedTools,
    Map<String, Object> context
) {}

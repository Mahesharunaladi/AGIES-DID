package com.agies.did.model;

import java.util.List;

public record TrustEvaluationResponse(
    String agentId,
    double trustScore,
    String trustBand,
    int svidTtlSeconds,
    List<String> allowedTools,
    List<String> strippedPermissions,
    boolean credentialRevocationRecommended
) {}

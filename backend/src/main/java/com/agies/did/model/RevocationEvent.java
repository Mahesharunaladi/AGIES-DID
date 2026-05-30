package com.agies.did.model;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.Map;

public record RevocationEvent(
    @NotBlank String credentialId,
    @NotBlank String agentId,
    String reason,
    Instant occurredAt,
    Map<String, Object> claims
) {}

package com.agies.did.model;

import java.time.Instant;
import java.util.List;

public record AgentState(
    String agentId,
    double trustScore,
    String trustBand,
    List<String> recentTools,
    Instant updatedAt
) {}

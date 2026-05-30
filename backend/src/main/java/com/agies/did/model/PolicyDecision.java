package com.agies.did.model;

import java.util.List;

public record PolicyDecision(
    boolean allow,
    List<String> allowedTools,
    List<String> strippedPermissions,
    String reason
) {
  public static PolicyDecision allowAll(List<String> requestedTools) {
    return new PolicyDecision(true, requestedTools == null ? List.of() : requestedTools, List.of(), "trust score is acceptable");
  }
}

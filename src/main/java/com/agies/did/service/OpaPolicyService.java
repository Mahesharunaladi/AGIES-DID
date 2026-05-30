package com.agies.did.service;

import com.agies.did.model.PolicyDecision;
import com.agies.did.model.TrustEvaluationRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class OpaPolicyService {
  private final HttpClient httpClient = HttpClient.newHttpClient();
  private final ObjectMapper objectMapper;
  private final URI opaUri;

  public OpaPolicyService(ObjectMapper objectMapper, @Value("${agies.opa.url}") String opaUrl) {
    this.objectMapper = objectMapper;
    this.opaUri = URI.create(opaUrl);
  }

  public PolicyDecision decide(TrustEvaluationRequest request, double trustScore, String trustBand) {
    try {
      String payload = objectMapper.writeValueAsString(Map.of("input", Map.of(
          "agentId", request.agentId(),
          "requestedTools", request.requestedTools() == null ? List.of() : request.requestedTools(),
          "trustScore", trustScore,
          "trustBand", trustBand
      )));
      HttpRequest httpRequest = HttpRequest.newBuilder(opaUri)
          .header("Content-Type", "application/json")
          .POST(HttpRequest.BodyPublishers.ofString(payload))
          .build();
      HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
      JsonNode result = objectMapper.readTree(response.body()).path("result");
      if (result.isMissingNode() || result.isNull()) {
        return localFallback(request, trustBand);
      }
      return new PolicyDecision(
          result.path("allow").asBoolean(false),
          toList(result.path("allowedTools")),
          toList(result.path("strippedPermissions")),
          result.path("reason").asText("OPA decision")
      );
    } catch (Exception ignored) {
      return localFallback(request, trustBand);
    }
  }

  private PolicyDecision localFallback(TrustEvaluationRequest request, String trustBand) {
    List<String> requested = request.requestedTools() == null ? List.of() : request.requestedTools();
    if ("LOW".equals(trustBand)) {
      return new PolicyDecision(false, List.of(), List.of("write", "admin", "network"), "local fallback isolated low-trust workload");
    }
    if ("MEDIUM".equals(trustBand)) {
      List<String> readOnlyTools = requested.stream()
          .filter(tool -> !tool.toLowerCase().contains("write"))
          .filter(tool -> !tool.toLowerCase().contains("admin"))
          .toList();
      return new PolicyDecision(true, readOnlyTools, List.of("write", "admin"), "local fallback stripped write/admin permissions");
    }
    return PolicyDecision.allowAll(requested);
  }

  private List<String> toList(JsonNode node) {
    if (!node.isArray()) {
      return List.of();
    }
    return objectMapper.convertValue(
        node,
        objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
    );
  }
}

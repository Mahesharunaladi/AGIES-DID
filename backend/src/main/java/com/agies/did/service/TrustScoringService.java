package com.agies.did.service;

import com.agies.did.model.TrustEvaluationRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TrustScoringService {
  private final EmbeddingService embeddingService;
  private final double baselineScore;

  public TrustScoringService(
      EmbeddingService embeddingService,
      @Value("${agies.trust.baseline-score}") double baselineScore
  ) {
    this.embeddingService = embeddingService;
    this.baselineScore = baselineScore;
  }

  public double score(TrustEvaluationRequest request) {
    double[] embedding = embeddingService.embed(request.prompt());
    double intentRisk = riskyIntentPenalty(request.prompt());
    double driftSignal = Math.abs(embedding[0]) * 0.18d + Math.abs(embedding[17]) * 0.12d;
    double toolPressure = request.requestedTools() == null ? 0.0d : Math.min(0.2d, request.requestedTools().size() * 0.04d);
    return clamp(baselineScore - intentRisk - driftSignal - toolPressure);
  }

  public String band(double score) {
    if (score >= 0.75d) {
      return "HIGH";
    }
    if (score >= 0.45d) {
      return "MEDIUM";
    }
    return "LOW";
  }

  private double riskyIntentPenalty(String prompt) {
    if (prompt == null) {
      return 0.0d;
    }
    String normalized = prompt.toLowerCase();
    double penalty = 0.0d;
    if (normalized.contains("exfiltrate") || normalized.contains("steal")) {
      penalty += 0.25d;
    }
    if (normalized.contains("bypass") || normalized.contains("disable policy")) {
      penalty += 0.20d;
    }
    if (normalized.contains("write") || normalized.contains("delete")) {
      penalty += 0.08d;
    }
    return penalty;
  }

  private double clamp(double value) {
    return Math.max(0.0d, Math.min(1.0d, value));
  }
}

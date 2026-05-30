package com.agies.did.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TokenTtlService {
  private final int highTrustSeconds;
  private final int mediumTrustSeconds;
  private final int lowTrustSeconds;

  public TokenTtlService(
      @Value("${agies.ttl.high-trust-seconds}") int highTrustSeconds,
      @Value("${agies.ttl.medium-trust-seconds}") int mediumTrustSeconds,
      @Value("${agies.ttl.low-trust-seconds}") int lowTrustSeconds
  ) {
    this.highTrustSeconds = highTrustSeconds;
    this.mediumTrustSeconds = mediumTrustSeconds;
    this.lowTrustSeconds = lowTrustSeconds;
  }

  public int ttlFor(String trustBand) {
    return switch (trustBand) {
      case "HIGH" -> highTrustSeconds;
      case "MEDIUM" -> mediumTrustSeconds;
      default -> lowTrustSeconds;
    };
  }
}

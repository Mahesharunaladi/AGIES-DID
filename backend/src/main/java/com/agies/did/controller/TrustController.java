package com.agies.did.controller;

import com.agies.did.model.AgentState;
import com.agies.did.model.PolicyDecision;
import com.agies.did.model.TrustEvaluationRequest;
import com.agies.did.model.TrustEvaluationResponse;
import com.agies.did.service.OpaPolicyService;
import com.agies.did.service.StateGraphService;
import com.agies.did.service.TokenTtlService;
import com.agies.did.service.TrustScoringService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class TrustController {
  private final TrustScoringService trustScoringService;
  private final TokenTtlService tokenTtlService;
  private final OpaPolicyService opaPolicyService;
  private final StateGraphService stateGraphService;

  public TrustController(
      TrustScoringService trustScoringService,
      TokenTtlService tokenTtlService,
      OpaPolicyService opaPolicyService,
      StateGraphService stateGraphService
  ) {
    this.trustScoringService = trustScoringService;
    this.tokenTtlService = tokenTtlService;
    this.opaPolicyService = opaPolicyService;
    this.stateGraphService = stateGraphService;
  }

  @PostMapping("/trust/evaluate")
  public ResponseEntity<TrustEvaluationResponse> evaluate(@Valid @RequestBody TrustEvaluationRequest request) {
    double trustScore = trustScoringService.score(request);
    String trustBand = trustScoringService.band(trustScore);
    PolicyDecision decision = opaPolicyService.decide(request, trustScore, trustBand);
    int ttl = tokenTtlService.ttlFor(trustBand);
    stateGraphService.record(request, trustScore, trustBand);

    return ResponseEntity.ok(new TrustEvaluationResponse(
        request.agentId(),
        trustScore,
        trustBand,
        ttl,
        decision.allowedTools(),
        decision.strippedPermissions(),
        "LOW".equals(trustBand)
    ));
  }

  @PostMapping("/policy/decision")
  public ResponseEntity<PolicyDecision> policy(@Valid @RequestBody TrustEvaluationRequest request) {
    double trustScore = trustScoringService.score(request);
    String trustBand = trustScoringService.band(trustScore);
    return ResponseEntity.ok(opaPolicyService.decide(request, trustScore, trustBand));
  }

  @GetMapping("/agents/{agentId}/state")
  public ResponseEntity<AgentState> state(@PathVariable String agentId) {
    return ResponseEntity.ok(stateGraphService.currentState(agentId));
  }
}

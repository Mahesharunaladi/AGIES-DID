package com.agies.did.service;

import com.agies.did.model.RevocationEvent;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RevocationBusService {
  private static final Logger log = LoggerFactory.getLogger(RevocationBusService.class);

  public RevocationEvent publish(RevocationEvent event) {
    RevocationEvent normalized = new RevocationEvent(
        event.credentialId(),
        event.agentId(),
        event.reason(),
        event.occurredAt() == null ? Instant.now() : event.occurredAt(),
        event.claims()
    );
    log.info("vc_revocation_published credentialId={} agentId={} reason={}",
        normalized.credentialId(), normalized.agentId(), normalized.reason());
    return normalized;
  }
}

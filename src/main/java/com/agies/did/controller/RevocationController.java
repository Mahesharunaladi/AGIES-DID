package com.agies.did.controller;

import com.agies.did.model.RevocationEvent;
import com.agies.did.service.RevocationBusService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/revocations")
public class RevocationController {
  private final RevocationBusService revocationBusService;

  public RevocationController(RevocationBusService revocationBusService) {
    this.revocationBusService = revocationBusService;
  }

  @PostMapping
  public ResponseEntity<RevocationEvent> publish(@Valid @RequestBody RevocationEvent event) {
    return ResponseEntity.accepted().body(revocationBusService.publish(event));
  }
}

package com.agies.did.controller;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RootController {
  @GetMapping("/")
  Map<String, String> root() {
    return Map.of(
        "service", "agies-did-gateway",
        "docs", "/docs",
        "health", "/actuator/health"
    );
  }
}

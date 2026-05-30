package com.agies.did.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
  @Bean
  OpenAPI apiInfo() {
    return new OpenAPI()
        .info(new Info()
            .title("AGIES DID Gateway")
            .version("0.1.0")
            .description("Java entry point for trust analytics, policy enforcement, revocation, and audit workflows."));
  }
}

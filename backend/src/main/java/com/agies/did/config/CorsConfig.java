package com.agies.did.config;

import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {
  @Bean
  CorsFilter corsFilter(@Value("${agies.cors.allowed-origins}") String origins) {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(parseOrigins(origins));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("*"));
    config.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return new CorsFilter(source);
  }

  private List<String> parseOrigins(String origins) {
    return Arrays.stream(origins.split(","))
        .map(String::trim)
        .filter(origin -> !origin.isBlank())
        .toList();
  }
}

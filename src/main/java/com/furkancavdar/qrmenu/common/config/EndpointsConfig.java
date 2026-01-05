package com.furkancavdar.qrmenu.common.config;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EndpointsConfig {
  @Bean("publicEndpoints")
  public List<String> publicEndpoints() {
    return List.of(
        "/actuator/**",
        "/v3/api-docs/**",
        "/swagger-ui.html",
        "/swagger-ui/**",
        "/api/v1/auth/login",
        "/api/v1/auth/register",
        "/api/v1/auth/refresh",
        "/api/v1/auth/csrf",
        "/api/v1/auth/forgot-password",
        "/api/v1/auth/reset-password",
        "/api/test/all",
        "/api/test/session",
        "/api/v1/menu/job/**",

        // TODO: FIX
        "/api/v1/theme",
        // "/api/v1/theme/{themeId}",
        "/api/v1/theme/{themeId}/schemas",
        "/api/theme/test/**");
  }

  @Bean("authenticatedEndpoints")
  public List<String> authenticatedEndpoints() {
    return List.of(
        "/api/v1/auth/change-password",
        "/api/v1/auth/switch-developer-role",
        "/api/test/user",
        "/api/test/whoami",
        "/qr/test",
        "/api/v1/session/**",
        "/api/v1/theme/test",
        "/api/v1/theme/register",
        "/api/v1/theme/unregister",
        "/api/v1/menu/**");
  }

  @Bean("adminEndpoints")
  public List<String> adminEndpoints() {
    return List.of("/api/test/admin");
  }
}

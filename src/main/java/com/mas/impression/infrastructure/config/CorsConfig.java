package com.mas.impression.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS is scoped to an explicit allow-list ({@code agent.security.cors-allowed-origins}), never
 * a wildcard, since this endpoint accepts local-network requests that trigger real hardware
 * side effects.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private final AgentSecurityProperties securityProperties;

    public CorsConfig(AgentSecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(securityProperties.getCorsAllowedOrigins().toArray(new String[0]))
                .allowedMethods("GET", "POST")
                .allowedHeaders("Content-Type", "X-Agent-Token");
    }
}

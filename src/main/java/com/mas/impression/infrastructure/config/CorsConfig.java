package com.mas.impression.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS is scoped to an explicit allow-list ({@code agent.security.cors-allowed-origins}), never
 * a wildcard, since this endpoint accepts local-network requests that trigger real hardware
 * side effects.
 *
 * <p>{@code allowPrivateNetwork(true)} answers Chrome/Edge's Private Network Access preflight:
 * an {@code https://} page calling a loopback address like {@code http://localhost:PORT} sends
 * {@code Access-Control-Request-Private-Network: true} on the preflight, and the browser blocks
 * the real request unless the response echoes {@code Access-Control-Allow-Private-Network: true}
 * — a separate check from ordinary CORS and from mixed-content, easy to miss because plain HTTP
 * testing (e.g. Angular's {@code ng serve} on {@code http://localhost:4200}) never triggers it.
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
                .allowedHeaders("Content-Type", "X-Agent-Token")
                .allowPrivateNetwork(true);
    }
}

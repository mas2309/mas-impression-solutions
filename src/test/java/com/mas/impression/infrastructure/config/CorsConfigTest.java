package com.mas.impression.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class CorsConfigTest {

    @Test
    void registersCorsMappingUsingConfiguredOrigins() {
        AgentSecurityProperties properties = new AgentSecurityProperties();
        properties.setCorsAllowedOrigins(List.of("http://localhost:4200"));
        CorsConfig corsConfig = new CorsConfig(properties);

        assertDoesNotThrow(() -> corsConfig.addCorsMappings(new CorsRegistry()));
    }
}

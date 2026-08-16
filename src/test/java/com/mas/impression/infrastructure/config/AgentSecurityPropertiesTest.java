package com.mas.impression.infrastructure.config;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class AgentSecurityPropertiesTest {

    @Test
    void generatesTokenWhenNoneConfigured() {
        AgentSecurityProperties properties = new AgentSecurityProperties();
        properties.setToken("");

        properties.generateTokenIfMissing();

        assertFalse(properties.getToken().isBlank());
    }

    @Test
    void keepsConfiguredTokenUnchanged() {
        AgentSecurityProperties properties = new AgentSecurityProperties();
        properties.setToken("configured-token");

        properties.generateTokenIfMissing();

        assertEquals("configured-token", properties.getToken());
    }

    @Test
    void storesConfiguredCorsAllowedOrigins() {
        AgentSecurityProperties properties = new AgentSecurityProperties();

        properties.setCorsAllowedOrigins(List.of("https://pos.amara.com"));

        assertEquals(List.of("https://pos.amara.com"), properties.getCorsAllowedOrigins());
    }
}

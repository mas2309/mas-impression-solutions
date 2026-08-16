package com.mas.impression.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.core.Ordered;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityConfigTest {

    @Test
    void registersAgentTokenAuthFilterForApiUrls() {
        SecurityConfig config = new SecurityConfig();

        FilterRegistrationBean<AgentTokenAuthFilter> registration =
                config.agentTokenAuthFilter(new AgentSecurityProperties());

        assertNotNull(registration.getFilter());
        assertTrue(registration.getUrlPatterns().contains("/api/*"));
    }

    @Test
    void runsAfterTheCorsFilter() {
        SecurityConfig config = new SecurityConfig();

        FilterRegistrationBean<AgentTokenAuthFilter> registration =
                config.agentTokenAuthFilter(new AgentSecurityProperties());

        assertTrue(registration.getOrder() > Ordered.HIGHEST_PRECEDENCE);
    }
}

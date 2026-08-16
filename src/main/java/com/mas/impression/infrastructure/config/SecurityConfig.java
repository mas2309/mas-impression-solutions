package com.mas.impression.infrastructure.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class SecurityConfig {

    @Bean
    public FilterRegistrationBean<AgentTokenAuthFilter> agentTokenAuthFilter(
            AgentSecurityProperties securityProperties) {
        FilterRegistrationBean<AgentTokenAuthFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new AgentTokenAuthFilter(securityProperties));
        registration.addUrlPatterns("/api/*");
        // Must run after CorsConfig's CorsFilter (Ordered.HIGHEST_PRECEDENCE) so every response
        // this filter writes — including a 401 for a bad token — already carries CORS headers.
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        return registration;
    }
}

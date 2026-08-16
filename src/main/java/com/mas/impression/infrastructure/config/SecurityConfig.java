package com.mas.impression.infrastructure.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityConfig {

    @Bean
    public FilterRegistrationBean<AgentTokenAuthFilter> agentTokenAuthFilter(
            AgentSecurityProperties securityProperties) {
        FilterRegistrationBean<AgentTokenAuthFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new AgentTokenAuthFilter(securityProperties));
        registration.addUrlPatterns("/api/*");
        return registration;
    }
}

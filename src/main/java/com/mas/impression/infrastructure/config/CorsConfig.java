package com.mas.impression.infrastructure.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

/**
 * CORS is scoped to an explicit allow-list ({@code agent.security.cors-allowed-origins}), never
 * a wildcard, since this endpoint accepts local-network requests that trigger real hardware
 * side effects.
 *
 * <p>Registered as an explicit {@link CorsFilter} bean — not via {@code WebMvcConfigurer
 * .addCorsMappings}, which only adds CORS headers inside the {@code DispatcherServlet}'s own
 * dispatch. {@link AgentTokenAuthFilter} runs as a plain servlet {@code Filter} *before* the
 * dispatcher and can short-circuit a request (e.g. an invalid token → 401) without ever reaching
 * it, which meant that response carried no CORS headers at all — the browser then reported a
 * confusing "no Access-Control-Allow-Origin" error instead of the real 401. A {@link CorsFilter}
 * is itself a servlet filter, so ordering it to run *before* {@code AgentTokenAuthFilter}
 * (see {@link SecurityConfig}) guarantees every response — success or 401 — carries the right
 * CORS headers.
 *
 * <p>{@code setAllowPrivateNetwork(true)} answers Chrome/Edge's Private Network Access preflight:
 * an {@code https://} page calling a loopback address like {@code http://localhost:PORT} sends
 * {@code Access-Control-Request-Private-Network: true} on the preflight, and the browser blocks
 * the real request unless the response echoes {@code Access-Control-Allow-Private-Network: true}
 * — a separate check from ordinary CORS and from mixed-content, easy to miss because plain HTTP
 * testing (e.g. Angular's {@code ng serve} on {@code http://localhost:4200}) never triggers it.
 */
@Configuration
public class CorsConfig {

    private final AgentSecurityProperties securityProperties;

    public CorsConfig(AgentSecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilter() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(securityProperties.getCorsAllowedOrigins());
        configuration.setAllowedMethods(List.of("GET", "POST"));
        configuration.setAllowedHeaders(List.of("Content-Type", "X-Agent-Token"));
        configuration.setAllowPrivateNetwork(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);

        FilterRegistrationBean<CorsFilter> registration = new FilterRegistrationBean<>(new CorsFilter(source));
        registration.addUrlPatterns("/api/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }
}

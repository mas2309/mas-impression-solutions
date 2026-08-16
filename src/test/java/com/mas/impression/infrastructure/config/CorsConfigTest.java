package com.mas.impression.infrastructure.config;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.core.Ordered;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

class CorsConfigTest {

    private static final String ALLOWED_ORIGIN = "https://amara.sandbox-massolutions.uk";

    @Test
    void corsFilterRunsWithHighestPrecedenceOnApiPaths() {
        FilterRegistrationBean<CorsFilter> registration = corsConfigWith(ALLOWED_ORIGIN).corsFilter();

        assertEquals(Ordered.HIGHEST_PRECEDENCE, registration.getOrder());
        assertEquals(List.of("/api/*"), registration.getUrlPatterns().stream().toList());
    }

    @Test
    void allowsPreflightFromConfiguredOrigin() throws Exception {
        CorsFilter filter = corsConfigWith(ALLOWED_ORIGIN).corsFilter().getFilter();
        MockHttpServletRequest request = preflightRequest(ALLOWED_ORIGIN);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, mock(FilterChain.class));

        assertEquals(ALLOWED_ORIGIN, response.getHeader("Access-Control-Allow-Origin"));
        assertEquals("true", response.getHeader("Access-Control-Allow-Private-Network"));
    }

    @Test
    void rejectsPreflightFromUnlistedOrigin() throws Exception {
        CorsFilter filter = corsConfigWith(ALLOWED_ORIGIN).corsFilter().getFilter();
        MockHttpServletRequest request = preflightRequest("https://otro-origen.com");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, mock(FilterChain.class));

        assertNull(response.getHeader("Access-Control-Allow-Origin"));
    }

    private static MockHttpServletRequest preflightRequest(String origin) {
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/v1/status");
        request.addHeader("Origin", origin);
        request.addHeader("Access-Control-Request-Method", "GET");
        request.addHeader("Access-Control-Request-Private-Network", "true");
        return request;
    }

    private static CorsConfig corsConfigWith(String origin) {
        AgentSecurityProperties properties = new AgentSecurityProperties();
        properties.setCorsAllowedOrigins(List.of(origin));
        return new CorsConfig(properties);
    }
}

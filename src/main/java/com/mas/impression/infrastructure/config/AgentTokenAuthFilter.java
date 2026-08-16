package com.mas.impression.infrastructure.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Guards {@code /api/**} with a shared-secret header, so an arbitrary webpage cannot silently
 * trigger a print via {@code fetch('http://localhost:PORT/...')} just because loopback requests
 * bypass the browser's mixed-content and (mostly) CORS restrictions.
 */
public class AgentTokenAuthFilter extends OncePerRequestFilter {

    private static final String TOKEN_HEADER = "X-Agent-Token";
    private static final String PROTECTED_PREFIX = "/api/";

    private final AgentSecurityProperties securityProperties;

    public AgentTokenAuthFilter(AgentSecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (!request.getRequestURI().startsWith(PROTECTED_PREFIX)
                || securityProperties.getToken().equals(request.getHeader(TOKEN_HEADER))) {
            chain.doFilter(request, response);
            return;
        }
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                "{\"code\":\"UNAUTHORIZED\",\"message\":\"Token de agente inválido o ausente.\"}");
    }
}

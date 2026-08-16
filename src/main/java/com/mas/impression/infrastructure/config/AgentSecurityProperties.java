package com.mas.impression.infrastructure.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.UUID;

/**
 * Local-pairing token and CORS allow-list guarding {@code /api/**}. If no token is configured,
 * one is generated at startup and logged — good enough to print a first ticket via curl, but it
 * changes on every restart until M2 persists it and surfaces it in the admin UI.
 */
@ConfigurationProperties(prefix = "agent.security")
public class AgentSecurityProperties {

    private static final Logger LOG = LoggerFactory.getLogger(AgentSecurityProperties.class);

    private String token = "";
    private List<String> corsAllowedOrigins = List.of();

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public List<String> getCorsAllowedOrigins() {
        return corsAllowedOrigins;
    }

    public void setCorsAllowedOrigins(List<String> corsAllowedOrigins) {
        this.corsAllowedOrigins = corsAllowedOrigins;
    }

    @PostConstruct
    void generateTokenIfMissing() {
        if (token == null || token.isBlank()) {
            token = UUID.randomUUID().toString();
            LOG.warn("agent.security.token no configurado: se generó uno nuevo para esta sesión: {}", token);
            LOG.warn("Este token cambiará en cada reinicio. Configuralo explícitamente en application.yml "
                    + "para pruebas repetibles.");
        }
    }
}

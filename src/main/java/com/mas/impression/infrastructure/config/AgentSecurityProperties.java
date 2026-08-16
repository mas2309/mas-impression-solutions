package com.mas.impression.infrastructure.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.UUID;

/**
 * Local-pairing token and CORS allow-list guarding {@code /api/**}. If no token is configured
 * here, a placeholder is generated so {@link #getToken()} is never blank; {@link AgentTokenInitializer}
 * then overwrites it with whatever is persisted on disk (or persists this fresh one), so the
 * value actually used survives restarts instead of invalidating every paired frontend each time.
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
            LOG.debug("agent.security.token no configurado, se generó un valor temporal: {}", token);
        }
    }
}

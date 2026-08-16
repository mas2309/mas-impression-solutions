package com.mas.impression.infrastructure.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Makes the agent's pairing token survive restarts. Without this, {@link AgentSecurityProperties}
 * would generate a brand-new random token every time the process started, silently invalidating
 * every frontend that had already been paired — which is exactly the kind of thing that looks
 * like a CORS/auth bug from the browser's side, but is really just a stale token.
 *
 * <p>Resolution order: an explicit {@code agent.security.token} in application.yml always wins;
 * otherwise the token persisted at {@code agent-token.txt} under the data directory is reused;
 * otherwise a new one is generated and written there for next time.
 */
@Component
public class AgentTokenInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(AgentTokenInitializer.class);
    private static final String FILE_NAME = "agent-token.txt";

    private final AgentSecurityProperties securityProperties;
    private final AgentDataProperties dataProperties;

    public AgentTokenInitializer(AgentSecurityProperties securityProperties, AgentDataProperties dataProperties) {
        this.securityProperties = securityProperties;
        this.dataProperties = dataProperties;
    }

    @PostConstruct
    void resolveToken() {
        Path file = dataProperties.resolve(FILE_NAME);
        try {
            if (loadPersistedToken(file)) {
                return;
            }
            persistToken(file, resolveTokenToPersist());
        } catch (IOException ex) {
            LOG.warn("No se pudo leer/escribir {}: el token no sobrevivirá un reinicio ({})",
                    file, ex.getMessage());
        }
    }

    private boolean loadPersistedToken(Path file) throws IOException {
        if (!Files.exists(file)) {
            return false;
        }
        String persisted = Files.readString(file, StandardCharsets.UTF_8).trim();
        if (persisted.isBlank()) {
            return false;
        }
        securityProperties.setToken(persisted);
        return true;
    }

    private String resolveTokenToPersist() {
        String configured = securityProperties.getToken();
        return configured.isBlank() ? UUID.randomUUID().toString() : configured;
    }

    private void persistToken(Path file, String token) throws IOException {
        Path parent = file.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.writeString(file, token, StandardCharsets.UTF_8);
        securityProperties.setToken(token);
        LOG.warn("Token del agente: {} (guardado en {} — se reutiliza en cada reinicio)", token, file);
    }
}

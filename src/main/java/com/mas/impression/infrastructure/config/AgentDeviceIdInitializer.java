package com.mas.impression.infrastructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Makes this install's device identity survive restarts. Without this, pairing with the ERP
 * backend (see {@code PrintAgentDeviceController} there, exposed to the frontend as
 * {@code GET /api/v1/device} here) would register a "new" device every time the process
 * restarted, letting a single machine silently consume unlimited slots against the tenant's
 * device cap — exactly the gap that motivated this whole feature.
 *
 * <p>Deliberately its own file, not folded into {@link AgentTokenInitializer}: losing or
 * rotating the auth token must never change which device this is, and vice versa.
 */
@Component
public class AgentDeviceIdInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(AgentDeviceIdInitializer.class);
    private static final String FILE_NAME = "device-id.txt";

    private final AgentDataProperties dataProperties;
    private String deviceId;

    public AgentDeviceIdInitializer(AgentDataProperties dataProperties) {
        this.dataProperties = dataProperties;
    }

    @PostConstruct
    void resolveDeviceId() {
        Path file = dataProperties.resolve(FILE_NAME);
        try {
            String persisted = loadPersisted(file);
            this.deviceId = persisted != null ? persisted : persistNew(file);
        } catch (IOException ex) {
            LOG.warn("No se pudo leer/escribir {}: el deviceId no sobrevivirá un reinicio ({})",
                    file, ex.getMessage());
            this.deviceId = UUID.randomUUID().toString();
        }
    }

    public String getDeviceId() {
        return deviceId;
    }

    private String loadPersisted(Path file) throws IOException {
        if (!Files.exists(file)) {
            return null;
        }
        String persisted = Files.readString(file, StandardCharsets.UTF_8).trim();
        return persisted.isBlank() ? null : persisted;
    }

    private String persistNew(Path file) throws IOException {
        String newId = UUID.randomUUID().toString();
        Path parent = file.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.writeString(file, newId, StandardCharsets.UTF_8);
        return newId;
    }
}

package com.mas.impression.infrastructure.persistence;

import com.mas.impression.domain.model.PrinterConnectionConfig;
import com.mas.impression.domain.port.PrinterConfigRepositoryPort;
import com.mas.impression.infrastructure.config.AgentDataProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Reads/writes the printer configuration as a single human-readable JSON file under the agent's
 * data directory. No database: this is one small object edited rarely through the admin UI, so a
 * flat file is simpler to install, back up, and reason about than a schema-migrated store.
 */
@Component
public class JsonPrinterConfigStore implements PrinterConfigRepositoryPort {

    private static final Logger LOG = LoggerFactory.getLogger(JsonPrinterConfigStore.class);
    private static final String FILE_NAME = "printer-config.json";

    private final ObjectMapper objectMapper;
    private final AgentDataProperties dataProperties;

    public JsonPrinterConfigStore(ObjectMapper objectMapper, AgentDataProperties dataProperties) {
        this.objectMapper = objectMapper;
        this.dataProperties = dataProperties;
    }

    @Override
    public Optional<PrinterConnectionConfig> load() {
        Path file = dataProperties.resolve(FILE_NAME);
        if (!Files.exists(file)) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(file.toFile(), PrinterConnectionConfig.class));
        } catch (RuntimeException ex) {
            LOG.warn("No se pudo leer {}, se usarán los valores por defecto: {}", file, ex.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public void save(PrinterConnectionConfig config) {
        Path file = dataProperties.resolve(FILE_NAME);
        try {
            DataFiles.ensureParentDirectoryExists(file);
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), config);
        } catch (IOException | RuntimeException ex) {
            throw new IllegalStateException("No se pudo escribir " + file, ex);
        }
    }
}

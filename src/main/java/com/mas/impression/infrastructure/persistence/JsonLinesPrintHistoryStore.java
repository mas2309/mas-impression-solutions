package com.mas.impression.infrastructure.persistence;

import com.mas.impression.domain.model.PrintJob;
import com.mas.impression.domain.port.PrintJobHistoryPort;
import com.mas.impression.infrastructure.config.AgentDataProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Append-only JSON Lines log of print attempts, one line per {@link PrintJob}. Deliberately not
 * a database: at this agent's scale (one till, dozens of sales a day) a flat file is trivial to
 * install, back up, and read by hand — and {@link #record} never throws, since a history-write
 * hiccup must never fail the print itself.
 */
@Component
public class JsonLinesPrintHistoryStore implements PrintJobHistoryPort {

    private static final Logger LOG = LoggerFactory.getLogger(JsonLinesPrintHistoryStore.class);
    private static final String FILE_NAME = "print-history.jsonl";

    private final ObjectMapper objectMapper;
    private final AgentDataProperties dataProperties;

    public JsonLinesPrintHistoryStore(ObjectMapper objectMapper, AgentDataProperties dataProperties) {
        this.objectMapper = objectMapper;
        this.dataProperties = dataProperties;
    }

    @Override
    public void record(PrintJob job) {
        Path file = dataProperties.resolve(FILE_NAME);
        try {
            DataFiles.ensureParentDirectoryExists(file);
            String line = objectMapper.writeValueAsString(job) + System.lineSeparator();
            Files.writeString(file, line, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException | RuntimeException ex) {
            LOG.warn("No se pudo registrar en el historial el intento de impresión {}: {}",
                    job.jobId(), ex.getMessage());
        }
    }

    @Override
    public List<PrintJob> recent(int limit) {
        Path file = dataProperties.resolve(FILE_NAME);
        if (!Files.exists(file)) {
            return List.of();
        }
        try {
            return readLast(file, limit);
        } catch (IOException | RuntimeException ex) {
            LOG.warn("No se pudo leer el historial de impresión: {}", ex.getMessage());
            return List.of();
        }
    }

    private List<PrintJob> readLast(Path file, int limit) throws IOException {
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        List<PrintJob> jobs = new ArrayList<>();
        for (String line : lines) {
            if (!line.isBlank()) {
                jobs.add(objectMapper.readValue(line, PrintJob.class));
            }
        }
        Collections.reverse(jobs);
        return jobs.size() > limit ? jobs.subList(0, limit) : jobs;
    }
}

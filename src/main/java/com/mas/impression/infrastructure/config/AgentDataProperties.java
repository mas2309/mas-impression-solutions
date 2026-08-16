package com.mas.impression.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Where this install keeps its local state (printer config, print history). A plain directory
 * of flat files, not a database — see {@code PrinterConfigRepositoryPort} /
 * {@code PrintJobHistoryPort} implementations for why.
 */
@ConfigurationProperties(prefix = "agent.data")
public class AgentDataProperties {

    private String directory = "./data";

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public Path directoryPath() {
        return Paths.get(directory);
    }

    public Path resolve(String fileName) {
        return directoryPath().resolve(fileName);
    }
}

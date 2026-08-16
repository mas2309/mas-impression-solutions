package com.mas.impression.domain.port;

import com.mas.impression.domain.model.PrinterConnectionConfig;

import java.util.Optional;

/**
 * Persists the admin-editable printer configuration across restarts. Empty when nothing has
 * been saved yet, in which case callers fall back to the packaged defaults.
 */
public interface PrinterConfigRepositoryPort {

    Optional<PrinterConnectionConfig> load();

    void save(PrinterConnectionConfig config);
}

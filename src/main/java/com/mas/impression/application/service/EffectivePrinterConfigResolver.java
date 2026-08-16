package com.mas.impression.application.service;

import com.mas.impression.domain.model.PrinterConnectionConfig;
import com.mas.impression.domain.port.PrinterConfigRepositoryPort;
import com.mas.impression.infrastructure.config.PrinterProperties;
import org.springframework.stereotype.Component;

/**
 * The config actually in effect right now: whatever was saved through the admin UI, or the
 * packaged {@code application.yml} defaults if nothing has been saved yet. Shared by every
 * service that needs to know "how do I currently reach the printer" so the fallback logic
 * lives in exactly one place.
 */
@Component
public class EffectivePrinterConfigResolver {

    private final PrinterConfigRepositoryPort repository;
    private final PrinterProperties defaults;

    public EffectivePrinterConfigResolver(PrinterConfigRepositoryPort repository, PrinterProperties defaults) {
        this.repository = repository;
        this.defaults = defaults;
    }

    public PrinterConnectionConfig resolve() {
        return repository.load().orElseGet(defaults::toConnectionConfig);
    }
}

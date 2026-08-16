package com.mas.impression.application.service;

import com.mas.impression.domain.model.PaperWidth;
import com.mas.impression.domain.model.PrinterConnectionConfig;
import com.mas.impression.domain.model.PrinterConnectionType;
import com.mas.impression.domain.port.PrinterConfigRepositoryPort;
import com.mas.impression.infrastructure.config.PrinterProperties;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EffectivePrinterConfigResolverTest {

    @Test
    void fallsBackToPackagedDefaultsWhenNothingSaved() {
        PrinterConfigRepositoryPort repository = mock(PrinterConfigRepositoryPort.class);
        when(repository.load()).thenReturn(Optional.empty());
        PrinterProperties defaults = new PrinterProperties();
        defaults.setPaperWidth(PaperWidth.MM_80);

        PrinterConnectionConfig resolved = new EffectivePrinterConfigResolver(repository, defaults).resolve();

        assertEquals(PaperWidth.MM_80, resolved.paperWidth());
    }

    @Test
    void prefersSavedConfigOverDefaults() {
        PrinterConfigRepositoryPort repository = mock(PrinterConfigRepositoryPort.class);
        PrinterConnectionConfig saved = new PrinterConnectionConfig(
                PrinterConnectionType.NETWORK, PaperWidth.MM_80, "192.168.1.50", 9100, 3000, null, null);
        when(repository.load()).thenReturn(Optional.of(saved));

        PrinterConnectionConfig resolved =
                new EffectivePrinterConfigResolver(repository, new PrinterProperties()).resolve();

        assertEquals(saved, resolved);
    }
}

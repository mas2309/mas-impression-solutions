package com.mas.impression.application.service;

import com.mas.impression.application.dto.PrinterStatusResponse;
import com.mas.impression.domain.model.PaperWidth;
import com.mas.impression.domain.model.PrinterConnectionType;
import com.mas.impression.domain.model.PrinterState;
import com.mas.impression.domain.port.PrinterConfigRepositoryPort;
import com.mas.impression.domain.port.PrinterPort;
import com.mas.impression.infrastructure.config.PrinterProperties;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PrinterStatusServiceTest {

    @Test
    void combinesPrinterStateWithConfiguredProperties() {
        PrinterPort printerPort = mock(PrinterPort.class);
        LocalDateTime lastPrintAt = LocalDateTime.of(2026, 8, 16, 9, 0);
        when(printerPort.status()).thenReturn(
                new PrinterState(true, PrinterConnectionType.NETWORK, null, lastPrintAt));

        PrinterConfigRepositoryPort repository = mock(PrinterConfigRepositoryPort.class);
        when(repository.load()).thenReturn(Optional.empty());
        PrinterProperties printerProperties = new PrinterProperties();
        printerProperties.setPaperWidth(PaperWidth.MM_80);
        EffectivePrinterConfigResolver configResolver =
                new EffectivePrinterConfigResolver(repository, printerProperties);

        PrinterStatusService service = new PrinterStatusService(printerPort, configResolver);
        PrinterStatusResponse response = service.getStatus();

        assertEquals(true, response.connected());
        assertEquals(PrinterConnectionType.NETWORK, response.transport());
        assertEquals(PaperWidth.MM_80, response.paperWidth());
        assertEquals(lastPrintAt, response.lastPrintAt());
    }
}

package com.mas.impression.application.service;

import com.mas.impression.application.dto.PrinterConfigRequest;
import com.mas.impression.application.dto.PrinterConfigResponse;
import com.mas.impression.domain.model.PaperWidth;
import com.mas.impression.domain.model.PrinterConnectionConfig;
import com.mas.impression.domain.model.PrinterConnectionType;
import com.mas.impression.domain.port.PrinterConfigRepositoryPort;
import com.mas.impression.infrastructure.config.PrinterProperties;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PrinterConfigServiceTest {

    private final PrinterConfigRepositoryPort repository = mock(PrinterConfigRepositoryPort.class);
    private final PrinterConfigService service = new PrinterConfigService(
            repository, new EffectivePrinterConfigResolver(repository, new PrinterProperties()));

    @Test
    void getConfigReturnsPackagedDefaultsWhenNothingSaved() {
        when(repository.load()).thenReturn(Optional.empty());

        PrinterConfigResponse response = service.getConfig();

        assertEquals(PrinterConnectionType.NETWORK, response.connectionType());
        assertEquals(PaperWidth.MM_58, response.paperWidth());
    }

    @Test
    void updateConfigSavesAndEchoesTheNewValue() {
        PrinterConfigRequest request = new PrinterConfigRequest(
                PrinterConnectionType.NETWORK, PaperWidth.MM_80, "192.168.1.50", 9100, 3000);

        PrinterConfigResponse response = service.updateConfig(request);

        verify(repository).save(new PrinterConnectionConfig(
                PrinterConnectionType.NETWORK, PaperWidth.MM_80, "192.168.1.50", 9100, 3000));
        assertEquals("192.168.1.50", response.host());
        assertEquals(PaperWidth.MM_80, response.paperWidth());
    }
}

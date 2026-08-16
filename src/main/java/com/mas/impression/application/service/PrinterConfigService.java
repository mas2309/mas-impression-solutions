package com.mas.impression.application.service;

import com.mas.impression.application.dto.PrinterConfigRequest;
import com.mas.impression.application.dto.PrinterConfigResponse;
import com.mas.impression.application.port.in.ConfigurePrinterUseCase;
import com.mas.impression.domain.model.PrinterConnectionConfig;
import com.mas.impression.domain.port.PrinterConfigRepositoryPort;
import org.springframework.stereotype.Service;

@Service
public class PrinterConfigService implements ConfigurePrinterUseCase {

    private final PrinterConfigRepositoryPort repository;
    private final EffectivePrinterConfigResolver configResolver;

    public PrinterConfigService(PrinterConfigRepositoryPort repository, EffectivePrinterConfigResolver configResolver) {
        this.repository = repository;
        this.configResolver = configResolver;
    }

    @Override
    public PrinterConfigResponse getConfig() {
        return toResponse(configResolver.resolve());
    }

    @Override
    public PrinterConfigResponse updateConfig(PrinterConfigRequest request) {
        PrinterConnectionConfig config = new PrinterConnectionConfig(
                request.connectionType(), request.paperWidth(), request.host(),
                request.port(), request.connectTimeoutMillis(),
                request.usbPrintServiceName(), request.bluetoothPortName());
        repository.save(config);
        return toResponse(config);
    }

    private static PrinterConfigResponse toResponse(PrinterConnectionConfig config) {
        return new PrinterConfigResponse(
                config.connectionType(), config.paperWidth(), config.host(),
                config.port(), config.connectTimeoutMillis(),
                config.usbPrintServiceName(), config.bluetoothPortName());
    }
}

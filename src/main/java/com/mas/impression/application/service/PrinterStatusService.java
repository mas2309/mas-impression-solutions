package com.mas.impression.application.service;

import com.mas.impression.application.dto.PrinterStatusResponse;
import com.mas.impression.application.port.in.GetPrinterStatusUseCase;
import com.mas.impression.domain.model.PrinterConnectionConfig;
import com.mas.impression.domain.model.PrinterState;
import com.mas.impression.domain.port.PrinterPort;
import com.mas.impression.domain.port.PrinterPortResolver;
import org.springframework.stereotype.Service;

@Service
public class PrinterStatusService implements GetPrinterStatusUseCase {

    private final PrinterPortResolver adapterResolver;
    private final EffectivePrinterConfigResolver configResolver;

    public PrinterStatusService(PrinterPortResolver adapterResolver, EffectivePrinterConfigResolver configResolver) {
        this.adapterResolver = adapterResolver;
        this.configResolver = configResolver;
    }

    @Override
    public PrinterStatusResponse getStatus() {
        PrinterConnectionConfig config = configResolver.resolve();
        PrinterPort printerPort = adapterResolver.resolve(config.connectionType());
        PrinterState state = printerPort.status();
        return new PrinterStatusResponse(
                state.connected(), config.connectionType(), config.paperWidth(),
                state.lastError(), state.lastPrintAt());
    }
}

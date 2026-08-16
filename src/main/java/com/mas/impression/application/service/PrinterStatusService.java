package com.mas.impression.application.service;

import com.mas.impression.application.dto.PrinterStatusResponse;
import com.mas.impression.application.port.in.GetPrinterStatusUseCase;
import com.mas.impression.domain.model.PrinterConnectionConfig;
import com.mas.impression.domain.model.PrinterState;
import com.mas.impression.domain.port.PrinterPort;
import org.springframework.stereotype.Service;

@Service
public class PrinterStatusService implements GetPrinterStatusUseCase {

    private final PrinterPort printerPort;
    private final EffectivePrinterConfigResolver configResolver;

    public PrinterStatusService(PrinterPort printerPort, EffectivePrinterConfigResolver configResolver) {
        this.printerPort = printerPort;
        this.configResolver = configResolver;
    }

    @Override
    public PrinterStatusResponse getStatus() {
        PrinterState state = printerPort.status();
        PrinterConnectionConfig config = configResolver.resolve();
        return new PrinterStatusResponse(
                state.connected(), config.connectionType(), config.paperWidth(),
                state.lastError(), state.lastPrintAt());
    }
}

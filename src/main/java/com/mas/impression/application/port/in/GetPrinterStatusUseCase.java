package com.mas.impression.application.port.in;

import com.mas.impression.application.dto.PrinterStatusResponse;

public interface GetPrinterStatusUseCase {

    PrinterStatusResponse getStatus();
}

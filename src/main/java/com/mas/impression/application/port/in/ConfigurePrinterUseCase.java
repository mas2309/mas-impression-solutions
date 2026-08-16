package com.mas.impression.application.port.in;

import com.mas.impression.application.dto.PrinterConfigRequest;
import com.mas.impression.application.dto.PrinterConfigResponse;

public interface ConfigurePrinterUseCase {

    PrinterConfigResponse getConfig();

    PrinterConfigResponse updateConfig(PrinterConfigRequest request);
}

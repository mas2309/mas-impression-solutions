package com.mas.impression.application.dto;

import com.mas.impression.domain.model.PaperWidth;
import com.mas.impression.domain.model.PrinterConnectionType;

public record PrinterConfigResponse(
        PrinterConnectionType connectionType,
        PaperWidth paperWidth,
        String host,
        int port,
        int connectTimeoutMillis) {
}

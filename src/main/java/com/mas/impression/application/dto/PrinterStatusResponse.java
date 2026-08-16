package com.mas.impression.application.dto;

import com.mas.impression.domain.model.PaperWidth;
import com.mas.impression.domain.model.PrinterConnectionType;

import java.time.LocalDateTime;

public record PrinterStatusResponse(
        boolean connected,
        PrinterConnectionType transport,
        PaperWidth paperWidth,
        String lastError,
        LocalDateTime lastPrintAt) {
}

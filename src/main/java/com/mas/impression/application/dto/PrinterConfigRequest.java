package com.mas.impression.application.dto;

import com.mas.impression.domain.model.PaperWidth;
import com.mas.impression.domain.model.PrinterConnectionType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Admin-editable printer configuration, submitted from the {@code /admin/config} form.
 */
public record PrinterConfigRequest(
        @NotNull PrinterConnectionType connectionType,
        @NotNull PaperWidth paperWidth,
        @NotBlank String host,
        @Min(1) @Max(65535) int port,
        @Min(100) int connectTimeoutMillis) {
}

package com.mas.impression.application.dto;

import com.mas.impression.domain.model.PrintStatus;

import java.time.LocalDateTime;

public record PrintJobResponse(
        String jobId,
        String saleNumber,
        PrintStatus status,
        String message,
        LocalDateTime createdAt,
        LocalDateTime printedAt) {
}

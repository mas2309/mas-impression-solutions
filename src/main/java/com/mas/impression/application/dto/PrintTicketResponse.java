package com.mas.impression.application.dto;

import com.mas.impression.domain.model.PrintStatus;

import java.time.LocalDateTime;

public record PrintTicketResponse(
        String jobId,
        PrintStatus status,
        String message,
        LocalDateTime printedAt) {
}

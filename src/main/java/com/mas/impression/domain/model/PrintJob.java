package com.mas.impression.domain.model;

import java.time.LocalDateTime;

/**
 * A single print attempt, kept for the admin UI's history view and for troubleshooting.
 * Not the source of truth for anything the print flow itself depends on.
 */
public record PrintJob(
        String jobId,
        String saleNumber,
        PrintStatus status,
        String message,
        LocalDateTime createdAt,
        LocalDateTime printedAt) {
}

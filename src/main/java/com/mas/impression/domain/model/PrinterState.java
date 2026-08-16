package com.mas.impression.domain.model;

import java.time.LocalDateTime;

/**
 * Point-in-time snapshot of the configured printer's reachability, used to answer
 * {@code GET /api/v1/status} so the POS UI can show a "printer offline" badge.
 *
 * @param connected     whether the last connection attempt succeeded
 * @param transport     configured transport
 * @param lastError     message from the last failed connection/write attempt, if any
 * @param lastPrintAt   timestamp of the last successful print, if any
 */
public record PrinterState(
        boolean connected,
        PrinterConnectionType transport,
        String lastError,
        LocalDateTime lastPrintAt) {
}

package com.mas.impression.domain.model;

/**
 * Where and how to reach the physical printer. In M1 this is sourced from static
 * application properties; later milestones source it from persisted, admin-editable config.
 * The shape stays the same either way, which is what keeps {@code PrinterPort} implementations
 * decoupled from where their configuration comes from.
 *
 * @param connectionType    transport to use
 * @param paperWidth        paper width to format tickets for
 * @param host              target host, only meaningful for {@link PrinterConnectionType#NETWORK}
 * @param port              target port, only meaningful for {@link PrinterConnectionType#NETWORK}
 * @param connectTimeoutMillis socket connect timeout
 */
public record PrinterConnectionConfig(
        PrinterConnectionType connectionType,
        PaperWidth paperWidth,
        String host,
        int port,
        int connectTimeoutMillis) {
}

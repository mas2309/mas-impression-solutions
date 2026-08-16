package com.mas.impression.domain.model;

/**
 * Where and how to reach the physical printer. Only the fields relevant to
 * {@link #connectionType()} are meaningful at any given time; the others are ignored.
 *
 * @param connectionType        transport to use
 * @param paperWidth            paper width to format tickets for
 * @param host                  target host, only meaningful for {@link PrinterConnectionType#NETWORK}
 * @param port                  target port, only meaningful for {@link PrinterConnectionType#NETWORK}
 * @param connectTimeoutMillis  socket connect timeout, only meaningful for
 *                              {@link PrinterConnectionType#NETWORK}
 * @param usbPrintServiceName   name of the Windows-registered print queue to send raw bytes to,
 *                              only meaningful for {@link PrinterConnectionType#USB}
 * @param bluetoothPortName     paired COM port (e.g. {@code "COM5"}), only meaningful for
 *                              {@link PrinterConnectionType#BLUETOOTH}
 */
public record PrinterConnectionConfig(
        PrinterConnectionType connectionType,
        PaperWidth paperWidth,
        String host,
        int port,
        int connectTimeoutMillis,
        String usbPrintServiceName,
        String bluetoothPortName) {
}

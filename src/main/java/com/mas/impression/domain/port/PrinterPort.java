package com.mas.impression.domain.port;

import com.mas.impression.domain.exception.PrinterNotConnectedException;
import com.mas.impression.domain.model.PrinterConnectionConfig;
import com.mas.impression.domain.model.PrinterState;

/**
 * Driven port for reaching the physical printer. Every transport (network, USB, Bluetooth)
 * implements this and only this; application/domain code never depends on a concrete transport,
 * which is what makes {@code PrintTicketService} testable without real hardware.
 */
public interface PrinterPort {

    /**
     * Opens whatever connection the transport needs for a single print attempt.
     *
     * @throws PrinterNotConnectedException if the printer cannot be reached
     */
    void connect(PrinterConnectionConfig config);

    /**
     * Writes raw ESC/POS bytes to the already-connected printer.
     *
     * @throws PrinterNotConnectedException if writing fails
     */
    void write(byte[] data);

    /**
     * Reports the last known reachability of the configured printer. Must not throw.
     */
    PrinterState status();

    /**
     * Releases whatever resources {@link #connect} opened. Safe to call even if not connected.
     */
    void disconnect();
}

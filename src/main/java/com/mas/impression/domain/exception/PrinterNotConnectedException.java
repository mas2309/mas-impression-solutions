package com.mas.impression.domain.exception;

/**
 * Thrown when the configured printer cannot be reached or a write to it fails.
 */
public class PrinterNotConnectedException extends PrintAgentException {

    private static final long serialVersionUID = 1L;

    private static final String CODE = "PRINTER_NOT_CONNECTED";

    public PrinterNotConnectedException(String message) {
        super(CODE, message);
    }

    public PrinterNotConnectedException(String message, Throwable cause) {
        super(CODE, message, cause);
    }
}

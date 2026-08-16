package com.mas.impression.domain.exception;

/**
 * Thrown when the agent is configured with a paper width it does not know how to format for.
 */
public class UnsupportedPaperWidthException extends PrintAgentException {

    private static final long serialVersionUID = 1L;

    private static final String CODE = "UNSUPPORTED_PAPER_WIDTH";

    public UnsupportedPaperWidthException(String message) {
        super(CODE, message);
    }
}

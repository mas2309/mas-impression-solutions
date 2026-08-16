package com.mas.impression.domain.exception;

/**
 * Base for all domain-level failures in the print agent, carrying a stable machine-readable
 * code alongside the human message (mirrors the backend's {@code BusinessException} shape).
 */
public class PrintAgentException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String code;

    public PrintAgentException(String code, String message) {
        super(message);
        this.code = code;
    }

    public PrintAgentException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}

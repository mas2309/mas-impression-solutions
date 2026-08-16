package com.mas.impression.infrastructure.rest.advice;

import com.mas.impression.domain.exception.PrintAgentException;
import com.mas.impression.infrastructure.rest.PrintController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Scoped to the JSON API controllers only ({@code basePackageClasses = PrintController.class}) —
 * an unscoped {@code @RestControllerAdvice} also intercepts exceptions from the Thymeleaf admin
 * controller and from framework internals like static-resource lookups (e.g. the browser's
 * automatic {@code /favicon.ico} request throws {@code NoResourceFoundException}), which would
 * otherwise get logged here as a scary "unexpected error" for something completely benign.
 */
@RestControllerAdvice(basePackageClasses = PrintController.class)
public class GlobalExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(PrintAgentException.class)
    public ResponseEntity<ApiError> handlePrintAgentException(PrintAgentException ex) {
        LOG.warn("Fallo de dominio [{}]: {}", ex.getCode(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiError.of(HttpStatus.SERVICE_UNAVAILABLE.value(), ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationException(MethodArgumentNotValidException ex) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest()
                .body(ApiError.of(HttpStatus.BAD_REQUEST.value(), "VALIDATION_ERROR",
                        "La solicitud contiene datos inválidos.", details));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpectedException(Exception ex) {
        LOG.error("Error inesperado", ex);
        return ResponseEntity.internalServerError()
                .body(ApiError.of(HttpStatus.INTERNAL_SERVER_ERROR.value(), "INTERNAL_ERROR",
                        "Ocurrió un error inesperado en el agente de impresión."));
    }
}

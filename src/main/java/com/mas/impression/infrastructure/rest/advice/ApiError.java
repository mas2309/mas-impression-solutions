package com.mas.impression.infrastructure.rest.advice;

import java.time.LocalDateTime;

public record ApiError(
        int status,
        String code,
        String message,
        String details,
        LocalDateTime timestamp) {

    public static ApiError of(int status, String code, String message) {
        return new ApiError(status, code, message, null, LocalDateTime.now());
    }

    public static ApiError of(int status, String code, String message, String details) {
        return new ApiError(status, code, message, details, LocalDateTime.now());
    }
}

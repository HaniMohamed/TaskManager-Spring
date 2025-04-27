package com.taskmanager.dto;


import java.time.LocalDateTime;

public record ErrorResponseDTO(
        String message,
        String errorCode,
        LocalDateTime timestamp
) {
    public ErrorResponseDTO(String message, String errorCode) {
        this(message, errorCode, LocalDateTime.now());
    }
}


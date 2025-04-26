package com.taskmanager.dto;

import jakarta.validation.constraints.NotBlank;

public record TaskDTO(
        @NotBlank(message = "Title is required")
        String title,

        String description,

        @NotBlank(message = "Status is required")
        String status
) {
}
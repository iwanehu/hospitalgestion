package com.hospital.gestion.api.ward.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record WardRequestDTO(
        @NotBlank(message = "name is required")
        @Size(max = 100)
        String name,


        @Size(max = 500)
        String description,

        @NotNull(message = "department ID is required")
        Long departmentId
) {
}

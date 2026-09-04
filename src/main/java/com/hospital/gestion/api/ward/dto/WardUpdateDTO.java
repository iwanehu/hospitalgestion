package com.hospital.gestion.api.ward.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WardUpdateDTO(
        @NotBlank(message = "name is required")
        @Size(max = 100)
        String name,

        @Size(max = 500)
        String description,

        Boolean isActive
) {
}

package com.hospital.gestion.api.department.dto;

import com.hospital.gestion.api.common.enums.DepartmentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DepartmentRequestDTO(


        @NotNull(message = "Department type is required")
        DepartmentType departmentType,

        @NotBlank(message = "Location is required")
        @Size(max = 100, message = "Location must be less than 100 characters")
        String location,

        @Size(max = 10, message = "Phone extension must be less than 10 characters")
        String phoneExtension,

        @Size(max = 500, message = "Description must be less than 500 characters")
        String description
) {
}

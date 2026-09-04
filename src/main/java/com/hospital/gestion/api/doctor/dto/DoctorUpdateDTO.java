package com.hospital.gestion.api.doctor.dto;

import com.hospital.gestion.api.common.enums.Specialty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DoctorUpdateDTO(


        @NotNull(message = "Department ID is required")
        Long departmentId,

        @NotNull(message = "Specialty is required")
        Specialty specialty,


        @NotNull(message = "Years of experience is required")
        @Min(value = 0, message = "years of experience must be at least 0")
        Integer yearsOfExperience,

        @Size(max = 500, message = "Biography must be less than 500 characters")
        String biography
) {}

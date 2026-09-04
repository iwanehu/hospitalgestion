package com.hospital.gestion.api.nurse.dto;

import com.hospital.gestion.api.common.enums.NurseSpecialty;
import com.hospital.gestion.api.common.enums.ShiftType;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record NurseUpdateDTO(

        @NotNull(message = "Department ID is required")
        Long departmentId,
        @NotNull(message = "Specialty is required")
        NurseSpecialty specialty,
        @NotNull(message = "Shift type is required")
        ShiftType shiftType,

        @NotNull(message = "Years of experience is required")
        @Min(value = 0, message = "Years of experience must be at least 0")
        @Max(
                value = 60,
                message = "Years of experience must be at most 60"
        )
        Integer yearsOfExperience,
        @PastOrPresent(
                message = "Hire date cannot be in the future"
        )
        LocalDate hireDate,

        @Size(max = 500, message = "Biography must be less than 500 characters")
        String biography,

        // Contacto de emergencia (Embeddable)
        @Size(max = 150, message = "Emergency contact name must be less than 150 characters")
        String emergencyContactName,

        @Size(max = 20, message = "Emergency contact phone must be less than 20 characters")
        String emergencyContactPhone,

        @Size(max = 50, message = "Emergency contact relationship must be less than 50 characters")
        String emergencyContactRelationship,

        @Min(value = 0, message = "Max patients per shift must be at least 0")
        @Max(
                value = 20,
                message = "Max patients per shift must be at most 20"
        )
        Integer maxPatientsPerShift,

        Boolean isChargeNurse,

        @Min(value = 0, message = "Vacation days must be at least 0")
        @Max(
                value = 60,
                message = "Vacation days available must be at most 60"
        )
        Integer vacationDaysAvailable
) {}
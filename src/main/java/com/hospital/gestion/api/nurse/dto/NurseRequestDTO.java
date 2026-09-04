package com.hospital.gestion.api.nurse.dto;

import com.hospital.gestion.api.common.enums.NurseSpecialty;
import com.hospital.gestion.api.common.enums.ShiftType;
import com.hospital.gestion.api.common.validation.ValidLicenceNumber;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record NurseRequestDTO(

        @NotNull(message = "User ID is required")
        Long userId,

        @NotNull(message = "Department ID is required")
        Long departmentId,

        @NotNull(message = "License number is required")
        @ValidLicenceNumber
        @Size(max = 50, message = "License number must be less than 50 characters")
        String licenseNumber,

        @NotNull(message = "Specialty is required")
        NurseSpecialty specialty,

        @NotNull(message = "Shift type is required")
        ShiftType shiftType,

        @NotNull(message = "Years of experience is required")
        @Min(
                value = 0,
                message = "Years of experience must be at least 0"
        )
        @Max(
                value = 60,
                message = "Years of experience must be at most 60"
        )
        Integer yearsOfExperience,

        LocalDate hireDate,

        @Size(max = 500, message = "Biography must be less than 500 characters")
        String biography,

        @NotNull(message = "Emergency contact name is required")
        @Size(max = 150, message = "Emergency contact name must be less than 150 characters")
        String emergencyContactName,

        @NotNull(message = "Emergency contact phone is required")
        @Size(max = 20, message = "Emergency contact phone must be less than 20 characters")
        String emergencyContactPhone,

        @NotNull(message = "Emergency contact relationship is required")
        @Size(max = 50, message = "Emergency contact relationship must be less than 50 characters")
        String emergencyContactRelationship,

        @NotNull(message = "Max patients per shift is required")
        @Min(
                value = 0,
                message = "Max patients per shift must be at least 0"
        )
        @Max(
                value = 20,
                message = "Max patients per shift must be at most 20"
        )
        Integer maxPatientsPerShift,

        @NotNull(message = "Charge nurse flag is required")
        Boolean isChargeNurse,

        @NotNull(message = "Vacation days available is required")
        @Min(
                value = 0,
                message = "Vacation days must be at least 0"
        )
        @Max(
                value = 60,
                message = "Vacation days available must be at most 60"
        )
        Integer vacationDaysAvailable
) {}

package com.hospital.gestion.api.patient.dto;

import com.hospital.gestion.api.common.enums.BloodType;
import com.hospital.gestion.api.common.validation.ValidBirthDate;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;


public record PatientRequestDTO(




        @NotNull(message = "User ID is required")
        Long userId,

        BloodType bloodType,

        @NotNull(message = "Birth date is required")
        @ValidBirthDate
        LocalDate birthDate,







        String emergencyContactName,
        String emergencyContactPhone,
        String emergencyContactRelationship,


        @Size(max = 500, message = "Allergies must be less than 500 characters")
        String allergies,

        Boolean hasHealthInsurance,
        String healthInsuranceProvider,
        String healthInsuranceNumber,

        @Size(max = 1000, message = "Medical history must be less than 1000 characters")
        String medicalHistory
) {}

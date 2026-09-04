package com.hospital.gestion.api.patient.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PatientResponseDTO(
        Long id,


        Long userId,
        String fullName,
        String email,
        String documentId,
        String phone,



        String bloodType,
        LocalDate birthDate,


        String emergencyContactName,
        String emergencyContactPhone,
        String emergencyContactRelationship,

        String allergies,

        Boolean hasHealthInsurance,
        String healthInsuranceProvider,
        String healthInsuranceNumber,

        String medicalHistory,

        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

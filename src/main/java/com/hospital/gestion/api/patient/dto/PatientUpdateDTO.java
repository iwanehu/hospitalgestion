package com.hospital.gestion.api.patient.dto;

import com.hospital.gestion.api.common.enums.BloodType;

import jakarta.validation.constraints.Size;


public record PatientUpdateDTO(



        BloodType bloodType,



        // Emergency contact
        @Size(max = 150, message = "Emergency contact name must be less than 150 characters")
        String emergencyContactName,

        @Size(max = 20, message = "Emergency contact phone must be less than 20 characters")
        String emergencyContactPhone,

        @Size(max = 50, message = "Emergency contact relationship must be less than 50 characters")
        String emergencyContactRelationship,


        @Size(max = 500, message = "Allergies must be less than 500 characters")
        String allergies,

        // Health insurance
        Boolean hasHealthInsurance,

        @Size(max = 100, message = "Insurance provider must be less than 100 characters")
        String healthInsuranceProvider,

        @Size(max = 50, message = "Insurance number must be less than 50 characters")
        String healthInsuranceNumber,

        @Size(max = 1000, message = "Medical history must be less than 1000 characters")
        String medicalHistory
) {
}
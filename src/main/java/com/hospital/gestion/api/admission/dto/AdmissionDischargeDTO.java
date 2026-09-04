package com.hospital.gestion.api.admission.dto;

import jakarta.validation.constraints.Size;

public record AdmissionDischargeDTO(
        @Size(
                max = 1000,
                message = "Discharge notes must be less than 1000 characters"
        )
        String notes
) {
}

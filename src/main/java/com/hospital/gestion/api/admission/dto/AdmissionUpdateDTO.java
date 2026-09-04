package com.hospital.gestion.api.admission.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdmissionUpdateDTO(
        @NotNull(message = "Attending doctor ID is required")
        Long attendingDoctorId,

        @Size(
                max = 255,
                message = "Admission reason must be less than 255 characters"
        )
        String admissionReason,

        @Size(
                max = 1000,
                message = "Notes must be less than 1000 characters"
        )
        String notes
) {
}

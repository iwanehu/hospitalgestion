package com.hospital.gestion.api.admission.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdmissionRequestDTO(
        @NotNull(message = "Patient ID is required")
        Long patientId,

        @NotNull(message = "Bed ID is required")
        Long bedId,

        @NotNull(message = "Attending doctor ID is required")
        Long attendingDoctorId,

        @NotBlank(message = "Admission reason is required")
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

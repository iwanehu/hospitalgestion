package com.hospital.gestion.api.admission.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdmissionTransferDTO(
        @NotNull(message = "New bed ID is required")
        Long newBedId,

        @Size(
                max = 500,
                message = "Transfer reason must be less than 500 characters"
        )
        String reason
) {
}

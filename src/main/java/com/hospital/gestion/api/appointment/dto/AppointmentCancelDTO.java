package com.hospital.gestion.api.appointment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AppointmentCancelDTO(


        @NotBlank
        @Size(max = 200)
        String reason
) {
}

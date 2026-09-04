package com.hospital.gestion.api.appointment.dto;

import com.hospital.gestion.api.common.enums.AppointmentStatus;
import jakarta.validation.constraints.NotNull;

public record AppointmentStatusUpdateDTO(
        @NotNull(message = "Status is required")
        AppointmentStatus status


) {
}

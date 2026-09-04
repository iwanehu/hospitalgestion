package com.hospital.gestion.api.appointment.dto;

import com.hospital.gestion.api.common.enums.AppointmentStatus;

public record AppointmentCountResponse(
        Long doctorId,
        AppointmentStatus status,
        long count
) {
}

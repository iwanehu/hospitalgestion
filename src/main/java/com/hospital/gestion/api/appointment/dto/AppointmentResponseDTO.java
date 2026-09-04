package com.hospital.gestion.api.appointment.dto;

import com.hospital.gestion.api.common.enums.AppointmentStatus;

import java.time.LocalDateTime;

public record AppointmentResponseDTO(
        Long id,

        Long doctorId,
        String doctorName,

        Long patientId,
        String patientName,

        Long roomId,
        String roomNumber,

        LocalDateTime dateTime,

        String reason,
        String notes,
        AppointmentStatus status,

        String cancellationReason,

        LocalDateTime cancelledAt,
        LocalDateTime confirmedAt,
        LocalDateTime completedAt,

        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

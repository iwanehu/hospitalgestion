package com.hospital.gestion.api.appointment.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public record AppointmentRequestDTO(
        @NotNull(message = "Doctor ID is required")
        Long doctorId,

        @NotNull(message = "Patient ID is required")
        Long patientId,


        Long roomId,

        @NotNull(message = "Date and time is required")
        @Future(message = "Appointment date must be in the future")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime dateTime,


        @NotBlank(message = "Reason is required")
        @Size(max = 200, message = "Reason must be less than 200 characters")
        String reason,

        @Size(max = 500, message = "Notes must be less than 500 characters")
        String notes
) {
}

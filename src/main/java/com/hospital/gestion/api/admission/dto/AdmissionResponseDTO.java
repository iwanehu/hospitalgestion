package com.hospital.gestion.api.admission.dto;

import java.time.LocalDateTime;

public record AdmissionResponseDTO(
        Long id,

        // Patient
        Long patientId,
        String patientName,

        // Doctor
        Long attendingDoctorId,
        String attendingDoctorName,

        // Bed
        Long bedId,
        String bedNumber,

        // Room
        Long roomId,
        String roomNumber,

        // Ward
        Long wardId,
        String wardName,

        // Department
        Long departmentId,
        String departmentType,

        // Admission
        String status,
        String admissionReason,

        LocalDateTime admittedAt,
        LocalDateTime dischargedAt,

        String notes,

        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

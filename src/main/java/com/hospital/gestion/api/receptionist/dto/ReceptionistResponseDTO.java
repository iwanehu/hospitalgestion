package com.hospital.gestion.api.receptionist.dto;

import com.hospital.gestion.api.common.enums.DepartmentType;
import com.hospital.gestion.api.common.enums.ShiftType;

import java.time.LocalDateTime;

public record ReceptionistResponseDTO(
        Long id,

        Long userId,
        String fullName,
        String email,


        Long departmentId,
        DepartmentType departmentType,

        String deskNumber,
        ShiftType shiftType,


        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

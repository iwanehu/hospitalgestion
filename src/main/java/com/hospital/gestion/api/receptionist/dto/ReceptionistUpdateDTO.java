package com.hospital.gestion.api.receptionist.dto;

import com.hospital.gestion.api.common.enums.ShiftType;
import com.hospital.gestion.api.common.validation.ValidDeskNumber;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ReceptionistUpdateDTO(
        @NotNull(message = "Department ID is required")
        @Positive(message = "Department ID must be greater than zero")
        Long departmentId,

        @ValidDeskNumber
        @NotBlank(message = "Desk number is required")
        String deskNumber,

        @NotNull(message = "Shift type is required")
        ShiftType shiftType
) {
}

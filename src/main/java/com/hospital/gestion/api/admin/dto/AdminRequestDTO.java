package com.hospital.gestion.api.admin.dto;

import com.hospital.gestion.api.common.enums.AdminLevel;
import com.hospital.gestion.api.common.enums.AdminPermission;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record AdminRequestDTO(

        @NotNull(message = "User ID is required")
        @Positive(message = "User ID must be greater than zero")
        Long userId,

        @NotNull(message = "Admin level is required")
        AdminLevel adminLevel,

        @Positive(
                message = "Department ID must be greater than zero"
        )
        Long departmentId,


        @NotNull(message = "Permissions are required")
        @Size(
                min = 1,
                message = "At least one permission is required"
        )
        List<AdminPermission> permissions

) {
}
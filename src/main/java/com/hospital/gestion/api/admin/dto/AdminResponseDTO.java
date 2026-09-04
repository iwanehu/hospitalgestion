package com.hospital.gestion.api.admin.dto;

import com.hospital.gestion.api.common.enums.AdminLevel;
import com.hospital.gestion.api.common.enums.AdminPermission;
import com.hospital.gestion.api.common.enums.DepartmentType;

import java.time.LocalDateTime;
import java.util.List;

public record AdminResponseDTO(
        Long id,

        Long userId,
        String fullName,
        String email,


        AdminLevel adminLevel,

        Long departmentId,
        DepartmentType departmentType,

        List<AdminPermission> permissions,


        LocalDateTime lastLogin,
        Boolean isSuperAdmin,

        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

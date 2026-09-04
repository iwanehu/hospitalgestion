package com.hospital.gestion.api.department.dto;


import com.hospital.gestion.api.common.enums.DepartmentType;

import java.time.LocalDateTime;

public record DepartmentResponseDTO(
        Long id,
        DepartmentType departmentType,
        String location,
        String phoneExtension,
        String description,
        Boolean isActive,

        Integer totalWards,


        LocalDateTime createdAt,
        LocalDateTime updatedAt)
{}

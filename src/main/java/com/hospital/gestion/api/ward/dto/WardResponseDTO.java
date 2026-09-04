package com.hospital.gestion.api.ward.dto;

import java.time.LocalDateTime;

public record WardResponseDTO(
        Long id,
        String name,
        String description,
        Boolean isActive,

        Long departmentId,
        String departmentType,

        Integer totalRooms,

        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {
}

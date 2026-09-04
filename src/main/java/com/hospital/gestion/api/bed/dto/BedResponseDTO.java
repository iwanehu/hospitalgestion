package com.hospital.gestion.api.bed.dto;

import java.time.LocalDateTime;

public record BedResponseDTO(

        Long id,
        String bedNumber,

        String status,
        Boolean isOccupied,

        Long roomId,
        String roomNumber,
        Integer roomFloor,
        String roomType,

        Long wardId,
        String wardName,

        Long departmentId,
        String departmentType,

        String notes,

        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {
}
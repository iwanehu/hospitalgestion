package com.hospital.gestion.api.room.dto;

import com.hospital.gestion.api.common.enums.RoomStatus;
import com.hospital.gestion.api.common.enums.RoomType;

import java.time.LocalDateTime;

public record RoomResponseDTO(
        Long id,
        String number,
        Integer floor,

        RoomType roomType,
        RoomStatus status,

        Integer capacity,


        Integer totalBeds,
        Long availableBeds,
        Long occupiedBeds,

        Long wardId,
        String wardName,

        Long departmentId,
        String departmentType,

        String notes,

        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

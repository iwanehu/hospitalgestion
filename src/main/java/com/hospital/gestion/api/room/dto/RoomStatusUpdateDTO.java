package com.hospital.gestion.api.room.dto;

import com.hospital.gestion.api.common.enums.RoomStatus;
import jakarta.validation.constraints.NotNull;

public record RoomStatusUpdateDTO(
        @NotNull(message = "Room status is required")
        RoomStatus status,

        String notes
) {
}

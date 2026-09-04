package com.hospital.gestion.api.room.dto;

import com.hospital.gestion.api.common.enums.RoomType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RoomRequestDTO(
        @NotBlank(message = "Room number is required")
        String number,

        @NotNull(message = "Floor is required")
        @Min(value = -1, message = "Floor must be at least -1 (basement)")
        @Max(value = 50, message = "Floor must be less than 50")
        Integer floor,

        @NotNull(message = "Room type is required")
        RoomType roomType,



        @NotNull(message = "Capacity is required")
        @Min(value = 1, message = "Capacity must be at least 1")
        @Max(value = 10, message = "Capacity must be less than 10")
        Integer capacity,

        @NotNull
        Long wardId,

        String notes
) {
}

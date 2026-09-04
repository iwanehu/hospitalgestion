package com.hospital.gestion.api.room.dto;

public record RoomStatsResponse(
        Long available,
        Long occupied,
        Long maintenance,
        Long cleaning,
        Long reserved,
        Long availableBeds
) {
}

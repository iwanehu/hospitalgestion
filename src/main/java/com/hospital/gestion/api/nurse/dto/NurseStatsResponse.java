package com.hospital.gestion.api.nurse.dto;

public record NurseStatsResponse(
        long total,
        long morning,
        long afternoon,
        long night,
        long rotating
) {
}

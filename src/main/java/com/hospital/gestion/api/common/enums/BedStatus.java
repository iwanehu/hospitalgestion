package com.hospital.gestion.api.common.enums;

public enum BedStatus {
    AVAILABLE("Available"),
    OCCUPIED("Occupied"),
    CLEANING("Cleaning"),
    MAINTENANCE("Maintenance"),
    RESERVED("Reserved");

    private final String description;

    BedStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

package com.hospital.gestion.api.common.enums;

public enum ShiftType {
    MORNING("06:00 - 14:00"),
    AFTERNOON("14:00 - 22:00"),
    NIGHT("22:00 - 06:00"),
    ROTATING("Rotating shifts");

    private final String description;

    ShiftType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

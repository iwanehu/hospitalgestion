package com.hospital.gestion.api.common.enums;

public enum AdminLevel {
    SUPER_ADMIN("Full system access"),
    SYSTEM_ADMIN("System-wide administration"),
    DEPARTMENT_ADMIN("Department-level administration");

    private final String description;

    AdminLevel(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

package com.hospital.gestion.api.common.enums;

public enum AdminPermission {

    // Dashboard y estadísticas
    VIEW_DASHBOARD,
    VIEW_STATISTICS,
    EXPORT_REPORTS,

    // Gestión de usuarios
    MANAGE_USERS,
    MANAGE_ROLES,
    VIEW_USERS,

    // Gestión de doctores
    MANAGE_DOCTORS,
    VIEW_DOCTORS,

    // Gestión de enfermeras
    MANAGE_NURSES,
    VIEW_NURSES,

    // Gestión de pacientes
    MANAGE_PATIENTS,
    VIEW_PATIENTS,

    // Gestión de departamentos
    MANAGE_DEPARTMENTS,
    VIEW_DEPARTMENTS,

    // Gestión de habitaciones
    MANAGE_ROOMS,
    VIEW_ROOMS,

    // Gestión de citas
    MANAGE_APPOINTMENTS,
    VIEW_APPOINTMENTS,

    // Gestión del sistema
    MANAGE_SYSTEM_CONFIG,
    VIEW_SYSTEM_CONFIG,
    MANAGE_AUDIT_LOGS,
    VIEW_AUDIT_LOGS,

    // Configuración
    MANAGE_SETTINGS,
    VIEW_SETTINGS
}

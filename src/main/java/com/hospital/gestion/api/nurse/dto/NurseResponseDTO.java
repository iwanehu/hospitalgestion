package com.hospital.gestion.api.nurse.dto;

import com.hospital.gestion.api.common.enums.DepartmentType;
import com.hospital.gestion.api.common.enums.NurseSpecialty;
import com.hospital.gestion.api.common.enums.ShiftType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record NurseResponseDTO(
        Long id,

        Long userId,
        String fullName,
        String email,

        Long departmentId,
        DepartmentType departmentType,


        String licenseNumber,
        NurseSpecialty specialty,
        ShiftType shiftType,


        Integer yearsOfExperience,
        LocalDate hireDate,

        String biography,

        String emergencyContactName,
        String emergencyContactPhone,
        String emergencyContactRelationship,

        Integer maxPatientsPerShift,
        Boolean isChargeNurse,
        Integer vacationDaysAvailable,

        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
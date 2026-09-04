package com.hospital.gestion.api.doctor.dto;

import com.hospital.gestion.api.common.enums.DepartmentType;
import com.hospital.gestion.api.common.enums.Specialty;

import java.time.LocalDateTime;

public record DoctorResponseDTO(
        Long id,


        Long userId,
        String fullName,
        String email,


        Long departmentId,
        DepartmentType departmentType,

        Specialty specialty,
        String medicalLicenseNumber,
        Integer yearsOfExperience,
        String biography,

        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}

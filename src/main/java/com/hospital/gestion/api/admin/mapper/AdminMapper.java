package com.hospital.gestion.api.admin.mapper;

import com.hospital.gestion.api.admin.dto.AdminRequestDTO;
import com.hospital.gestion.api.admin.dto.AdminResponseDTO;
import com.hospital.gestion.api.admin.dto.AdminUpdateDTO;
import com.hospital.gestion.api.admin.entity.Admin;
import com.hospital.gestion.api.department.entity.Department;
import com.hospital.gestion.api.user.entity.User;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AdminMapper {

    // ======================
    // REQUEST DTO -> ENTITY
    // ===================

    public Admin toEntity(
            AdminRequestDTO request,
            User user,
            Department department
    ) {
        return Admin.builder()
                .user(user)
                .adminLevel(request.adminLevel())
                .department(department)
                .permissions(
                        copyPermissions(
                                request.permissions()
                        )
                )
                .build();
    }

    // ===============================
    // ENTITY -> RESPONSE DTO
    // ==========================================

    public AdminResponseDTO toResponseDTO(
            Admin admin
    ) {
        if (admin == null) {
            return null;
        }

        User user = admin.getUser();
        Department department = admin.getDepartment();

        return new AdminResponseDTO(
                admin.getId(),

                user.getId(),
                admin.getFullName(),
                user.getEmail(),

                admin.getAdminLevel(),

                department != null
                        ? department.getId()
                        : null,

                department != null
                        ? department.getDepartmentType()
                        : null,

                copyPermissions(admin.getPermissions()),

                admin.getLastLogin(),
                admin.isSuperAdmin(),

                admin.getCreatedAt(),
                admin.getUpdatedAt()
        );
    }

    // ============================================================
    // UPDATE DTO -> EXISTING ENTITY
    // ============================================================

    public void updateEntity(
            Admin admin,
            AdminUpdateDTO request,
            Department department
    ) {
        /*
         * AdminLevel es obligatorio en AdminUpdateDTO.
         */
        admin.setAdminLevel(request.adminLevel());

        /*
         * Puede ser null para SUPER_ADMIN y SYSTEM_ADMIN.
         * El service valida que DEPARTMENT_ADMIN tenga
         * un departamento.
         */
        admin.setDepartment(department);

        /*
         * Si permissions llega null, conserva los permisos
         * actuales. Si llega una lista, los reemplaza.
         */
        if (request.permissions() != null) {
            admin.setPermissions(
                    copyPermissions(
                            request.permissions()
                    )
            );
        }

        /*
         * No actualizamos isSuperAdmin aquí.
         * La entidad lo calcula mediante @PrePersist y @PreUpdate.
         */
    }

    // ============================================================
    // ENTITY LIST -> RESPONSE DTO LIST
    // ============================================================

    public List<AdminResponseDTO> toResponseDTOList(
            List<Admin> admins
    ) {
        if (admins == null || admins.isEmpty()) {
            return List.of();
        }

        return admins.stream()
                .map(this::toResponseDTO)
                .toList();
    }



    private <T> List<T> copyPermissions(
            List<T> permissions
    ) {
        return permissions == null
                ? new ArrayList<>()
                : new ArrayList<>(permissions);
    }
}
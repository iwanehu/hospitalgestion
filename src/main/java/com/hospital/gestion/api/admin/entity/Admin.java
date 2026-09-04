package com.hospital.gestion.api.admin.entity;

import com.hospital.gestion.api.common.enums.AdminLevel;
import com.hospital.gestion.api.common.enums.AdminPermission;
import com.hospital.gestion.api.department.entity.Department;
import com.hospital.gestion.api.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "admin",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_admin_user",
                        columnNames = "user_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Admin {

    // ============================================================
    // ID
    // ============================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ============================================================
    // USER
    // ============================================================

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            unique = true,
            foreignKey = @ForeignKey(
                    name = "fk_admin_user"
            )
    )
    private User user;

    // ============================================================
    // ADMIN LEVEL
    // ============================================================

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(
            name = "admin_level",
            nullable = false,
            length = 30
    )
    private AdminLevel adminLevel =
            AdminLevel.DEPARTMENT_ADMIN;

    // ============================================================
    // DEPARTMENT
    // ============================================================

    /*
     * Puede ser null para SUPER_ADMIN y SYSTEM_ADMIN.
     * El service exige departamento cuando el nivel es
     * DEPARTMENT_ADMIN.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "department_id",
            foreignKey = @ForeignKey(
                    name = "fk_admin_department"
            )
    )
    private Department department;

    // ============================================================
    // PERMISSIONS
    // ============================================================

    @Builder.Default
    @ElementCollection(
            targetClass = AdminPermission.class,
            fetch = FetchType.EAGER
    )
    @CollectionTable(
            name = "admin_permissions",
            joinColumns = @JoinColumn(
                    name = "admin_id",
                    nullable = false,
                    foreignKey = @ForeignKey(
                            name = "fk_admin_permissions_admin"
                    )
            ),
            uniqueConstraints = {
                    @UniqueConstraint(
                            name = "uk_admin_permission",
                            columnNames = {
                                    "admin_id",
                                    "permission"
                            }
                    )
            }
    )
    @Enumerated(EnumType.STRING)
    @Column(
            name = "permission",
            nullable = false,
            length = 50
    )
    private List<AdminPermission> permissions =
            new ArrayList<>();

    // ============================================================
    // LOGIN INFORMATION
    // ============================================================

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    // ============================================================
    // SUPER ADMIN
    // ============================================================

    /*
     * El service mantiene sincronizados:
     *
     * AdminLevel.SUPER_ADMIN -> true
     * cualquier otro nivel   -> false
     */
    @Builder.Default
    @Column(
            name = "is_super_admin",
            nullable = false
    )
    private boolean isSuperAdmin = false;

    // ============================================================
    // METADATA
    // ============================================================

    @CreationTimestamp
    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ============================================================
    // DOMAIN HELPERS
    // ============================================================

    public String getFullName() {
        return user.getFirstName()
                + " "
                + user.getLastName();
    }

    public boolean hasPermission(
            AdminPermission permission
    ) {
        return permission != null
                && permissions != null
                && permissions.contains(permission);
    }

    public void addPermission(
            AdminPermission permission
    ) {
        if (permission == null) {
            throw new IllegalArgumentException(
                    "Permission cannot be null"
            );
        }

        if (permissions == null) {
            permissions = new ArrayList<>();
        }

        if (!permissions.contains(permission)) {
            permissions.add(permission);
        }
    }

    public boolean removePermission(
            AdminPermission permission
    ) {
        return permission != null
                && permissions != null
                && permissions.remove(permission);
    }

    public void registerLogin() {
        this.lastLogin = LocalDateTime.now();
    }

    public boolean belongsToDepartment(
            Long departmentId
    ) {
        return departmentId != null
                && department != null
                && department.getId() != null
                && department.getId().equals(departmentId);
    }

    public boolean hasSystemAccess() {
        return adminLevel == AdminLevel.SUPER_ADMIN
                || adminLevel == AdminLevel.SYSTEM_ADMIN;
    }

    // ============================================================
    // ENTITY CALLBACKS
    // ============================================================

    @PrePersist
    @PreUpdate
    private void synchronizeSuperAdminStatus() {
        this.isSuperAdmin =
                this.adminLevel == AdminLevel.SUPER_ADMIN;

        if (permissions == null) {
            permissions = new ArrayList<>();
        }
    }


    public void touch() {
        this.updatedAt = LocalDateTime.now(
                ZoneId.of("Europe/Madrid")
        );
    }
}
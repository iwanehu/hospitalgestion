package com.hospital.gestion.api.department.entity;

import com.hospital.gestion.api.common.enums.DepartmentType;
import com.hospital.gestion.api.ward.entity.Ward;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "departments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "department_type",
            nullable = false,
            unique = true
    )
    private DepartmentType departmentType;

    @Column(
            nullable = false,
            length = 100
    )
    private String location;

    @Column(
            name = "phone_extension",
            length = 10
    )
    private String phoneExtension;

    @Column(length = 500)
    private String description;

    @Builder.Default
    @Column(
            name = "is_active",
            nullable = false
    )
    private Boolean isActive = true;


    // =========================
    // Wards
    // =========================

    @Builder.Default
    @OneToMany(
            mappedBy = "department",
            fetch = FetchType.LAZY
    )
    private List<Ward> wards = new ArrayList<>();


    // =========================
    // Metadata
    // =========================

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


    // =========================
    // Relaciones
    // =========================

    public void addWard(Ward ward) {
        wards.add(ward);
        ward.setDepartment(this);
    }
}
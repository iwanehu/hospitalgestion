package com.hospital.gestion.api.doctor.entity;

import com.hospital.gestion.api.admission.entity.Admission;
import com.hospital.gestion.api.common.enums.Specialty;
import com.hospital.gestion.api.department.entity.Department;
import com.hospital.gestion.api.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "doctors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            unique = true
    )
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "department_id",
            nullable = false
    )
    private Department department;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "specialty",
            nullable = false
    )
    private Specialty specialty;

    @Column(
            name = "medical_license_number",
            nullable = false,
            unique = true,
            length = 50
    )
    private String medicalLicenseNumber;

    @Column(
            name = "years_of_experience",
            nullable = false
    )
    private Integer yearsOfExperience;

    @Column(columnDefinition = "TEXT")
    private String biography;


    // =========================
    // Admissions
    // =========================

    @Builder.Default
    @OneToMany(
            mappedBy = "attendingDoctor",
            fetch = FetchType.LAZY
    )
    private List<Admission> admissions = new ArrayList<>();


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


    public String getFullName() {
        return user.getFirstName() + " " + user.getLastName();
    }
}
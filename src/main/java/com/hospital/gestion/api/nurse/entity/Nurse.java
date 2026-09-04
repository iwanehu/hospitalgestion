package com.hospital.gestion.api.nurse.entity;

import com.hospital.gestion.api.common.enums.NurseSpecialty;
import com.hospital.gestion.api.common.enums.ShiftType;
import com.hospital.gestion.api.department.entity.Department;
import com.hospital.gestion.api.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "nurses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Nurse {

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

    @Column(
            name = "license_number",
            nullable = false,
            unique = true,
            length = 50
    )
    private String licenseNumber;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(
            name = "specialty",
            nullable = false
    )
    private NurseSpecialty specialty = NurseSpecialty.GENERAL;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "shift_type",
            nullable = false
    )
    private ShiftType shiftType;

    @Builder.Default
    @Column(
            name = "years_of_experience",
            nullable = false
    )
    private Integer yearsOfExperience = 0;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Column(columnDefinition = "TEXT")
    private String biography;

    @Embedded
    private EmergencyContact emergencyContact;

    @Builder.Default
    @Column(
            name = "max_patients_per_shift",
            nullable = false
    )
    private Integer maxPatientsPerShift = 5;

    @Builder.Default
    @Column(
            name = "is_charge_nurse",
            nullable = false
    )
    private Boolean isChargeNurse = false;

    @Builder.Default
    @Column(name = "vacation_days_available")
    private Integer vacationDaysAvailable = 30;


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
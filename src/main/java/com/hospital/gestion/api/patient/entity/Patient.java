package com.hospital.gestion.api.patient.entity;

import com.hospital.gestion.api.admission.entity.Admission;
import com.hospital.gestion.api.common.enums.BloodType;
import com.hospital.gestion.api.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "patients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Patient {

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

    @Enumerated(EnumType.STRING)
    @Column(
            name = "blood_type",
            length = 20
    )
    private BloodType bloodType;

    @Column(
            name = "birth_date",
            nullable = false
    )
    private LocalDate birthDate;

    @Embedded
    private EmergencyContact emergencyContact;

    @Column(columnDefinition = "TEXT")
    private String allergies;

    @Builder.Default
    @Column(
            name = "has_health_insurance",
            nullable = false
    )
    private Boolean hasHealthInsurance = false;

    @Column(
            name = "health_insurance_provider",
            length = 100
    )
    private String healthInsuranceProvider;

    @Column(
            name = "health_insurance_number",
            length = 50
    )
    private String healthInsuranceNumber;

    @Column(columnDefinition = "TEXT")
    private String medicalHistory;


    // =========================
    // Admissions
    // =========================

    @Builder.Default
    @OneToMany(
            mappedBy = "patient",
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


    // =========================
    // Helpers
    // =========================

    public String getFullName() {
        return user.getFirstName() + " " + user.getLastName();
    }
}
package com.hospital.gestion.api.admission.entity;

import com.hospital.gestion.api.bed.entity.Bed;
import com.hospital.gestion.api.common.enums.AdmissionStatus;
import com.hospital.gestion.api.doctor.entity.Doctor;
import com.hospital.gestion.api.patient.entity.Patient;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "admissions")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Admission {

    public static final ZoneId ZONE_MADRID =
            ZoneId.of("Europe/Madrid");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // =========================
    // Patient
    // =========================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "patient_id",
            nullable = false
    )
    private Patient patient;


    // =========================
    // Bed
    // =========================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "bed_id",
            nullable = false
    )
    private Bed bed;


    // =========================
    // Doctor responsable
    // =========================

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "attending_doctor_id",
            nullable = false
    )
    private Doctor attendingDoctor;


    // =========================
    // Estado
    // =========================

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false
    )
    private AdmissionStatus status = AdmissionStatus.ACTIVE;


    // =========================
    // Información del ingreso
    // =========================

    @Setter
    @Column(
            name = "admission_reason",
            nullable = false,
            length = 255
    )
    private String admissionReason;

    @Column(
            name = "admitted_at",
            nullable = false
    )
    private LocalDateTime admittedAt;

    @Column(name = "discharged_at")
    private LocalDateTime dischargedAt;

    @Setter
    @Column(columnDefinition = "TEXT")
    private String notes;


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
    // Métodos de negocio
    // =========================

    public void discharge() {

        if (status != AdmissionStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Only active admissions can be discharged"
            );
        }

        this.status = AdmissionStatus.DISCHARGED;
        this.dischargedAt =
                LocalDateTime.now(ZONE_MADRID);
    }

    public void transfer() {

        if (status != AdmissionStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Only active admissions can be transferred"
            );
        }

        this.status = AdmissionStatus.TRANSFERRED;
        this.dischargedAt =
                LocalDateTime.now(ZONE_MADRID);
    }

    public void cancel() {

        if (status != AdmissionStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Only active admissions can be cancelled"
            );
        }

        this.status = AdmissionStatus.CANCELLED;
        this.dischargedAt =
                LocalDateTime.now(ZONE_MADRID);
    }

    public boolean isActive() {
        return status == AdmissionStatus.ACTIVE;
    }
}
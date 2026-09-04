package com.hospital.gestion.api.bed.entity;

import com.hospital.gestion.api.admission.entity.Admission;
import com.hospital.gestion.api.common.enums.BedStatus;
import com.hospital.gestion.api.common.validation.ValidBedNumber;
import com.hospital.gestion.api.room.entity.Room;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "beds",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_bed_number_room",
                        columnNames = {"bed_number", "room_id"}
                )
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bed {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @ValidBedNumber
    @Column(
            name = "bed_number",
            nullable = false,
            length = 20
    )
    private String bedNumber;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "room_id",
            nullable = false
    )
    private Room room;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false
    )
    private BedStatus status = BedStatus.AVAILABLE;

    @Setter
    @Column(columnDefinition = "TEXT")
    private String notes;


    // =========================
    // Admissions
    // =========================

    @Builder.Default
    @OneToMany(
            mappedBy = "bed",
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
    // Métodos de negocio
    // =========================

    public void reserve() {
        if (status != BedStatus.AVAILABLE) {
            throw new IllegalStateException(
                    "Only available beds can be reserved"
            );
        }

        this.status = BedStatus.RESERVED;
    }

    public void occupy() {
        if (status != BedStatus.AVAILABLE
                && status != BedStatus.RESERVED) {

            throw new IllegalStateException(
                    "Bed is not available to be occupied"
            );
        }

        this.status = BedStatus.OCCUPIED;
    }

    public void free() {
        if (status != BedStatus.OCCUPIED) {
            throw new IllegalStateException(
                    "Bed is not occupied"
            );
        }

        this.status = BedStatus.CLEANING;
    }

    public void finishCleaning() {
        if (status != BedStatus.CLEANING) {
            throw new IllegalStateException(
                    "Bed is not being cleaned"
            );
        }

        this.status = BedStatus.AVAILABLE;
    }

    public void maintenance() {
        if (status == BedStatus.OCCUPIED) {
            throw new IllegalStateException(
                    "Occupied bed cannot enter maintenance"
            );
        }

        this.status = BedStatus.MAINTENANCE;
    }

    public void finishMaintenance() {
        if (status != BedStatus.MAINTENANCE) {
            throw new IllegalStateException(
                    "Bed is not in maintenance"
            );
        }

        this.status = BedStatus.AVAILABLE;
    }

    public void cancelReservation() {
        if (status != BedStatus.RESERVED) {
            throw new IllegalStateException(
                    "Bed is not reserved"
            );
        }

        this.status = BedStatus.AVAILABLE;
    }


    // =========================
    // Consultas
    // =========================

    public boolean isAvailable() {
        return status == BedStatus.AVAILABLE;
    }

    public boolean isOccupied() {
        return status == BedStatus.OCCUPIED;
    }

    public boolean isReserved() {
        return status == BedStatus.RESERVED;
    }

    public boolean isCleaning() {
        return status == BedStatus.CLEANING;
    }

    public boolean isMaintenance() {
        return status == BedStatus.MAINTENANCE;
    }
}
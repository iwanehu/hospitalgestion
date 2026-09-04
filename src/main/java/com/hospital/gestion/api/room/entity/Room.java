package com.hospital.gestion.api.room.entity;

import com.hospital.gestion.api.bed.entity.Bed;
import com.hospital.gestion.api.common.enums.RoomStatus;
import com.hospital.gestion.api.common.enums.RoomType;
import com.hospital.gestion.api.ward.entity.Ward;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            nullable = false,
            unique = true,
            length = 20
    )
    private String number;

    @Column(nullable = false)
    private Integer floor;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "room_type",
            nullable = false
    )
    private RoomType roomType;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(
            name = "room_status",
            nullable = false
    )
    private RoomStatus status = RoomStatus.AVAILABLE;

    /**
     * Número máximo de camas permitidas.
     */
    @Column(nullable = false)
    private Integer capacity;


    // =========================
    // Ward
    // =========================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "ward_id",
            nullable = false
    )
    private Ward ward;


    // =========================
    // Beds
    // =========================

    @Builder.Default
    @OneToMany(
            mappedBy = "room",
            fetch = FetchType.LAZY
    )
    private List<Bed> beds = new ArrayList<>();


    // =========================
    // Otros
    // =========================

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
    // Relaciones
    // =========================

    public void addBed(Bed bed) {

        if (capacity == null || capacity <= 0) {
            throw new IllegalStateException(
                    "Room capacity must be greater than zero"
            );
        }

        if (beds.size() >= capacity) {
            throw new IllegalStateException(
                    "Room has reached its maximum bed capacity"
            );
        }

        beds.add(bed);
        bed.setRoom(this);
    }


    // =========================
    // Consultas
    // =========================

    public int getTotalBeds() {
        return beds.size();
    }

    public boolean hasAvailableBedCapacity() {
        return beds.size() < capacity;
    }

    public boolean isAtBedCapacity() {
        return beds.size() >= capacity;
    }
}
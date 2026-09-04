package com.hospital.gestion.api.ward.entity;

import com.hospital.gestion.api.department.entity.Department;
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
        name = "wards",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_ward_name_department",
                        columnNames = {
                                "name",
                                "department_id"
                        }
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ward {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            nullable = false,
            length = 100
    )
    private String name;

    @Column(length = 500)
    private String description;

    @Builder.Default
    @Column(
            name = "is_active",
            nullable = false
    )
    private Boolean isActive = true;


    // =========================
    // Department
    // =========================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "department_id",
            nullable = false
    )
    private Department department;


    // =========================
    // Rooms
    // =========================

    @Builder.Default
    @OneToMany(
            mappedBy = "ward",
            fetch = FetchType.LAZY
    )
    private List<Room> rooms = new ArrayList<>();


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

    public void addRoom(Room room) {
        rooms.add(room);
        room.setWard(this);
    }
}
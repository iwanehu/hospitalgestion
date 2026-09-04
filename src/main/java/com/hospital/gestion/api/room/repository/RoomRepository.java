package com.hospital.gestion.api.room.repository;

import com.hospital.gestion.api.common.enums.BedStatus;
import com.hospital.gestion.api.common.enums.RoomStatus;
import com.hospital.gestion.api.common.enums.RoomType;
import com.hospital.gestion.api.room.entity.Room;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoomRepository
        extends JpaRepository<Room, Long>,
        JpaSpecificationExecutor<Room> {

    // ========================================
    // NUMBER
    // ========================================

    Optional<Room> findByNumberIgnoreCase(
            String number
    );

    boolean existsByNumberIgnoreCase(
            String number
    );

    // ========================================
    // BY WARD
    // ========================================

    List<Room> findByWard_Id(
            Long wardId
    );

    Page<Room> findByWard_Id(
            Long wardId,
            Pageable pageable
    );

    // ========================================
    // BY STATUS
    // ========================================

    List<Room> findByStatus(
            RoomStatus status
    );

    Page<Room> findByStatus(
            RoomStatus status,
            Pageable pageable
    );

    // ========================================
    // BY TYPE
    // ========================================

    List<Room> findByRoomType(
            RoomType roomType
    );

    Page<Room> findByRoomType(
            RoomType roomType,
            Pageable pageable
    );

    // ========================================
    // BY FLOOR
    // ========================================

    List<Room> findByFloor(
            Integer floor
    );

    Page<Room> findByFloor(
            Integer floor,
            Pageable pageable
    );

    // ========================================
    // BY WARD AND STATUS
    // ========================================

    List<Room> findByWard_IdAndStatus(
            Long wardId,
            RoomStatus status
    );

    Page<Room> findByWard_IdAndStatus(
            Long wardId,
            RoomStatus status,
            Pageable pageable
    );

    // ========================================
    // BY DEPARTMENT
    // ========================================

    List<Room> findByWard_Department_Id(
            Long departmentId
    );

    Page<Room> findByWard_Department_Id(
            Long departmentId,
            Pageable pageable
    );

    // ========================================
    // BY DEPARTMENT AND STATUS
    // ========================================

    List<Room> findByWard_Department_IdAndStatus(
            Long departmentId,
            RoomStatus status
    );

    Page<Room> findByWard_Department_IdAndStatus(
            Long departmentId,
            RoomStatus status,
            Pageable pageable
    );

    // ========================================
    // COUNT
    // ========================================

    long countByStatus(
            RoomStatus status
    );

    long countByRoomType(
            RoomType roomType
    );

    long countByFloor(
            Integer floor
    );

    long countByWard_Id(
            Long wardId
    );

    long countByWard_IdAndStatus(
            Long wardId,
            RoomStatus status
    );

    long countByWard_Department_Id(
            Long departmentId
    );

    long countByWard_Department_IdAndStatus(
            Long departmentId,
            RoomStatus status
    );

    // ========================================
    // BED STATISTICS
    // ========================================

    @Query("""
            SELECT COUNT(b)
            FROM Room room
            JOIN room.beds b
            WHERE b.status = :status
            """)
    long countBedsByStatus(
            @Param("status") BedStatus status
    );


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT room
        FROM Room room
        WHERE room.id = :id
        """)
    Optional<Room> findByIdForUpdate(
            @Param("id") Long id
    );
}
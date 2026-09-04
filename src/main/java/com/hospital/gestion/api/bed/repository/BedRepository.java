package com.hospital.gestion.api.bed.repository;

import com.hospital.gestion.api.bed.entity.Bed;
import com.hospital.gestion.api.common.enums.BedStatus;
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

public interface BedRepository
        extends JpaRepository<Bed, Long>,
        JpaSpecificationExecutor<Bed> {

    // ========================================
    // BY ROOM
    // ========================================

    List<Bed> findByRoom_Id(
            Long roomId
    );

    Page<Bed> findByRoom_Id(
            Long roomId,
            Pageable pageable
    );

    // ========================================
    // BY STATUS
    // ========================================

    List<Bed> findByStatus(
            BedStatus status
    );

    Page<Bed> findByStatus(
            BedStatus status,
            Pageable pageable
    );

    // ========================================
    // BY ROOM AND STATUS
    // ========================================

    List<Bed> findByRoom_IdAndStatus(
            Long roomId,
            BedStatus status
    );

    Page<Bed> findByRoom_IdAndStatus(
            Long roomId,
            BedStatus status,
            Pageable pageable
    );

    // ========================================
    // BY WARD
    // ========================================

    List<Bed> findByRoom_Ward_Id(
            Long wardId
    );

    Page<Bed> findByRoom_Ward_Id(
            Long wardId,
            Pageable pageable
    );

    // ========================================
    // BY WARD AND STATUS
    // ========================================

    List<Bed> findByRoom_Ward_IdAndStatus(
            Long wardId,
            BedStatus status
    );

    Page<Bed> findByRoom_Ward_IdAndStatus(
            Long wardId,
            BedStatus status,
            Pageable pageable
    );

    // ========================================
    // BY DEPARTMENT
    // ========================================

    List<Bed> findByRoom_Ward_Department_Id(
            Long departmentId
    );

    Page<Bed> findByRoom_Ward_Department_Id(
            Long departmentId,
            Pageable pageable
    );

    // ========================================
    // BY DEPARTMENT AND STATUS
    // ========================================

    List<Bed> findByRoom_Ward_Department_IdAndStatus(
            Long departmentId,
            BedStatus status
    );

    Page<Bed> findByRoom_Ward_Department_IdAndStatus(
            Long departmentId,
            BedStatus status,
            Pageable pageable
    );

    // ========================================
    // BED NUMBER
    // ========================================

    Optional<Bed> findByBedNumberIgnoreCaseAndRoom_Id(
            String bedNumber,
            Long roomId
    );

    boolean existsByBedNumberIgnoreCaseAndRoom_Id(
            String bedNumber,
            Long roomId
    );

    List<Bed> findByBedNumberContainingIgnoreCase(
            String bedNumber
    );

    Page<Bed> findByBedNumberContainingIgnoreCase(
            String bedNumber,
            Pageable pageable
    );

    // ========================================
    // COUNT
    // ========================================

    long countByStatus(
            BedStatus status
    );

    long countByRoom_Id(
            Long roomId
    );

    long countByRoom_IdAndStatus(
            Long roomId,
            BedStatus status
    );

    long countByRoom_Ward_Id(
            Long wardId
    );

    long countByRoom_Ward_IdAndStatus(
            Long wardId,
            BedStatus status
    );

    long countByRoom_Ward_Department_Id(
            Long departmentId
    );

    long countByRoom_Ward_Department_IdAndStatus(
            Long departmentId,
            BedStatus status
    );

    // ========================================
    // PESSIMISTIC LOCK
    // ========================================

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT bed
            FROM Bed bed
            WHERE bed.id = :id
            """)
    Optional<Bed> findByIdForUpdate(
            @Param("id") Long id
    );
}
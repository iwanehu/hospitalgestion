package com.hospital.gestion.api.admission.repository;

import com.hospital.gestion.api.admission.entity.Admission;
import com.hospital.gestion.api.common.enums.AdmissionStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AdmissionRepository
        extends JpaRepository<Admission, Long>,
        JpaSpecificationExecutor<Admission> {

    // ========================================
    // BY PATIENT
    // ========================================

    List<Admission> findByPatient_Id(
            Long patientId
    );

    Page<Admission> findByPatient_Id(
            Long patientId,
            Pageable pageable
    );

    // ========================================
    // BY PATIENT AND STATUS
    // ========================================

    List<Admission> findByPatient_IdAndStatus(
            Long patientId,
            AdmissionStatus status
    );

    Page<Admission> findByPatient_IdAndStatus(
            Long patientId,
            AdmissionStatus status,
            Pageable pageable
    );

    Optional<Admission> findFirstByPatient_IdAndStatus(
            Long patientId,
            AdmissionStatus status
    );

    // ========================================
    // BY BED
    // ========================================

    List<Admission> findByBed_Id(
            Long bedId
    );

    Page<Admission> findByBed_Id(
            Long bedId,
            Pageable pageable
    );

    // ========================================
    // BY BED AND STATUS
    // ========================================

    List<Admission> findByBed_IdAndStatus(
            Long bedId,
            AdmissionStatus status
    );

    Optional<Admission> findFirstByBed_IdAndStatus(
            Long bedId,
            AdmissionStatus status
    );

    // ========================================
    // BY DOCTOR
    // ========================================

    List<Admission> findByAttendingDoctor_Id(
            Long doctorId
    );

    Page<Admission> findByAttendingDoctor_Id(
            Long doctorId,
            Pageable pageable
    );

    List<Admission> findByAttendingDoctor_IdAndStatus(
            Long doctorId,
            AdmissionStatus status
    );

    Page<Admission> findByAttendingDoctor_IdAndStatus(
            Long doctorId,
            AdmissionStatus status,
            Pageable pageable
    );

    // ========================================
    // BY STATUS
    // ========================================

    List<Admission> findByStatus(
            AdmissionStatus status
    );

    Page<Admission> findByStatus(
            AdmissionStatus status,
            Pageable pageable
    );

    // ========================================
    // BY ROOM
    // ========================================

    List<Admission> findByBed_Room_Id(
            Long roomId
    );

    Page<Admission> findByBed_Room_Id(
            Long roomId,
            Pageable pageable
    );

    List<Admission> findByBed_Room_IdAndStatus(
            Long roomId,
            AdmissionStatus status
    );

    // ========================================
    // BY WARD
    // ========================================

    List<Admission> findByBed_Room_Ward_Id(
            Long wardId
    );

    Page<Admission> findByBed_Room_Ward_Id(
            Long wardId,
            Pageable pageable
    );

    List<Admission> findByBed_Room_Ward_IdAndStatus(
            Long wardId,
            AdmissionStatus status
    );

    // ========================================
    // BY DEPARTMENT
    // ========================================

    List<Admission> findByBed_Room_Ward_Department_Id(
            Long departmentId
    );

    Page<Admission> findByBed_Room_Ward_Department_Id(
            Long departmentId,
            Pageable pageable
    );

    List<Admission>
    findByBed_Room_Ward_Department_IdAndStatus(
            Long departmentId,
            AdmissionStatus status
    );

    // ========================================
    // BY ADMISSION DATE
    // ========================================

    List<Admission> findByAdmittedAtBetween(
            LocalDateTime start,
            LocalDateTime end
    );

    Page<Admission> findByAdmittedAtBetween(
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    );

    // ========================================
    // EXISTS
    // ========================================

    boolean existsByPatient_IdAndStatus(
            Long patientId,
            AdmissionStatus status
    );

    boolean existsByBed_IdAndStatus(
            Long bedId,
            AdmissionStatus status
    );

    // ========================================
    // COUNT
    // ========================================

    long countByStatus(
            AdmissionStatus status
    );

    long countByPatient_Id(
            Long patientId
    );

    long countByAttendingDoctor_Id(
            Long doctorId
    );

    long countByAttendingDoctor_IdAndStatus(
            Long doctorId,
            AdmissionStatus status
    );

    long countByBed_Room_IdAndStatus(
            Long roomId,
            AdmissionStatus status
    );

    long countByBed_Room_Ward_IdAndStatus(
            Long wardId,
            AdmissionStatus status
    );

    long countByBed_Room_Ward_Department_IdAndStatus(
            Long departmentId,
            AdmissionStatus status
    );

    // ========================================
    // PESSIMISTIC LOCK
    // ========================================

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT admission
            FROM Admission admission
            WHERE admission.id = :id
            """)
    Optional<Admission> findByIdForUpdate(
            @Param("id") Long id
    );

    boolean existsByIdAndPatient_User_Id(
            Long admissionId,
            Long userId
    );
}
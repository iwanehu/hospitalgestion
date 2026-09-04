package com.hospital.gestion.api.nurse.repository;

import com.hospital.gestion.api.common.enums.NurseSpecialty;
import com.hospital.gestion.api.common.enums.ShiftType;
import com.hospital.gestion.api.nurse.entity.Nurse;
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

public interface NurseRepository
        extends JpaRepository<Nurse, Long>,
        JpaSpecificationExecutor<Nurse> {

    // ========================================
    // USER
    // ========================================

    Optional<Nurse> findByUser_Id(Long userId);

    boolean existsByUser_Id(Long userId);

    Optional<Nurse> findByUser_EmailIgnoreCase(
            String email
    );

    Optional<Nurse> findByUser_DocumentIdIgnoreCase(
            String documentId
    );

    // ========================================
    // LICENSE
    // ========================================

    Optional<Nurse> findByLicenseNumberIgnoreCase(
            String licenseNumber
    );

    boolean existsByLicenseNumberIgnoreCase(
            String licenseNumber
    );

    // ========================================
    // DEPARTMENT
    // ========================================

    List<Nurse> findByDepartment_Id(
            Long departmentId
    );

    Page<Nurse> findByDepartment_Id(
            Long departmentId,
            Pageable pageable
    );

    // ========================================
    // SPECIALTY
    // ========================================

    List<Nurse> findBySpecialty(
            NurseSpecialty specialty
    );

    Page<Nurse> findBySpecialty(
            NurseSpecialty specialty,
            Pageable pageable
    );

    // ========================================
    // SHIFT
    // ========================================

    List<Nurse> findByShiftType(
            ShiftType shiftType
    );

    Page<Nurse> findByShiftType(
            ShiftType shiftType,
            Pageable pageable
    );

    // ========================================
    // CHARGE NURSE
    // ========================================

    List<Nurse> findByIsChargeNurse(
            Boolean isChargeNurse
    );

    Page<Nurse> findByIsChargeNurse(
            Boolean isChargeNurse,
            Pageable pageable
    );

    // ========================================
    // ACTIVE STATUS
    // ========================================

    List<Nurse> findByUser_IsActive(
            Boolean isActive
    );

    Page<Nurse> findByUser_IsActive(
            Boolean isActive,
            Pageable pageable
    );

    // ========================================
    // DEPARTMENT AND SPECIALTY
    // ========================================

    List<Nurse> findByDepartment_IdAndSpecialty(
            Long departmentId,
            NurseSpecialty specialty
    );

    Page<Nurse> findByDepartment_IdAndSpecialty(
            Long departmentId,
            NurseSpecialty specialty,
            Pageable pageable
    );

    // ========================================
    // DEPARTMENT AND SHIFT
    // ========================================

    List<Nurse> findByDepartment_IdAndShiftType(
            Long departmentId,
            ShiftType shiftType
    );

    Page<Nurse> findByDepartment_IdAndShiftType(
            Long departmentId,
            ShiftType shiftType,
            Pageable pageable
    );

    // ========================================
    // SPECIALTY AND SHIFT
    // ========================================

    List<Nurse> findBySpecialtyAndShiftType(
            NurseSpecialty specialty,
            ShiftType shiftType
    );

    Page<Nurse> findBySpecialtyAndShiftType(
            NurseSpecialty specialty,
            ShiftType shiftType,
            Pageable pageable
    );

    // ========================================
    // SPECIALTY AND ACTIVE STATUS
    // ========================================

    List<Nurse> findBySpecialtyAndUser_IsActive(
            NurseSpecialty specialty,
            Boolean isActive
    );

    Page<Nurse> findBySpecialtyAndUser_IsActive(
            NurseSpecialty specialty,
            Boolean isActive,
            Pageable pageable
    );

    // ========================================
    // DEPARTMENT AND ACTIVE STATUS
    // ========================================

    List<Nurse>
    findByDepartment_IdAndUser_IsActive(
            Long departmentId,
            Boolean isActive
    );

    Page<Nurse>
    findByDepartment_IdAndUser_IsActive(
            Long departmentId,
            Boolean isActive,
            Pageable pageable
    );

    // ========================================
    // SHIFT AND ACTIVE STATUS
    // ========================================

    List<Nurse> findByShiftTypeAndUser_IsActive(
            ShiftType shiftType,
            Boolean isActive
    );

    Page<Nurse> findByShiftTypeAndUser_IsActive(
            ShiftType shiftType,
            Boolean isActive,
            Pageable pageable
    );

    // ========================================
    // SEARCH
    // ========================================

    @Query("""
            SELECT nurse
            FROM Nurse nurse
            JOIN nurse.user user
            WHERE LOWER(user.firstName)
                    LIKE LOWER(CONCAT('%', :text, '%'))
               OR LOWER(user.lastName)
                    LIKE LOWER(CONCAT('%', :text, '%'))
               OR LOWER(CONCAT(
                    user.firstName,
                    ' ',
                    user.lastName
               )) LIKE LOWER(CONCAT('%', :text, '%'))
               OR LOWER(user.email)
                    LIKE LOWER(CONCAT('%', :text, '%'))
               OR LOWER(user.documentId)
                    LIKE LOWER(CONCAT('%', :text, '%'))
               OR LOWER(nurse.licenseNumber)
                    LIKE LOWER(CONCAT('%', :text, '%'))
            """)
    List<Nurse> searchNurses(
            @Param("text") String text
    );

    @Query("""
            SELECT nurse
            FROM Nurse nurse
            JOIN nurse.user user
            WHERE LOWER(user.firstName)
                    LIKE LOWER(CONCAT('%', :text, '%'))
               OR LOWER(user.lastName)
                    LIKE LOWER(CONCAT('%', :text, '%'))
               OR LOWER(CONCAT(
                    user.firstName,
                    ' ',
                    user.lastName
               )) LIKE LOWER(CONCAT('%', :text, '%'))
               OR LOWER(user.email)
                    LIKE LOWER(CONCAT('%', :text, '%'))
               OR LOWER(user.documentId)
                    LIKE LOWER(CONCAT('%', :text, '%'))
               OR LOWER(nurse.licenseNumber)
                    LIKE LOWER(CONCAT('%', :text, '%'))
            """)
    Page<Nurse> searchNurses(
            @Param("text") String text,
            Pageable pageable
    );

    // ========================================
    // ORDERED
    // ========================================

    List<Nurse>
    findAllByOrderByUser_LastNameAscUser_FirstNameAsc();

    List<Nurse>
    findByUser_IsActiveTrueOrderByUser_LastNameAscUser_FirstNameAsc();

    // ========================================
    // COUNT
    // ========================================

    long countByDepartment_Id(Long departmentId);

    long countBySpecialty(NurseSpecialty specialty);

    long countByShiftType(ShiftType shiftType);

    long countByUser_IsActive(Boolean isActive);

    long countByIsChargeNurse(Boolean isChargeNurse);

    long countByDepartment_IdAndShiftType(
            Long departmentId,
            ShiftType shiftType
    );

    long countBySpecialtyAndUser_IsActive(
            NurseSpecialty specialty,
            Boolean isActive
    );

    // ========================================
    // PESSIMISTIC LOCK
    // ========================================

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT nurse
            FROM Nurse nurse
            WHERE nurse.id = :id
            """)
    Optional<Nurse> findByIdForUpdate(
            @Param("id") Long id
    );


    boolean existsByIdAndUser_Id(
            Long nurseId,
            Long userId
    );




}
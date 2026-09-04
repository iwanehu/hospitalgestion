package com.hospital.gestion.api.patient.repository;

import com.hospital.gestion.api.common.enums.BloodType;
import com.hospital.gestion.api.patient.entity.Patient;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PatientRepository
        extends JpaRepository<Patient, Long>,
        JpaSpecificationExecutor<Patient> {

    // ========================================
    // USER
    // ========================================

    Optional<Patient> findByUser_Id(
            Long userId
    );

    boolean existsByUser_Id(
            Long userId
    );

    // ========================================
    // EMAIL
    // ========================================

    Optional<Patient> findByUser_EmailIgnoreCase(
            String email
    );

    boolean existsByUser_EmailIgnoreCase(
            String email
    );

    // ========================================
    // DOCUMENT
    // ========================================

    Optional<Patient> findByUser_DocumentIdIgnoreCase(
            String documentId
    );

    boolean existsByUser_DocumentIdIgnoreCase(
            String documentId
    );

    // ========================================
    // BLOOD TYPE
    // ========================================

    List<Patient> findByBloodType(
            BloodType bloodType
    );

    Page<Patient> findByBloodType(
            BloodType bloodType,
            Pageable pageable
    );

    // ========================================
    // HEALTH INSURANCE
    // ========================================

    List<Patient> findByHasHealthInsurance(
            Boolean hasHealthInsurance
    );

    Page<Patient> findByHasHealthInsurance(
            Boolean hasHealthInsurance,
            Pageable pageable
    );

    List<Patient>
    findByHealthInsuranceProviderContainingIgnoreCase(
            String provider
    );

    Page<Patient>
    findByHealthInsuranceProviderContainingIgnoreCase(
            String provider,
            Pageable pageable
    );

    // ========================================
    // ACTIVE USER
    // ========================================

    List<Patient> findByUser_IsActive(
            Boolean isActive
    );

    Page<Patient> findByUser_IsActive(
            Boolean isActive,
            Pageable pageable
    );

    List<Patient> findByBloodTypeAndUser_IsActive(
            BloodType bloodType,
            Boolean isActive
    );

    Page<Patient> findByBloodTypeAndUser_IsActive(
            BloodType bloodType,
            Boolean isActive,
            Pageable pageable
    );

    // ========================================
    // BIRTH DATE
    // ========================================

    List<Patient> findByBirthDateBetween(
            LocalDate start,
            LocalDate end
    );

    Page<Patient> findByBirthDateBetween(
            LocalDate start,
            LocalDate end,
            Pageable pageable
    );

    // ========================================
    // SEARCH
    // ========================================

    @Query("""
            SELECT patient
            FROM Patient patient
            JOIN patient.user user
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
               OR LOWER(COALESCE(user.phone, ''))
                    LIKE LOWER(CONCAT('%', :text, '%'))
            """)
    List<Patient> searchPatients(
            @Param("text") String text
    );

    @Query("""
            SELECT patient
            FROM Patient patient
            JOIN patient.user user
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
               OR LOWER(COALESCE(user.phone, ''))
                    LIKE LOWER(CONCAT('%', :text, '%'))
            """)
    Page<Patient> searchPatients(
            @Param("text") String text,
            Pageable pageable
    );

    // ========================================
    // ORDERED
    // ========================================

    List<Patient>
    findAllByOrderByUser_LastNameAscUser_FirstNameAsc();

    List<Patient>
    findByUser_IsActiveTrueOrderByUser_LastNameAscUser_FirstNameAsc();

    // ========================================
    // COUNT
    // ========================================

    long countByBloodType(
            BloodType bloodType
    );

    long countByHasHealthInsurance(
            Boolean hasHealthInsurance
    );

    long countByUser_IsActive(
            Boolean isActive
    );

    long countByBloodTypeAndUser_IsActive(
            BloodType bloodType,
            Boolean isActive
    );

    // ========================================
    // PESSIMISTIC LOCK
    // ========================================

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT patient
            FROM Patient patient
            WHERE patient.id = :id
            """)
    Optional<Patient> findByIdForUpdate(
            @Param("id") Long id
    );


    boolean existsByIdAndUser_Id(
            Long patientId,
            Long userId
    );
}
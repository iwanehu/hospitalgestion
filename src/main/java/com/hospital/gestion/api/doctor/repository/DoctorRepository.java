package com.hospital.gestion.api.doctor.repository;

import com.hospital.gestion.api.common.enums.Specialty;
import com.hospital.gestion.api.doctor.entity.Doctor;
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

public interface DoctorRepository
        extends JpaRepository<Doctor, Long>,
        JpaSpecificationExecutor<Doctor> {

    // ========================================
    // USER
    // ========================================

    Optional<Doctor> findByUser_Id(Long userId);

    boolean existsByUser_Id(Long userId);

    Optional<Doctor> findByUser_EmailIgnoreCase(
            String email
    );

    Optional<Doctor> findByUser_DocumentIdIgnoreCase(
            String documentId
    );

    // ========================================
    // MEDICAL LICENSE
    // ========================================

    Optional<Doctor>
    findByMedicalLicenseNumberIgnoreCase(
            String medicalLicenseNumber
    );

    boolean existsByMedicalLicenseNumberIgnoreCase(
            String medicalLicenseNumber
    );

    // ========================================
    // SPECIALTY
    // ========================================

    List<Doctor> findBySpecialty(
            Specialty specialty
    );

    Page<Doctor> findBySpecialty(
            Specialty specialty,
            Pageable pageable
    );

    // ========================================
    // DEPARTMENT
    // ========================================

    List<Doctor> findByDepartment_Id(
            Long departmentId
    );

    Page<Doctor> findByDepartment_Id(
            Long departmentId,
            Pageable pageable
    );

    // ========================================
    // DEPARTMENT AND SPECIALTY
    // ========================================

    List<Doctor> findByDepartment_IdAndSpecialty(
            Long departmentId,
            Specialty specialty
    );

    Page<Doctor> findByDepartment_IdAndSpecialty(
            Long departmentId,
            Specialty specialty,
            Pageable pageable
    );

    // ========================================
    // ACTIVE STATUS
    // ========================================

    List<Doctor> findByUser_IsActive(
            Boolean isActive
    );

    Page<Doctor> findByUser_IsActive(
            Boolean isActive,
            Pageable pageable
    );

    List<Doctor> findBySpecialtyAndUser_IsActive(
            Specialty specialty,
            Boolean isActive
    );

    Page<Doctor> findBySpecialtyAndUser_IsActive(
            Specialty specialty,
            Boolean isActive,
            Pageable pageable
    );

    List<Doctor>
    findByDepartment_IdAndUser_IsActive(
            Long departmentId,
            Boolean isActive
    );

    Page<Doctor>
    findByDepartment_IdAndUser_IsActive(
            Long departmentId,
            Boolean isActive,
            Pageable pageable
    );

    List<Doctor>
    findByDepartment_IdAndSpecialtyAndUser_IsActive(
            Long departmentId,
            Specialty specialty,
            Boolean isActive
    );

    Page<Doctor>
    findByDepartment_IdAndSpecialtyAndUser_IsActive(
            Long departmentId,
            Specialty specialty,
            Boolean isActive,
            Pageable pageable
    );

    // ========================================
    // SEARCH
    // ========================================

    @Query("""
            SELECT doctor
            FROM Doctor doctor
            JOIN doctor.user user
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
               OR LOWER(doctor.medicalLicenseNumber)
                    LIKE LOWER(CONCAT('%', :text, '%'))
            """)
    List<Doctor> searchDoctors(
            @Param("text") String text
    );

    @Query("""
            SELECT doctor
            FROM Doctor doctor
            JOIN doctor.user user
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
               OR LOWER(doctor.medicalLicenseNumber)
                    LIKE LOWER(CONCAT('%', :text, '%'))
            """)
    Page<Doctor> searchDoctors(
            @Param("text") String text,
            Pageable pageable
    );

    // ========================================
    // ORDERED
    // ========================================

    List<Doctor>
    findAllByOrderByUser_LastNameAscUser_FirstNameAsc();

    List<Doctor>
    findByUser_IsActiveTrueOrderByUser_LastNameAscUser_FirstNameAsc();

    // ========================================
    // COUNT
    // ========================================

    long countBySpecialty(
            Specialty specialty
    );

    long countByDepartment_Id(
            Long departmentId
    );

    long countByUser_IsActive(
            Boolean isActive
    );

    long countBySpecialtyAndUser_IsActive(
            Specialty specialty,
            Boolean isActive
    );

    long countByDepartment_IdAndUser_IsActive(
            Long departmentId,
            Boolean isActive
    );

    long countByDepartment_IdAndSpecialty(
            Long departmentId,
            Specialty specialty
    );

    // ========================================
    // PESSIMISTIC LOCK
    // ========================================

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT doctor
            FROM Doctor doctor
            WHERE doctor.id = :id
            """)
    Optional<Doctor> findByIdForUpdate(
            @Param("id") Long id
    );


    boolean existsByIdAndUser_Id(
            Long doctorId,
            Long userId
    );
}
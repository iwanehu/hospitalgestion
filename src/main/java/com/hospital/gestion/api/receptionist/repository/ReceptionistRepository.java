package com.hospital.gestion.api.receptionist.repository;

import com.hospital.gestion.api.common.enums.ShiftType;
import com.hospital.gestion.api.receptionist.entity.Receptionist;
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

public interface ReceptionistRepository
        extends JpaRepository<Receptionist, Long>,
        JpaSpecificationExecutor<Receptionist> {

    // ============================================================
    // USER
    // ============================================================

    boolean existsByUser_Id(Long userId);

    Optional<Receptionist> findByUser_Id(Long userId);

    Optional<Receptionist> findByUser_EmailIgnoreCase(
            String email
    );

    Optional<Receptionist> findByUser_DocumentIdIgnoreCase(
            String documentId
    );

    // ============================================================
    // DEPARTMENT
    // ============================================================

    List<Receptionist> findByDepartment_Id(
            Long departmentId
    );

    Page<Receptionist> findByDepartment_Id(
            Long departmentId,
            Pageable pageable
    );

    // ============================================================
    // SHIFT
    // ============================================================

    List<Receptionist> findByShiftType(
            ShiftType shiftType
    );

    Page<Receptionist> findByShiftType(
            ShiftType shiftType,
            Pageable pageable
    );

    // ============================================================
    // DEPARTMENT AND SHIFT
    // ============================================================

    List<Receptionist> findByDepartment_IdAndShiftType(
            Long departmentId,
            ShiftType shiftType
    );

    Page<Receptionist> findByDepartment_IdAndShiftType(
            Long departmentId,
            ShiftType shiftType,
            Pageable pageable
    );

    // ============================================================
    // ACTIVE STATUS
    // ============================================================

    List<Receptionist> findByUser_IsActive(
            Boolean isActive
    );

    Page<Receptionist> findByUser_IsActive(
            Boolean isActive,
            Pageable pageable
    );

    // ============================================================
    // DESK
    // ============================================================

    List<Receptionist> findByDeskNumberIgnoreCase(
            String deskNumber
    );

    List<Receptionist>
    findByDepartment_IdAndDeskNumberIgnoreCase(
            Long departmentId,
            String deskNumber
    );

    // ============================================================
    // SEARCH
    // ============================================================

    @Query("""
            SELECT r
            FROM Receptionist r
            JOIN r.user u
            JOIN r.department d
            WHERE LOWER(u.firstName) LIKE LOWER(
                    CONCAT('%', :text, '%')
            )
               OR LOWER(u.lastName) LIKE LOWER(
                    CONCAT('%', :text, '%')
            )
               OR LOWER(u.email) LIKE LOWER(
                    CONCAT('%', :text, '%')
            )
               OR LOWER(u.documentId) LIKE LOWER(
                    CONCAT('%', :text, '%')
            )
               OR LOWER(r.deskNumber) LIKE LOWER(
                    CONCAT('%', :text, '%')
            )
            """)
    List<Receptionist> searchReceptionists(
            @Param("text") String text
    );

    @Query(
            value = """
                    SELECT r
                    FROM Receptionist r
                    JOIN r.user u
                    JOIN r.department d
                    WHERE LOWER(u.firstName) LIKE LOWER(
                            CONCAT('%', :text, '%')
                    )
                       OR LOWER(u.lastName) LIKE LOWER(
                            CONCAT('%', :text, '%')
                    )
                       OR LOWER(u.email) LIKE LOWER(
                            CONCAT('%', :text, '%')
                    )
                       OR LOWER(u.documentId) LIKE LOWER(
                            CONCAT('%', :text, '%')
                    )
                       OR LOWER(r.deskNumber) LIKE LOWER(
                            CONCAT('%', :text, '%')
                    )
                    """,
            countQuery = """
                    SELECT COUNT(r)
                    FROM Receptionist r
                    JOIN r.user u
                    JOIN r.department d
                    WHERE LOWER(u.firstName) LIKE LOWER(
                            CONCAT('%', :text, '%')
                    )
                       OR LOWER(u.lastName) LIKE LOWER(
                            CONCAT('%', :text, '%')
                    )
                       OR LOWER(u.email) LIKE LOWER(
                            CONCAT('%', :text, '%')
                    )
                       OR LOWER(u.documentId) LIKE LOWER(
                            CONCAT('%', :text, '%')
                    )
                       OR LOWER(r.deskNumber) LIKE LOWER(
                            CONCAT('%', :text, '%')
                    )
                    """
    )
    Page<Receptionist> searchReceptionists(
            @Param("text") String text,
            Pageable pageable
    );

    // ============================================================
    // ORDER
    // ============================================================

    List<Receptionist>
    findAllByOrderByUser_LastNameAscUser_FirstNameAsc();

    List<Receptionist>
    findByUser_IsActiveTrueOrderByUser_LastNameAscUser_FirstNameAsc();

    // ============================================================
    // COUNT
    // ============================================================

    long countByDepartment_Id(Long departmentId);

    long countByShiftType(ShiftType shiftType);

    long countByDepartment_IdAndShiftType(
            Long departmentId,
            ShiftType shiftType
    );

    long countByUser_IsActive(Boolean isActive);

    boolean existsByIdAndUser_Id(
            Long receptionistId,
            Long userId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT receptionist
        FROM Receptionist receptionist
        WHERE receptionist.id = :id
        """)
    Optional<Receptionist> findByIdForUpdate(
            @Param("id") Long id
    );
}
package com.hospital.gestion.api.admin.repository;

import com.hospital.gestion.api.admin.entity.Admin;
import com.hospital.gestion.api.common.enums.AdminLevel;
import com.hospital.gestion.api.common.enums.AdminPermission;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AdminRepository
        extends JpaRepository<Admin, Long>,
        JpaSpecificationExecutor<Admin> {

    // ============================================================
    // USER
    // ============================================================

    boolean existsByUser_Id(Long userId);

    @EntityGraph(attributePaths = {
            "user",
            "department"
    })
    Optional<Admin> findByUser_Id(Long userId);

    @EntityGraph(attributePaths = {
            "user",
            "department"
    })
    Optional<Admin> findByUser_EmailIgnoreCase(
            String email
    );

    @EntityGraph(attributePaths = {
            "user",
            "department"
    })
    Optional<Admin> findByUser_DocumentIdIgnoreCase(
            String documentId
    );

    // ============================================================
    // ADMIN LEVEL
    // ============================================================

    @EntityGraph(attributePaths = {
            "user",
            "department"
    })
    List<Admin> findByAdminLevel(
            AdminLevel adminLevel
    );

    @EntityGraph(attributePaths = {
            "user",
            "department"
    })
    Page<Admin> findByAdminLevel(
            AdminLevel adminLevel,
            Pageable pageable
    );

    // ============================================================
    // DEPARTMENT
    // ============================================================

    @EntityGraph(attributePaths = {
            "user",
            "department"
    })
    List<Admin> findByDepartment_Id(
            Long departmentId
    );

    @EntityGraph(attributePaths = {
            "user",
            "department"
    })
    Page<Admin> findByDepartment_Id(
            Long departmentId,
            Pageable pageable
    );

    // ============================================================
    // DEPARTMENT AND LEVEL
    // ============================================================

    @EntityGraph(attributePaths = {
            "user",
            "department"
    })
    List<Admin> findByDepartment_IdAndAdminLevel(
            Long departmentId,
            AdminLevel adminLevel
    );

    @EntityGraph(attributePaths = {
            "user",
            "department"
    })
    Page<Admin> findByDepartment_IdAndAdminLevel(
            Long departmentId,
            AdminLevel adminLevel,
            Pageable pageable
    );

    // ============================================================
    // SUPER ADMIN
    // ============================================================

    @EntityGraph(attributePaths = {
            "user",
            "department"
    })
    List<Admin> findByIsSuperAdmin(
            boolean isSuperAdmin
    );

    @EntityGraph(attributePaths = {
            "user",
            "department"
    })
    Page<Admin> findByIsSuperAdmin(
            boolean isSuperAdmin,
            Pageable pageable
    );

    // ============================================================
    // ACTIVE STATUS
    // ============================================================

    @EntityGraph(attributePaths = {
            "user",
            "department"
    })
    List<Admin> findByUser_IsActive(
            Boolean isActive
    );

    @EntityGraph(attributePaths = {
            "user",
            "department"
    })
    Page<Admin> findByUser_IsActive(
            Boolean isActive,
            Pageable pageable
    );

    // ============================================================
    // PERMISSIONS
    // ============================================================

    @EntityGraph(attributePaths = {
            "user",
            "department"
    })
    @Query("""
            SELECT DISTINCT a
            FROM Admin a
            JOIN a.permissions permission
            WHERE permission = :permission
            """)
    List<Admin> findByPermission(
            @Param("permission")
            AdminPermission permission
    );

    @EntityGraph(attributePaths = {
            "user",
            "department"
    })
    @Query(
            value = """
                    SELECT DISTINCT a
                    FROM Admin a
                    JOIN a.permissions permission
                    WHERE permission = :permission
                    """,
            countQuery = """
                    SELECT COUNT(DISTINCT a)
                    FROM Admin a
                    JOIN a.permissions permission
                    WHERE permission = :permission
                    """
    )
    Page<Admin> findByPermission(
            @Param("permission")
            AdminPermission permission,
            Pageable pageable
    );

    @Query("""
            SELECT CASE
                       WHEN COUNT(a) > 0 THEN true
                       ELSE false
                   END
            FROM Admin a
            JOIN a.permissions permission
            WHERE a.id = :adminId
              AND permission = :permission
            """)
    boolean hasPermission(
            @Param("adminId")
            Long adminId,

            @Param("permission")
            AdminPermission permission
    );

    // ============================================================
    // SEARCH
    // ============================================================

    @EntityGraph(attributePaths = {
            "user",
            "department"
    })
    @Query("""
            SELECT a
            FROM Admin a
            JOIN a.user u
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
            """)
    List<Admin> searchAdmins(
            @Param("text")
            String text
    );

    @EntityGraph(attributePaths = {
            "user",
            "department"
    })
    @Query(
            value = """
                    SELECT a
                    FROM Admin a
                    JOIN a.user u
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
                    """,
            countQuery = """
                    SELECT COUNT(a)
                    FROM Admin a
                    JOIN a.user u
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
                    """
    )
    Page<Admin> searchAdmins(
            @Param("text")
            String text,
            Pageable pageable
    );

    // ============================================================
    // ORDERED
    // ============================================================

    @EntityGraph(attributePaths = {
            "user",
            "department"
    })
    List<Admin>
    findAllByOrderByUser_LastNameAscUser_FirstNameAsc();

    @EntityGraph(attributePaths = {
            "user",
            "department"
    })
    List<Admin>
    findByUser_IsActiveTrueOrderByUser_LastNameAscUser_FirstNameAsc();

    // ============================================================
    // COUNT
    // ============================================================

    long countByAdminLevel(
            AdminLevel adminLevel
    );

    long countByDepartment_Id(
            Long departmentId
    );

    long countByDepartment_IdAndAdminLevel(
            Long departmentId,
            AdminLevel adminLevel
    );

    long countByIsSuperAdmin(
            boolean isSuperAdmin
    );

    long countByUser_IsActive(
            Boolean isActive
    );


    boolean existsByIdAndUser_Id(
            Long adminId,
            Long userId
    );


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {
            "user",
            "department"
    })
    @Query("""
        SELECT admin
        FROM Admin admin
        WHERE admin.id = :id
        """)
    Optional<Admin> findByIdForUpdate(
            @Param("id") Long id
    );
}
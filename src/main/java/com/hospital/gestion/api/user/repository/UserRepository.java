package com.hospital.gestion.api.user.repository;

import com.hospital.gestion.api.common.enums.Role;
import com.hospital.gestion.api.user.entity.User;
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

public interface UserRepository
        extends JpaRepository<User, Long>,
        JpaSpecificationExecutor<User> {

    // ========================================
    // EMAIL
    // ========================================

    Optional<User> findByEmailIgnoreCase(
            String email
    );

    boolean existsByEmailIgnoreCase(
            String email
    );

    // ========================================
    // DOCUMENT
    // ========================================

    Optional<User> findByDocumentIdIgnoreCase(
            String documentId
    );

    boolean existsByDocumentIdIgnoreCase(
            String documentId
    );

    // ========================================
    // ROLE
    // ========================================

    List<User> findByRole(
            Role role
    );

    Page<User> findByRole(
            Role role,
            Pageable pageable
    );

    // ========================================
    // ACTIVE STATUS
    // ========================================

    List<User> findByIsActive(
            Boolean isActive
    );

    Page<User> findByIsActive(
            Boolean isActive,
            Pageable pageable
    );

    // ========================================
    // ROLE AND STATUS
    // ========================================

    List<User> findByRoleAndIsActive(
            Role role,
            Boolean isActive
    );

    Page<User> findByRoleAndIsActive(
            Role role,
            Boolean isActive,
            Pageable pageable
    );

    // ========================================
    // SEARCH
    // ========================================

    @Query("""
            SELECT user
            FROM User user
            WHERE LOWER(user.firstName)
                    LIKE LOWER(CONCAT('%', :text, '%'))
               OR LOWER(user.lastName)
                    LIKE LOWER(CONCAT('%', :text, '%'))
               OR LOWER(user.email)
                    LIKE LOWER(CONCAT('%', :text, '%'))
               OR LOWER(user.documentId)
                    LIKE LOWER(CONCAT('%', :text, '%'))
               OR LOWER(CONCAT(
                    user.firstName,
                    ' ',
                    user.lastName
               )) LIKE LOWER(CONCAT('%', :text, '%'))
            """)
    List<User> searchUsers(
            @Param("text") String text
    );

    @Query("""
            SELECT user
            FROM User user
            WHERE LOWER(user.firstName)
                    LIKE LOWER(CONCAT('%', :text, '%'))
               OR LOWER(user.lastName)
                    LIKE LOWER(CONCAT('%', :text, '%'))
               OR LOWER(user.email)
                    LIKE LOWER(CONCAT('%', :text, '%'))
               OR LOWER(user.documentId)
                    LIKE LOWER(CONCAT('%', :text, '%'))
               OR LOWER(CONCAT(
                    user.firstName,
                    ' ',
                    user.lastName
               )) LIKE LOWER(CONCAT('%', :text, '%'))
            """)
    Page<User> searchUsers(
            @Param("text") String text,
            Pageable pageable
    );

    // ========================================
    // ORDERED
    // ========================================

    List<User> findAllByOrderByLastNameAscFirstNameAsc();

    List<User>
    findByIsActiveTrueOrderByLastNameAscFirstNameAsc();

    // ========================================
    // COUNT
    // ========================================

    long countByRole(
            Role role
    );

    long countByIsActive(
            Boolean isActive
    );

    long countByRoleAndIsActive(
            Role role,
            Boolean isActive
    );

    // ========================================
    // PESSIMISTIC LOCK
    // ========================================

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT user
            FROM User user
            WHERE user.id = :id
            """)
    Optional<User> findByIdForUpdate(
            @Param("id") Long id
    );
}
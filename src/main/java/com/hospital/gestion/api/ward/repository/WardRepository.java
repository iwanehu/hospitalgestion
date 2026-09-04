package com.hospital.gestion.api.ward.repository;

import com.hospital.gestion.api.ward.entity.Ward;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface WardRepository
        extends JpaRepository<Ward, Long>,
        JpaSpecificationExecutor<Ward> {

    // ========================================
    // UNIQUE VALIDATION
    // ========================================

    boolean existsByNameIgnoreCaseAndDepartment_Id(
            String name,
            Long departmentId
    );

    Optional<Ward> findByNameIgnoreCaseAndDepartment_Id(
            String name,
            Long departmentId
    );

    // ========================================
    // ACTIVE STATUS
    // ========================================

    List<Ward> findByIsActive(
            Boolean isActive
    );

    Page<Ward> findByIsActive(
            Boolean isActive,
            Pageable pageable
    );

    // ========================================
    // BY DEPARTMENT
    // ========================================

    List<Ward> findByDepartment_Id(
            Long departmentId
    );

    Page<Ward> findByDepartment_Id(
            Long departmentId,
            Pageable pageable
    );

    // ========================================
    // BY DEPARTMENT AND ACTIVE STATUS
    // ========================================

    List<Ward> findByDepartment_IdAndIsActive(
            Long departmentId,
            Boolean isActive
    );

    Page<Ward> findByDepartment_IdAndIsActive(
            Long departmentId,
            Boolean isActive,
            Pageable pageable
    );

    // ========================================
    // SEARCH BY NAME
    // ========================================

    List<Ward> findByNameContainingIgnoreCase(
            String name
    );

    Page<Ward> findByNameContainingIgnoreCase(
            String name,
            Pageable pageable
    );

    // ========================================
    // COUNT
    // ========================================

    long countByIsActive(
            Boolean isActive
    );

    long countByDepartment_Id(
            Long departmentId
    );

    long countByDepartment_IdAndIsActive(
            Long departmentId,
            Boolean isActive
    );
}
package com.hospital.gestion.api.department.repository;

import com.hospital.gestion.api.common.enums.DepartmentType;
import com.hospital.gestion.api.department.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.List;
import java.util.Optional;

public interface DepartmentRepository
        extends JpaRepository<Department, Long>, JpaSpecificationExecutor<Department> {

    // ========================================
    // DEPARTMENT TYPE
    // ========================================

    Optional<Department> findByDepartmentType(
            DepartmentType departmentType
    );

    boolean existsByDepartmentType(
            DepartmentType departmentType
    );

    @Query("""
           SELECT d
           FROM Department d
           WHERE d.departmentType = :type
             AND d.isActive = true
           """)
    Optional<Department> findByDepartmentTypeAndActive(
            @Param("type") DepartmentType departmentType
    );

    // ========================================
    // ACTIVE STATUS
    // ========================================

    List<Department> findByIsActive(
            Boolean isActive
    );

    List<Department> findByIsActiveTrueOrderByDepartmentTypeAsc();

    long countByIsActive(
            Boolean isActive
    );

    // ========================================
    // LOCATION
    // ========================================

    Optional<Department> findByLocation(
            String location
    );

    boolean existsByLocation(
            String location
    );

    List<Department> findByLocationContainingIgnoreCase(
            String location
    );

    // ========================================
    // DESCRIPTION
    // ========================================

    List<Department> findByDescriptionContainingIgnoreCase(
            String description
    );

    // ========================================
    // ORDER
    // ========================================

    List<Department> findAllByOrderByDepartmentTypeAsc();

    // ========================================
    // DEPARTMENTS WITH WARDS
    // ========================================

    @Query("""
           SELECT DISTINCT d
           FROM Department d
           LEFT JOIN FETCH d.wards
           WHERE d.id = :id
           """)
    Optional<Department> findByIdWithWards(
            @Param("id") Long id
    );

    @Query("""
           SELECT DISTINCT d
           FROM Department d
           LEFT JOIN FETCH d.wards
           """)
    List<Department> findAllWithWards();



    // ========================================
// PAGINATED FILTER
// ========================================

}
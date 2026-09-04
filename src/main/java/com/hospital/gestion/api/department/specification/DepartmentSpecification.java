package com.hospital.gestion.api.department.specification;

import com.hospital.gestion.api.common.enums.DepartmentType;
import com.hospital.gestion.api.department.entity.Department;
import org.springframework.data.jpa.domain.Specification;

import java.util.Locale;

public final class DepartmentSpecification {

    private DepartmentSpecification() {
    }

    public static Specification<Department>
    hasDepartmentType(
            DepartmentType departmentType
    ) {
        return (root, query, criteriaBuilder) -> {
            if (departmentType == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    root.get("departmentType"),
                    departmentType
            );
        };
    }

    public static Specification<Department>
    hasActiveStatus(
            Boolean isActive
    ) {
        return (root, query, criteriaBuilder) -> {
            if (isActive == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    root.get("isActive"),
                    isActive
            );
        };
    }

    public static Specification<Department>
    locationContains(
            String location
    ) {
        return (root, query, criteriaBuilder) -> {
            if (location == null || location.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            String pattern = "%"
                    + location.trim()
                    .toLowerCase(Locale.ROOT)
                    + "%";

            return criteriaBuilder.like(
                    criteriaBuilder.lower(
                            root.<String>get("location")
                    ),
                    pattern
            );
        };
    }

    public static Specification<Department>
    descriptionContains(
            String description
    ) {
        return (root, query, criteriaBuilder) -> {
            if (description == null
                    || description.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            String pattern = "%"
                    + description.trim()
                    .toLowerCase(Locale.ROOT)
                    + "%";

            return criteriaBuilder.like(
                    criteriaBuilder.lower(
                            root.<String>get("description")
                    ),
                    pattern
            );
        };
    }
}

package com.hospital.gestion.api.ward.specification;

import com.hospital.gestion.api.ward.entity.Ward;
import org.springframework.data.jpa.domain.Specification;

import java.util.Locale;

public final class WardSpecification {

    private WardSpecification() {
    }

    public static Specification<Ward> nameContains(
            String name
    ) {
        return (root, query, criteriaBuilder) -> {
            if (name == null || name.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            String pattern = "%"
                    + name.trim()
                    .toLowerCase(Locale.ROOT)
                    + "%";

            return criteriaBuilder.like(
                    criteriaBuilder.lower(
                            root.<String>get("name")
                    ),
                    pattern
            );
        };
    }

    public static Specification<Ward> descriptionContains(
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

    public static Specification<Ward> hasActiveStatus(
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

    public static Specification<Ward> belongsToDepartment(
            Long departmentId
    ) {
        return (root, query, criteriaBuilder) -> {
            if (departmentId == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    root.get("department").get("id"),
                    departmentId
            );
        };
    }
}

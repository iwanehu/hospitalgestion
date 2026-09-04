package com.hospital.gestion.api.bed.specification;

import com.hospital.gestion.api.bed.entity.Bed;
import com.hospital.gestion.api.common.enums.BedStatus;
import org.springframework.data.jpa.domain.Specification;

import java.util.Locale;

public final class BedSpecification {

    private BedSpecification() {
    }

    public static Specification<Bed> numberContains(
            String bedNumber
    ) {
        return (root, query, criteriaBuilder) -> {
            if (bedNumber == null || bedNumber.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            String pattern = "%"
                    + bedNumber.trim()
                    .toLowerCase(Locale.ROOT)
                    + "%";

            return criteriaBuilder.like(
                    criteriaBuilder.lower(
                            root.<String>get("bedNumber")
                    ),
                    pattern
            );
        };
    }

    public static Specification<Bed> hasStatus(
            BedStatus status
    ) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    root.get("status"),
                    status
            );
        };
    }

    public static Specification<Bed> belongsToRoom(
            Long roomId
    ) {
        return (root, query, criteriaBuilder) -> {
            if (roomId == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    root.get("room").get("id"),
                    roomId
            );
        };
    }

    public static Specification<Bed> belongsToWard(
            Long wardId
    ) {
        return (root, query, criteriaBuilder) -> {
            if (wardId == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    root.get("room")
                            .get("ward")
                            .get("id"),
                    wardId
            );
        };
    }

    public static Specification<Bed> belongsToDepartment(
            Long departmentId
    ) {
        return (root, query, criteriaBuilder) -> {
            if (departmentId == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    root.get("room")
                            .get("ward")
                            .get("department")
                            .get("id"),
                    departmentId
            );
        };
    }
}

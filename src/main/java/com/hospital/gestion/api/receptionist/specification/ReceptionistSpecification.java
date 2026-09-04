package com.hospital.gestion.api.receptionist.specification;

import com.hospital.gestion.api.common.enums.ShiftType;
import com.hospital.gestion.api.receptionist.entity.Receptionist;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.util.Locale;

public final class ReceptionistSpecification {

    private ReceptionistSpecification() {
    }

    public static Specification<Receptionist> textContains(
            String text
    ) {
        return (root, query, criteriaBuilder) -> {
            if (text == null || text.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            var user = root.join("user", JoinType.INNER);

            String pattern = "%"
                    + text.trim()
                    .toLowerCase(Locale.ROOT)
                    + "%";

            Expression<String> fullName =
                    criteriaBuilder.concat(
                            criteriaBuilder.concat(
                                    user.<String>get("firstName"),
                                    " "
                            ),
                            user.<String>get("lastName")
                    );

            return criteriaBuilder.or(
                    criteriaBuilder.like(
                            criteriaBuilder.lower(
                                    user.<String>get("firstName")
                            ),
                            pattern
                    ),
                    criteriaBuilder.like(
                            criteriaBuilder.lower(
                                    user.<String>get("lastName")
                            ),
                            pattern
                    ),
                    criteriaBuilder.like(
                            criteriaBuilder.lower(fullName),
                            pattern
                    ),
                    criteriaBuilder.like(
                            criteriaBuilder.lower(
                                    user.<String>get("email")
                            ),
                            pattern
                    ),
                    criteriaBuilder.like(
                            criteriaBuilder.lower(
                                    user.<String>get("documentId")
                            ),
                            pattern
                    ),
                    criteriaBuilder.like(
                            criteriaBuilder.lower(
                                    criteriaBuilder.coalesce(
                                            root.<String>get(
                                                    "deskNumber"
                                            ),
                                            ""
                                    )
                            ),
                            pattern
                    )
            );
        };
    }

    public static Specification<Receptionist> belongsToDepartment(
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

    public static Specification<Receptionist> hasShiftType(
            ShiftType shiftType
    ) {
        return (root, query, criteriaBuilder) -> {
            if (shiftType == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    root.get("shiftType"),
                    shiftType
            );
        };
    }

    public static Specification<Receptionist> hasActiveStatus(
            Boolean isActive
    ) {
        return (root, query, criteriaBuilder) -> {
            if (isActive == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    root.get("user").get("isActive"),
                    isActive
            );
        };
    }

    public static Specification<Receptionist> deskContains(
            String deskNumber
    ) {
        return (root, query, criteriaBuilder) -> {
            if (deskNumber == null || deskNumber.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            String pattern = "%"
                    + deskNumber.trim()
                    .toLowerCase(Locale.ROOT)
                    + "%";

            return criteriaBuilder.like(
                    criteriaBuilder.lower(
                            root.<String>get("deskNumber")
                    ),
                    pattern
            );
        };
    }
}


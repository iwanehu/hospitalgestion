package com.hospital.gestion.api.nurse.specification;

import com.hospital.gestion.api.common.enums.NurseSpecialty;
import com.hospital.gestion.api.common.enums.ShiftType;
import com.hospital.gestion.api.nurse.entity.Nurse;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.Locale;

public final class NurseSpecification {

    private NurseSpecification() {
    }

    public static Specification<Nurse> textContains(
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
                                    root.<String>get("licenseNumber")
                            ),
                            pattern
                    )
            );
        };
    }

    public static Specification<Nurse> belongsToDepartment(
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

    public static Specification<Nurse> hasSpecialty(
            NurseSpecialty specialty
    ) {
        return (root, query, criteriaBuilder) -> {
            if (specialty == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    root.get("specialty"),
                    specialty
            );
        };
    }

    public static Specification<Nurse> hasShiftType(
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

    public static Specification<Nurse> hasActiveStatus(
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

    public static Specification<Nurse> hasChargeStatus(
            Boolean isChargeNurse
    ) {
        return (root, query, criteriaBuilder) -> {
            if (isChargeNurse == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    root.get("isChargeNurse"),
                    isChargeNurse
            );
        };
    }

    public static Specification<Nurse> minimumExperience(
            Integer minimumExperience
    ) {
        return (root, query, criteriaBuilder) -> {
            if (minimumExperience == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.greaterThanOrEqualTo(
                    root.get("yearsOfExperience"),
                    minimumExperience
            );
        };
    }

    public static Specification<Nurse> maximumExperience(
            Integer maximumExperience
    ) {
        return (root, query, criteriaBuilder) -> {
            if (maximumExperience == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.lessThanOrEqualTo(
                    root.get("yearsOfExperience"),
                    maximumExperience
            );
        };
    }

    public static Specification<Nurse> hiredFrom(
            LocalDate from
    ) {
        return (root, query, criteriaBuilder) -> {
            if (from == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.greaterThanOrEqualTo(
                    root.get("hireDate"),
                    from
            );
        };
    }

    public static Specification<Nurse> hiredTo(
            LocalDate to
    ) {
        return (root, query, criteriaBuilder) -> {
            if (to == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.lessThanOrEqualTo(
                    root.get("hireDate"),
                    to
            );
        };
    }
}

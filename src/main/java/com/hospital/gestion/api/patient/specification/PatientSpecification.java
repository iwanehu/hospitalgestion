package com.hospital.gestion.api.patient.specification;

import com.hospital.gestion.api.common.enums.BloodType;
import com.hospital.gestion.api.patient.entity.Patient;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.Locale;

public final class PatientSpecification {

    private PatientSpecification() {
    }

    public static Specification<Patient> textContains(
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
                                            user.<String>get("phone"),
                                            ""
                                    )
                            ),
                            pattern
                    )
            );
        };
    }

    public static Specification<Patient> hasBloodType(
            BloodType bloodType
    ) {
        return (root, query, criteriaBuilder) -> {
            if (bloodType == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    root.get("bloodType"),
                    bloodType
            );
        };
    }

    public static Specification<Patient> hasInsuranceStatus(
            Boolean hasHealthInsurance
    ) {
        return (root, query, criteriaBuilder) -> {
            if (hasHealthInsurance == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    root.get("hasHealthInsurance"),
                    hasHealthInsurance
            );
        };
    }

    public static Specification<Patient>
    insuranceProviderContains(
            String insuranceProvider
    ) {
        return (root, query, criteriaBuilder) -> {
            if (insuranceProvider == null
                    || insuranceProvider.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            String pattern = "%"
                    + insuranceProvider.trim()
                    .toLowerCase(Locale.ROOT)
                    + "%";

            return criteriaBuilder.like(
                    criteriaBuilder.lower(
                            root.<String>get(
                                    "healthInsuranceProvider"
                            )
                    ),
                    pattern
            );
        };
    }

    public static Specification<Patient> hasActiveStatus(
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

    public static Specification<Patient> birthDateFrom(
            LocalDate from
    ) {
        return (root, query, criteriaBuilder) -> {
            if (from == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.greaterThanOrEqualTo(
                    root.get("birthDate"),
                    from
            );
        };
    }

    public static Specification<Patient> birthDateTo(
            LocalDate to
    ) {
        return (root, query, criteriaBuilder) -> {
            if (to == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.lessThanOrEqualTo(
                    root.get("birthDate"),
                    to
            );
        };
    }
}

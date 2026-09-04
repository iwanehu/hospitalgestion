package com.hospital.gestion.api.doctor.specification;

import com.hospital.gestion.api.common.enums.Specialty;
import com.hospital.gestion.api.doctor.entity.Doctor;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.util.Locale;

public final class DoctorSpecification {

    private DoctorSpecification() {
    }

    public static Specification<Doctor> textContains(
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
                                    root.<String>get(
                                            "medicalLicenseNumber"
                                    )
                            ),
                            pattern
                    )
            );
        };
    }

    public static Specification<Doctor> belongsToDepartment(
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

    public static Specification<Doctor> hasSpecialty(
            Specialty specialty
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

    public static Specification<Doctor> hasActiveStatus(
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

    public static Specification<Doctor> minimumExperience(
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

    public static Specification<Doctor> maximumExperience(
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
}


package com.hospital.gestion.api.user.specification;

import com.hospital.gestion.api.common.enums.Role;
import com.hospital.gestion.api.user.entity.User;
import jakarta.persistence.criteria.Expression;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.Locale;

public final class UserSpecification {

    private UserSpecification() {
    }

    public static Specification<User> textContains(
            String text
    ) {
        return (root, query, criteriaBuilder) -> {
            if (text == null || text.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            String pattern = "%"
                    + text.trim()
                    .toLowerCase(Locale.ROOT)
                    + "%";

            Expression<String> fullName =
                    criteriaBuilder.concat(
                            criteriaBuilder.concat(
                                    root.<String>get("firstName"),
                                    " "
                            ),
                            root.<String>get("lastName")
                    );

            return criteriaBuilder.or(
                    criteriaBuilder.like(
                            criteriaBuilder.lower(
                                    root.<String>get("firstName")
                            ),
                            pattern
                    ),
                    criteriaBuilder.like(
                            criteriaBuilder.lower(
                                    root.<String>get("lastName")
                            ),
                            pattern
                    ),
                    criteriaBuilder.like(
                            criteriaBuilder.lower(fullName),
                            pattern
                    ),
                    criteriaBuilder.like(
                            criteriaBuilder.lower(
                                    root.<String>get("email")
                            ),
                            pattern
                    ),
                    criteriaBuilder.like(
                            criteriaBuilder.lower(
                                    root.<String>get("documentId")
                            ),
                            pattern
                    ),
                    criteriaBuilder.like(
                            criteriaBuilder.lower(
                                    criteriaBuilder.coalesce(
                                            root.<String>get("phone"),
                                            ""
                                    )
                            ),
                            pattern
                    )
            );
        };
    }

    public static Specification<User> hasRole(
            Role role
    ) {
        return (root, query, criteriaBuilder) -> {
            if (role == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    root.get("role"),
                    role
            );
        };
    }

    public static Specification<User> hasActiveStatus(
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

    public static Specification<User> createdFrom(
            LocalDateTime from
    ) {
        return (root, query, criteriaBuilder) -> {
            if (from == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.greaterThanOrEqualTo(
                    root.get("createdAt"),
                    from
            );
        };
    }

    public static Specification<User> createdTo(
            LocalDateTime to
    ) {
        return (root, query, criteriaBuilder) -> {
            if (to == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.lessThanOrEqualTo(
                    root.get("createdAt"),
                    to
            );
        };
    }
}

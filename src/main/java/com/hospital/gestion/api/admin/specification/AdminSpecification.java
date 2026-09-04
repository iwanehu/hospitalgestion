package com.hospital.gestion.api.admin.specification;

import com.hospital.gestion.api.admin.entity.Admin;
import com.hospital.gestion.api.common.enums.AdminLevel;
import com.hospital.gestion.api.common.enums.AdminPermission;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class AdminSpecification {

    private AdminSpecification() {
    }

    public static Specification<Admin> withFilters(
            String text,
            AdminLevel adminLevel,
            Long departmentId,
            AdminPermission permission,
            Boolean isActive,
            Boolean isSuperAdmin,
            LocalDateTime lastLoginFrom,
            LocalDateTime lastLoginTo
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates =
                    new ArrayList<>();

            if (text != null && !text.isBlank()) {
                String pattern =
                        "%"
                                + text.trim()
                                .toLowerCase(Locale.ROOT)
                                + "%";

                predicates.add(
                        criteriaBuilder.or(
                                criteriaBuilder.like(
                                        criteriaBuilder.lower(
                                                root.get("user")
                                                        .get("firstName")
                                        ),
                                        pattern
                                ),
                                criteriaBuilder.like(
                                        criteriaBuilder.lower(
                                                root.get("user")
                                                        .get("lastName")
                                        ),
                                        pattern
                                ),
                                criteriaBuilder.like(
                                        criteriaBuilder.lower(
                                                root.get("user")
                                                        .get("email")
                                        ),
                                        pattern
                                ),
                                criteriaBuilder.like(
                                        criteriaBuilder.lower(
                                                root.get("user")
                                                        .get("documentId")
                                        ),
                                        pattern
                                )
                        )
                );
            }

            if (adminLevel != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("adminLevel"),
                                adminLevel
                        )
                );
            }

            if (departmentId != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("department").get("id"),
                                departmentId
                        )
                );
            }

            if (permission != null) {
                var permissionSubquery =
                        query.subquery(Integer.class);

                var adminSubqueryRoot =
                        permissionSubquery.from(Admin.class);

                var permissionsJoin =
                        adminSubqueryRoot.join(
                                "permissions",
                                JoinType.INNER
                        );

                permissionSubquery
                        .select(criteriaBuilder.literal(1))
                        .where(
                                criteriaBuilder.equal(
                                        adminSubqueryRoot.get("id"),
                                        root.get("id")
                                ),
                                criteriaBuilder.equal(
                                        permissionsJoin,
                                        permission
                                )
                        );

                predicates.add(
                        criteriaBuilder.exists(
                                permissionSubquery
                        )
                );
            }

            if (isActive != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("user").get("isActive"),
                                isActive
                        )
                );
            }

            if (isSuperAdmin != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("isSuperAdmin"),
                                isSuperAdmin
                        )
                );
            }

            if (lastLoginFrom != null) {
                predicates.add(
                        criteriaBuilder.greaterThanOrEqualTo(
                                root.get("lastLogin"),
                                lastLoginFrom
                        )
                );
            }

            if (lastLoginTo != null) {
                predicates.add(
                        criteriaBuilder.lessThanOrEqualTo(
                                root.get("lastLogin"),
                                lastLoginTo
                        )
                );
            }

            return criteriaBuilder.and(
                    predicates.toArray(Predicate[]::new)
            );
        };
    }
}

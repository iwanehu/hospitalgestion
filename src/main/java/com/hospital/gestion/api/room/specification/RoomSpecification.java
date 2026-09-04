package com.hospital.gestion.api.room.specification;

import com.hospital.gestion.api.common.enums.RoomStatus;
import com.hospital.gestion.api.common.enums.RoomType;
import com.hospital.gestion.api.room.entity.Room;
import org.springframework.data.jpa.domain.Specification;

import java.util.Locale;

public final class RoomSpecification {

    private RoomSpecification() {
    }

    public static Specification<Room> numberContains(
            String number
    ) {
        return (root, query, criteriaBuilder) -> {
            if (number == null || number.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            String pattern = "%"
                    + number.trim()
                    .toLowerCase(Locale.ROOT)
                    + "%";

            return criteriaBuilder.like(
                    criteriaBuilder.lower(
                            root.<String>get("number")
                    ),
                    pattern
            );
        };
    }

    public static Specification<Room> hasFloor(
            Integer floor
    ) {
        return (root, query, criteriaBuilder) -> {
            if (floor == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    root.get("floor"),
                    floor
            );
        };
    }

    public static Specification<Room> hasRoomType(
            RoomType roomType
    ) {
        return (root, query, criteriaBuilder) -> {
            if (roomType == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    root.get("roomType"),
                    roomType
            );
        };
    }

    public static Specification<Room> hasStatus(
            RoomStatus status
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

    public static Specification<Room> belongsToWard(
            Long wardId
    ) {
        return (root, query, criteriaBuilder) -> {
            if (wardId == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    root.get("ward").get("id"),
                    wardId
            );
        };
    }

    public static Specification<Room> belongsToDepartment(
            Long departmentId
    ) {
        return (root, query, criteriaBuilder) -> {
            if (departmentId == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    root.get("ward")
                            .get("department")
                            .get("id"),
                    departmentId
            );
        };
    }
}

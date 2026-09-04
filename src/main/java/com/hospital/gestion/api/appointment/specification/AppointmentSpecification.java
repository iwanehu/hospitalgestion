package com.hospital.gestion.api.appointment.specification;

import com.hospital.gestion.api.appointment.entity.Appointment;
import com.hospital.gestion.api.common.enums.AppointmentStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.Locale;

public final class AppointmentSpecification {

    private AppointmentSpecification() {
    }

    public static Specification<Appointment> hasStatus(
            AppointmentStatus status
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

    public static Specification<Appointment> belongsToPatient(
            Long patientId
    ) {
        return (root, query, criteriaBuilder) -> {
            if (patientId == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    root.get("patient").get("id"),
                    patientId
            );
        };
    }

    public static Specification<Appointment> belongsToDoctor(
            Long doctorId
    ) {
        return (root, query, criteriaBuilder) -> {
            if (doctorId == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    root.get("doctor").get("id"),
                    doctorId
            );
        };
    }

    public static Specification<Appointment> belongsToRoom(
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

    public static Specification<Appointment> belongsToDepartment(
            Long departmentId
    ) {
        return (root, query, criteriaBuilder) -> {
            if (departmentId == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    root.get("doctor")
                            .get("department")
                            .get("id"),
                    departmentId
            );
        };
    }

    public static Specification<Appointment> dateTimeFrom(
            LocalDateTime from
    ) {
        return (root, query, criteriaBuilder) -> {
            if (from == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.greaterThanOrEqualTo(
                    root.get("dateTime"),
                    from
            );
        };
    }

    public static Specification<Appointment> dateTimeTo(
            LocalDateTime to
    ) {
        return (root, query, criteriaBuilder) -> {
            if (to == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.lessThanOrEqualTo(
                    root.get("dateTime"),
                    to
            );
        };
    }

    public static Specification<Appointment> reasonContains(
            String reason
    ) {
        return (root, query, criteriaBuilder) -> {
            if (reason == null || reason.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            String pattern = "%"
                    + reason.trim()
                    .toLowerCase(Locale.ROOT)
                    + "%";

            return criteriaBuilder.like(
                    criteriaBuilder.lower(
                            root.<String>get("reason")
                    ),
                    pattern
            );
        };
    }
}

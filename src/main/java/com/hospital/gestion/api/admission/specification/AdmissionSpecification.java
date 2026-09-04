package com.hospital.gestion.api.admission.specification;

import com.hospital.gestion.api.admission.entity.Admission;
import com.hospital.gestion.api.common.enums.AdmissionStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public final class AdmissionSpecification {

    private AdmissionSpecification() {
    }

    public static Specification<Admission> hasStatus(
            AdmissionStatus status
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

    public static Specification<Admission> belongsToPatient(
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

    public static Specification<Admission> belongsToDoctor(
            Long doctorId
    ) {
        return (root, query, criteriaBuilder) -> {
            if (doctorId == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    root.get("attendingDoctor").get("id"),
                    doctorId
            );
        };
    }

    public static Specification<Admission> belongsToBed(
            Long bedId
    ) {
        return (root, query, criteriaBuilder) -> {
            if (bedId == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    root.get("bed").get("id"),
                    bedId
            );
        };
    }

    public static Specification<Admission> belongsToRoom(
            Long roomId
    ) {
        return (root, query, criteriaBuilder) -> {
            if (roomId == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    root.get("bed")
                            .get("room")
                            .get("id"),
                    roomId
            );
        };
    }

    public static Specification<Admission> belongsToWard(
            Long wardId
    ) {
        return (root, query, criteriaBuilder) -> {
            if (wardId == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    root.get("bed")
                            .get("room")
                            .get("ward")
                            .get("id"),
                    wardId
            );
        };
    }

    public static Specification<Admission> belongsToDepartment(
            Long departmentId
    ) {
        return (root, query, criteriaBuilder) -> {
            if (departmentId == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    root.get("bed")
                            .get("room")
                            .get("ward")
                            .get("department")
                            .get("id"),
                    departmentId
            );
        };
    }

    public static Specification<Admission> admittedFrom(
            LocalDateTime admittedFrom
    ) {
        return (root, query, criteriaBuilder) -> {
            if (admittedFrom == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.greaterThanOrEqualTo(
                    root.get("admittedAt"),
                    admittedFrom
            );
        };
    }

    public static Specification<Admission> admittedTo(
            LocalDateTime admittedTo
    ) {
        return (root, query, criteriaBuilder) -> {
            if (admittedTo == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.lessThanOrEqualTo(
                    root.get("admittedAt"),
                    admittedTo
            );
        };
    }
}

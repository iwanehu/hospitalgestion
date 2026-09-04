package com.hospital.gestion.api.security.authorization;

import com.hospital.gestion.api.admin.entity.Admin;
import com.hospital.gestion.api.admin.repository.AdminRepository;
import com.hospital.gestion.api.admission.repository.AdmissionRepository;
import com.hospital.gestion.api.appointment.repository.AppointmentRepository;
import com.hospital.gestion.api.common.enums.AdminLevel;
import com.hospital.gestion.api.common.enums.AdminPermission;
import com.hospital.gestion.api.doctor.repository.DoctorRepository;
import com.hospital.gestion.api.nurse.repository.NurseRepository;
import com.hospital.gestion.api.patient.repository.PatientRepository;
import com.hospital.gestion.api.receptionist.repository.ReceptionistRepository;
import com.hospital.gestion.api.security.user.HospitalUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component("hospitalAuthorization")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HospitalAuthorization {

    private final PatientRepository patientRepository;
    private final AdmissionRepository admissionRepository;
    private final AppointmentRepository appointmentRepository;

    private final DoctorRepository doctorRepository;
    private final NurseRepository nurseRepository;
    private final ReceptionistRepository receptionistRepository;
    private final AdminRepository adminRepository;

    public boolean isCurrentUser(
            Long userId,
            Authentication authentication
    ) {
        Long authenticatedUserId =
                extractAuthenticatedUserId(authentication);

        return userId != null
                && userId.equals(authenticatedUserId);
    }

    public boolean ownsPatient(
            Long patientId,
            Authentication authentication
    ) {
        Long userId =
                extractAuthenticatedUserId(authentication);

        return patientId != null
                && userId != null
                && patientRepository
                .existsByIdAndUser_Id(
                        patientId,
                        userId
                );
    }

    public boolean ownsAdmission(
            Long admissionId,
            Authentication authentication
    ) {
        Long userId =
                extractAuthenticatedUserId(authentication);

        return admissionId != null
                && userId != null
                && admissionRepository
                .existsByIdAndPatient_User_Id(
                        admissionId,
                        userId
                );
    }

    public boolean ownsAppointment(
            Long appointmentId,
            Authentication authentication
    ) {
        Long userId =
                extractAuthenticatedUserId(authentication);

        return appointmentId != null
                && userId != null
                && appointmentRepository
                .existsByIdAndPatient_User_Id(
                        appointmentId,
                        userId
                );
    }


    public boolean ownsDoctor(
            Long doctorId,
            Authentication authentication
    ) {
        Long userId =
                extractAuthenticatedUserId(authentication);

        return doctorId != null
                && userId != null
                && doctorRepository.existsByIdAndUser_Id(
                doctorId,
                userId
        );
    }

    public boolean ownsNurse(
            Long nurseId,
            Authentication authentication
    ) {
        Long userId =
                extractAuthenticatedUserId(authentication);

        return nurseId != null
                && userId != null
                && nurseRepository.existsByIdAndUser_Id(
                nurseId,
                userId
        );
    }

    public boolean ownsReceptionist(
            Long receptionistId,
            Authentication authentication
    ) {
        Long userId =
                extractAuthenticatedUserId(authentication);

        return receptionistId != null
                && userId != null
                && receptionistRepository
                .existsByIdAndUser_Id(
                        receptionistId,
                        userId
                );
    }

    public boolean ownsAdmin(
            Long adminId,
            Authentication authentication
    ) {
        Long userId =
                extractAuthenticatedUserId(authentication);

        return adminId != null
                && userId != null
                && adminRepository.existsByIdAndUser_Id(
                adminId,
                userId
        );
    }


    public boolean isSuperAdmin(
            Authentication authentication
    ) {
        return findAuthenticatedAdmin(authentication)
                .map(Admin::getAdminLevel)
                .filter(level ->
                        level == AdminLevel.SUPER_ADMIN
                )
                .isPresent();
    }

    public boolean hasAdminPermission(
            AdminPermission permission,
            Authentication authentication
    ) {
        if (permission == null) {
            return false;
        }

        return findAuthenticatedAdmin(authentication)
                .map(admin ->
                        admin.hasPermission(permission)
                )
                .orElse(false);
    }

    private Optional<Admin> findAuthenticatedAdmin(
            Authentication authentication
    ) {
        Long userId =
                extractAuthenticatedUserId(authentication);

        if (userId == null) {
            return Optional.empty();
        }

        return adminRepository.findByUser_Id(userId);
    }


    private Long extractAuthenticatedUserId(
            Authentication authentication
    ) {
        if (authentication == null
                || !authentication.isAuthenticated()) {
            return null;
        }

        if (!(authentication.getPrincipal()
                instanceof HospitalUserPrincipal principal)) {
            return null;
        }

        if (!principal.isEnabled()) {
            return null;
        }

        return principal.id();
    }




}

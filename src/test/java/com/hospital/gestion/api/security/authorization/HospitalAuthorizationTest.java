package com.hospital.gestion.api.security.authorization;

import com.hospital.gestion.api.admin.entity.Admin;
import com.hospital.gestion.api.admin.repository.AdminRepository;
import com.hospital.gestion.api.admission.repository.AdmissionRepository;
import com.hospital.gestion.api.appointment.repository.AppointmentRepository;
import com.hospital.gestion.api.common.enums.AdminLevel;
import com.hospital.gestion.api.common.enums.AdminPermission;
import com.hospital.gestion.api.common.enums.Role;
import com.hospital.gestion.api.doctor.repository.DoctorRepository;
import com.hospital.gestion.api.nurse.repository.NurseRepository;
import com.hospital.gestion.api.patient.repository.PatientRepository;
import com.hospital.gestion.api.receptionist.repository.ReceptionistRepository;
import com.hospital.gestion.api.security.user.HospitalUserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HospitalAuthorizationTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private AdmissionRepository admissionRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private NurseRepository nurseRepository;

    @Mock
    private ReceptionistRepository receptionistRepository;

    @Mock
    private AdminRepository adminRepository;

    @InjectMocks
    private HospitalAuthorization authorization;

    private HospitalUserPrincipal principal;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        principal = new HospitalUserPrincipal(
                5L,
                "patient@hospital.com",
                "encoded-password",
                Role.PATIENT,
                true
        );

        authentication =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        principal.getAuthorities()
                );
    }

    @Test
    void currentUserMatchesAuthenticatedPrincipal() {
        assertTrue(
                authorization.isCurrentUser(
                        5L,
                        authentication
                )
        );

        assertFalse(
                authorization.isCurrentUser(
                        99L,
                        authentication
                )
        );
    }

    @Test
    void patientOwnsLinkedPatientProfile() {
        when(
                patientRepository.existsByIdAndUser_Id(
                        1L,
                        5L
                )
        ).thenReturn(true);

        assertTrue(
                authorization.ownsPatient(
                        1L,
                        authentication
                )
        );
    }

    @Test
    void patientDoesNotOwnAnotherPatientProfile() {
        when(
                patientRepository.existsByIdAndUser_Id(
                        2L,
                        5L
                )
        ).thenReturn(false);

        assertFalse(
                authorization.ownsPatient(
                        2L,
                        authentication
                )
        );
    }

    @Test
    void ownershipChecksAdmissionAndAppointment() {
        when(
                admissionRepository
                        .existsByIdAndPatient_User_Id(
                                10L,
                                5L
                        )
        ).thenReturn(true);

        when(
                appointmentRepository
                        .existsByIdAndPatient_User_Id(
                                20L,
                                5L
                        )
        ).thenReturn(true);

        assertTrue(
                authorization.ownsAdmission(
                        10L,
                        authentication
                )
        );

        assertTrue(
                authorization.ownsAppointment(
                        20L,
                        authentication
                )
        );
    }

    @Test
    void ownershipChecksProfessionalProfiles() {
        when(
                doctorRepository.existsByIdAndUser_Id(
                        1L,
                        5L
                )
        ).thenReturn(true);

        when(
                nurseRepository.existsByIdAndUser_Id(
                        2L,
                        5L
                )
        ).thenReturn(true);

        when(
                receptionistRepository
                        .existsByIdAndUser_Id(
                                3L,
                                5L
                        )
        ).thenReturn(true);

        when(
                adminRepository.existsByIdAndUser_Id(
                        4L,
                        5L
                )
        ).thenReturn(true);

        assertTrue(
                authorization.ownsDoctor(
                        1L,
                        authentication
                )
        );

        assertTrue(
                authorization.ownsNurse(
                        2L,
                        authentication
                )
        );

        assertTrue(
                authorization.ownsReceptionist(
                        3L,
                        authentication
                )
        );

        assertTrue(
                authorization.ownsAdmin(
                        4L,
                        authentication
                )
        );
    }

    @Test
    void unauthenticatedRequestOwnsNothing() {
        assertFalse(
                authorization.ownsPatient(
                        1L,
                        null
                )
        );

        assertFalse(
                authorization.ownsAdmission(
                        1L,
                        null
                )
        );

        assertFalse(
                authorization.ownsAppointment(
                        1L,
                        null
                )
        );

        verifyNoInteractions(
                patientRepository,
                admissionRepository,
                appointmentRepository
        );
    }

    @Test
    void disabledPrincipalOwnsNothing() {
        HospitalUserPrincipal disabledPrincipal =
                new HospitalUserPrincipal(
                        5L,
                        "patient@hospital.com",
                        "encoded-password",
                        Role.PATIENT,
                        false
                );

        Authentication disabledAuthentication =
                new UsernamePasswordAuthenticationToken(
                        disabledPrincipal,
                        null,
                        disabledPrincipal.getAuthorities()
                );

        assertFalse(
                authorization.ownsPatient(
                        1L,
                        disabledAuthentication
                )
        );

        verifyNoInteractions(patientRepository);
    }

    @Test
    void recognizesSuperAdmin() {
        Admin admin = mock(Admin.class);

        when(admin.getAdminLevel())
                .thenReturn(AdminLevel.SUPER_ADMIN);

        when(adminRepository.findByUser_Id(5L))
                .thenReturn(Optional.of(admin));

        assertTrue(
                authorization.isSuperAdmin(
                        authentication
                )
        );
    }

    @Test
    void recognizesGrantedAdminPermission() {
        Admin admin = mock(Admin.class);

        when(
                admin.hasPermission(
                        AdminPermission.MANAGE_ROLES
                )
        ).thenReturn(true);

        when(adminRepository.findByUser_Id(5L))
                .thenReturn(Optional.of(admin));

        assertTrue(
                authorization.hasAdminPermission(
                        AdminPermission.MANAGE_ROLES,
                        authentication
                )
        );
    }

    @Test
    void missingAdminProfileHasNoAdminPrivileges() {
        when(adminRepository.findByUser_Id(5L))
                .thenReturn(Optional.empty());

        assertFalse(
                authorization.isSuperAdmin(
                        authentication
                )
        );

        assertFalse(
                authorization.hasAdminPermission(
                        AdminPermission.MANAGE_ROLES,
                        authentication
                )
        );
    }
}

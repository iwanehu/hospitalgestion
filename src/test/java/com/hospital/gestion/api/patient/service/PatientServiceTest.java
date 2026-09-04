package com.hospital.gestion.api.patient.service;

import com.hospital.gestion.api.admission.repository.AdmissionRepository;
import com.hospital.gestion.api.appointment.repository.AppointmentRepository;
import com.hospital.gestion.api.common.enums.BloodType;
import com.hospital.gestion.api.common.enums.Role;
import com.hospital.gestion.api.common.exception.ConflictException;
import com.hospital.gestion.api.common.helper.HospitalEntityHelper;
import com.hospital.gestion.api.patient.dto.PatientRequestDTO;
import com.hospital.gestion.api.patient.dto.PatientResponseDTO;
import com.hospital.gestion.api.patient.dto.PatientUpdateDTO;
import com.hospital.gestion.api.patient.entity.EmergencyContact;
import com.hospital.gestion.api.patient.entity.Patient;
import com.hospital.gestion.api.patient.mapper.PatientMapper;
import com.hospital.gestion.api.patient.repository.PatientRepository;
import com.hospital.gestion.api.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private AdmissionRepository admissionRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private PatientMapper patientMapper;

    @Mock
    private HospitalEntityHelper helper;

    @InjectMocks
    private PatientService patientService;

    private User user;
    private Patient patient;
    private PatientResponseDTO response;

    @BeforeEach
    void setUp() {
        user = mock(User.class);
        response = mock(PatientResponseDTO.class);

        lenient().when(user.getId()).thenReturn(5L);
        lenient().when(user.getRole()).thenReturn(Role.PATIENT);
        lenient().when(user.getIsActive()).thenReturn(true);

        patient = Patient.builder()
                .id(1L)
                .user(user)
                .bloodType(BloodType.O_POSITIVE)
                .birthDate(LocalDate.of(1990, 5, 15))
                .emergencyContact(
                        EmergencyContact.builder()
                                .name("Maria Familiar")
                                .phone("633444555")
                                .relationship("SISTER")
                                .build()
                )
                .allergies("Penicillin")
                .hasHealthInsurance(true)
                .healthInsuranceProvider("Sanitas")
                .healthInsuranceNumber("SAN-123")
                .medicalHistory("Mild asthma")
                .build();
    }

    @Test
    void createPatientValidatesNormalizesAndSavesPatient() {
        PatientRequestDTO request =
                new PatientRequestDTO(
                        5L,
                        BloodType.O_POSITIVE,
                        LocalDate.of(1990, 5, 15),
                        "Maria Familiar",
                        "633444555",
                        "SISTER",
                        "Penicillin",
                        true,
                        "Sanitas",
                        "SAN-123",
                        "Mild asthma"
                );

        when(helper.findUserByIdForUpdate(5L))
                .thenReturn(user);

        when(patientRepository.existsByUser_Id(5L))
                .thenReturn(false);

        when(patientMapper.toEntity(request, user))
                .thenReturn(patient);

        when(patientRepository.save(patient))
                .thenReturn(patient);

        when(patientMapper.toResponseDTO(patient))
                .thenReturn(response);

        PatientResponseDTO result =
                patientService.createPatient(request);

        assertSame(response, result);

        verify(helper).validateBirthDate(
                LocalDate.of(1990, 5, 15)
        );

        verify(helper).validateEmergencyContact(
                "Maria Familiar",
                "633444555",
                "SISTER"
        );

        verify(helper).validateInsurance(
                true,
                "Sanitas",
                "SAN-123"
        );

        verify(helper).normalizePatient(patient, true);
        verify(patientRepository).save(patient);
    }

    @Test
    void createPatientRejectsUserWithoutPatientRole() {
        PatientRequestDTO request =
                new PatientRequestDTO(
                        5L,
                        null,
                        LocalDate.of(1990, 5, 15),
                        null,
                        null,
                        null,
                        null,
                        false,
                        null,
                        null,
                        null
                );

        when(user.getRole()).thenReturn(Role.DOCTOR);

        when(helper.findUserByIdForUpdate(5L))
                .thenReturn(user);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> patientService.createPatient(request)
        );

        assertEquals(
                "User must have PATIENT role",
                exception.getMessage()
        );

        verify(patientRepository, never())
                .save(any(Patient.class));
    }

    @Test
    void createPatientRejectsInactiveUser() {
        PatientRequestDTO request =
                new PatientRequestDTO(
                        5L,
                        null,
                        LocalDate.of(1990, 5, 15),
                        null,
                        null,
                        null,
                        null,
                        false,
                        null,
                        null,
                        null
                );

        when(user.getIsActive()).thenReturn(false);

        when(helper.findUserByIdForUpdate(5L))
                .thenReturn(user);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> patientService.createPatient(request)
        );

        assertEquals(
                "Inactive user cannot be registered as a patient",
                exception.getMessage()
        );

        verify(patientRepository, never())
                .save(any(Patient.class));
    }

    @Test
    void createPatientRejectsUserAlreadyAssociated() {
        PatientRequestDTO request =
                new PatientRequestDTO(
                        5L,
                        null,
                        LocalDate.of(1990, 5, 15),
                        null,
                        null,
                        null,
                        null,
                        false,
                        null,
                        null,
                        null
                );

        when(helper.findUserByIdForUpdate(5L))
                .thenReturn(user);

        when(patientRepository.existsByUser_Id(5L))
                .thenReturn(true);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> patientService.createPatient(request)
        );

        assertEquals(
                "User is already associated with a patient",
                exception.getMessage()
        );

        verify(helper, never()).validateInsurance(
                any(),
                any(),
                any()
        );

        verify(patientRepository, never())
                .save(any(Patient.class));
    }

    @Test
    void getPatientByEmailNormalizesEmail() {
        when(
                helper.normalizeRequiredText(
                        " ADMIN@HOSPITAL.COM ",
                        "Email"
                )
        ).thenReturn("ADMIN@HOSPITAL.COM");

        when(
                patientRepository.findByUser_EmailIgnoreCase(
                        "admin@hospital.com"
                )
        ).thenReturn(java.util.Optional.of(patient));

        when(patientMapper.toResponseDTO(patient))
                .thenReturn(response);

        PatientResponseDTO result =
                patientService.getPatientByEmail(
                        " ADMIN@HOSPITAL.COM "
                );

        assertSame(response, result);

        verify(patientRepository)
                .findByUser_EmailIgnoreCase(
                        "admin@hospital.com"
                );
    }

    @Test
    void updatePatientUpdatesMedicalContactAndInsuranceData() {
        PatientUpdateDTO request =
                new PatientUpdateDTO(
                        BloodType.A_POSITIVE,
                        " Updated Contact ",
                        " 600111222 ",
                        " MOTHER ",
                        " Ibuprofen ",
                        true,
                        " Adeslas ",
                        " ADE-999 ",
                        " Updated history "
                );

        when(helper.findPatientByIdForUpdate(1L))
                .thenReturn(patient);

        when(helper.normalizeNullableText(" Updated Contact "))
                .thenReturn("Updated Contact");

        when(helper.normalizeNullableText(" 600111222 "))
                .thenReturn("600111222");

        when(helper.normalizeNullableText(" MOTHER "))
                .thenReturn("MOTHER");

        when(helper.normalizeNullableText(" Ibuprofen "))
                .thenReturn("Ibuprofen");

        when(helper.normalizeNullableText(" Updated history "))
                .thenReturn("Updated history");

        when(helper.normalizeNullableText(" Adeslas "))
                .thenReturn("Adeslas");

        when(helper.normalizeNullableText(" ADE-999 "))
                .thenReturn("ADE-999");

        when(patientRepository.save(patient))
                .thenReturn(patient);

        when(patientMapper.toResponseDTO(patient))
                .thenReturn(response);

        PatientResponseDTO result =
                patientService.updatePatient(1L, request);

        assertSame(response, result);
        assertEquals(BloodType.A_POSITIVE, patient.getBloodType());

        assertEquals(
                "Updated Contact",
                patient.getEmergencyContact().getName()
        );

        assertEquals(
                "600111222",
                patient.getEmergencyContact().getPhone()
        );

        assertEquals(
                "MOTHER",
                patient.getEmergencyContact().getRelationship()
        );

        assertEquals("Ibuprofen", patient.getAllergies());
        assertEquals("Updated history", patient.getMedicalHistory());
        assertTrue(patient.getHasHealthInsurance());
        assertEquals("Adeslas", patient.getHealthInsuranceProvider());
        assertEquals("ADE-999", patient.getHealthInsuranceNumber());

        verify(helper).validateEmergencyContact(
                "Updated Contact",
                "600111222",
                "MOTHER"
        );

        verify(helper).validateInsurance(
                true,
                "Adeslas",
                "ADE-999"
        );

        verify(patientRepository).save(patient);
    }

    @Test
    void updatePatientDisablesInsuranceAndClearsDetails() {
        PatientUpdateDTO request =
                new PatientUpdateDTO(
                        null,
                        null,
                        null,
                        null,
                        null,
                        false,
                        null,
                        null,
                        null
                );

        when(helper.findPatientByIdForUpdate(1L))
                .thenReturn(patient);

        when(patientRepository.save(patient))
                .thenReturn(patient);

        when(patientMapper.toResponseDTO(patient))
                .thenReturn(response);

        patientService.updatePatient(1L, request);

        assertFalse(patient.getHasHealthInsurance());
        assertNull(patient.getHealthInsuranceProvider());
        assertNull(patient.getHealthInsuranceNumber());

        verify(helper, never()).validateInsurance(
                any(),
                any(),
                any()
        );
    }

    @Test
    void deletePatientDeletesPatientWithoutHistory() {
        when(helper.findPatientByIdForUpdate(1L))
                .thenReturn(patient);

        when(admissionRepository.countByPatient_Id(1L))
                .thenReturn(0L);

        when(appointmentRepository.countByPatient_Id(1L))
                .thenReturn(0L);

        patientService.deletePatient(1L);

        verify(patientRepository).delete(patient);
        verify(patientRepository).flush();
    }

    @Test
    void deletePatientRejectsAdmissionHistory() {
        when(helper.findPatientByIdForUpdate(1L))
                .thenReturn(patient);

        when(admissionRepository.countByPatient_Id(1L))
                .thenReturn(1L);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> patientService.deletePatient(1L)
        );

        assertEquals(
                "Patient cannot be deleted because they have admission history",
                exception.getMessage()
        );

        verify(appointmentRepository, never())
                .countByPatient_Id(anyLong());

        verify(patientRepository, never())
                .delete(any(Patient.class));
    }

    @Test
    void deletePatientRejectsAppointmentHistory() {
        when(helper.findPatientByIdForUpdate(1L))
                .thenReturn(patient);

        when(admissionRepository.countByPatient_Id(1L))
                .thenReturn(0L);

        when(appointmentRepository.countByPatient_Id(1L))
                .thenReturn(2L);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> patientService.deletePatient(1L)
        );

        assertEquals(
                "Patient cannot be deleted because they have appointment history",
                exception.getMessage()
        );

        verify(patientRepository, never())
                .delete(any(Patient.class));
    }

    @Test
    void deletePatientTranslatesDatabaseConstraintViolation() {
        when(helper.findPatientByIdForUpdate(1L))
                .thenReturn(patient);

        when(admissionRepository.countByPatient_Id(1L))
                .thenReturn(0L);

        when(appointmentRepository.countByPatient_Id(1L))
                .thenReturn(0L);

        doThrow(
                new DataIntegrityViolationException(
                        "Foreign key constraint"
                )
        ).when(patientRepository).flush();

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> patientService.deletePatient(1L)
        );

        assertEquals(
                "Patient cannot be deleted because they are associated with hospital records",
                exception.getMessage()
        );

        verify(patientRepository).delete(patient);
        verify(patientRepository).flush();
    }

    @Test
    void existsByEmailUsesNormalizedEmail() {
        when(
                helper.normalizeRequiredText(
                        " patient@hospital.com ",
                        "Email"
                )
        ).thenReturn("patient@hospital.com");

        when(
                patientRepository.existsByUser_EmailIgnoreCase(
                        "patient@hospital.com"
                )
        ).thenReturn(true);

        boolean result = patientService.existsByEmail(
                " patient@hospital.com "
        );

        assertTrue(result);

        verify(patientRepository)
                .existsByUser_EmailIgnoreCase(
                        "patient@hospital.com"
                );
    }

    @SuppressWarnings("unchecked")
    @Test
    void getPatientsAppliesAllFilters() {
        Pageable pageable = PageRequest.of(
                0,
                20,
                Sort.by("user.lastName").ascending()
        );

        LocalDate from = LocalDate.of(1950, 1, 1);
        LocalDate to = LocalDate.of(2026, 1, 1);

        Page<Patient> patientPage =
                new PageImpl<>(
                        List.of(patient),
                        pageable,
                        1
                );

        when(
                patientRepository.findAll(
                        any(Specification.class),
                        eq(pageable)
                )
        ).thenReturn(patientPage);

        when(patientMapper.toResponseDTO(patient))
                .thenReturn(response);

        Page<PatientResponseDTO> result =
                patientService.getPatients(
                        " pedro ",
                        BloodType.O_POSITIVE,
                        true,
                        " sanitas ",
                        true,
                        from,
                        to,
                        pageable
                );

        assertEquals(1, result.getTotalElements());

        assertSame(
                response,
                result.getContent().getFirst()
        );

        verify(helper).validatePageable(
                eq(pageable),
                anySet()
        );

        verify(helper).validateBloodType(
                BloodType.O_POSITIVE
        );

        verify(helper).validateDateRangeLocalDate(
                from,
                to
        );

        verify(patientRepository).findAll(
                any(Specification.class),
                eq(pageable)
        );
    }
}

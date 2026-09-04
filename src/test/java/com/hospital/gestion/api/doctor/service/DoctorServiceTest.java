package com.hospital.gestion.api.doctor.service;

import com.hospital.gestion.api.admission.repository.AdmissionRepository;
import com.hospital.gestion.api.appointment.repository.AppointmentRepository;
import com.hospital.gestion.api.common.enums.Role;
import com.hospital.gestion.api.common.enums.Specialty;
import com.hospital.gestion.api.common.exception.ConflictException;
import com.hospital.gestion.api.common.helper.HospitalEntityHelper;
import com.hospital.gestion.api.department.entity.Department;
import com.hospital.gestion.api.doctor.dto.DoctorRequestDTO;
import com.hospital.gestion.api.doctor.dto.DoctorResponseDTO;
import com.hospital.gestion.api.doctor.dto.DoctorUpdateDTO;
import com.hospital.gestion.api.doctor.entity.Doctor;
import com.hospital.gestion.api.doctor.mapper.DoctorMapper;
import com.hospital.gestion.api.doctor.repository.DoctorRepository;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorServiceTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private AdmissionRepository admissionRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private DoctorMapper doctorMapper;

    @Mock
    private HospitalEntityHelper helper;

    @InjectMocks
    private DoctorService doctorService;

    private User user;
    private Department department;
    private Doctor doctor;
    private DoctorResponseDTO response;

    @BeforeEach
    void setUp() {
        user = mock(User.class);
        department = mock(Department.class);
        response = mock(DoctorResponseDTO.class);

        lenient().when(user.getId()).thenReturn(2L);
        lenient().when(user.getRole()).thenReturn(Role.DOCTOR);
        lenient().when(user.getIsActive()).thenReturn(true);
        lenient().when(department.getId()).thenReturn(1L);

        doctor = Doctor.builder()
                .id(1L)
                .user(user)
                .department(department)
                .specialty(Specialty.CARDIOLOGY)
                .medicalLicenseNumber("CAR-12345")
                .yearsOfExperience(9)
                .biography("Cardiologist")
                .build();
    }

    @Test
    void createDoctorNormalizesAndSavesDoctor() {
        DoctorRequestDTO request =
                new DoctorRequestDTO(
                        2L,
                        1L,
                        Specialty.CARDIOLOGY,
                        " car-12345 ",
                        9,
                        " Cardiologist "
                );

        when(
                helper.normalizeLicense(
                        " car-12345 "
                )
        ).thenReturn("CAR-12345");

        when(helper.findUserByIdForUpdate(2L))
                .thenReturn(user);

        when(doctorRepository.existsByUser_Id(2L))
                .thenReturn(false);

        when(
                doctorRepository
                        .existsByMedicalLicenseNumberIgnoreCase(
                                "CAR-12345"
                        )
        ).thenReturn(false);

        when(helper.findActiveDepartmentById(1L))
                .thenReturn(department);

        when(
                doctorMapper.toEntity(
                        request,
                        user,
                        department
                )
        ).thenReturn(doctor);

        when(
                helper.normalizeNullableText(
                        " Cardiologist "
                )
        ).thenReturn("Cardiologist");

        when(doctorRepository.save(doctor))
                .thenReturn(doctor);

        when(doctorMapper.toResponseDTO(doctor))
                .thenReturn(response);

        DoctorResponseDTO result =
                doctorService.createDoctor(request);

        assertSame(response, result);

        assertEquals(
                "CAR-12345",
                doctor.getMedicalLicenseNumber()
        );

        assertEquals(
                "Cardiologist",
                doctor.getBiography()
        );

        verify(helper).validateSpecialty(
                Specialty.CARDIOLOGY
        );

        verify(helper).validateExperience(9);
        verify(helper).findActiveDepartmentById(1L);
        verify(doctorRepository).save(doctor);
    }

    @Test
    void createDoctorRejectsUserWithoutDoctorRole() {
        DoctorRequestDTO request =
                new DoctorRequestDTO(
                        2L,
                        1L,
                        Specialty.CARDIOLOGY,
                        "CAR-12345",
                        9,
                        null
                );

        when(
                helper.normalizeLicense("CAR-12345")
        ).thenReturn("CAR-12345");

        when(user.getRole()).thenReturn(Role.PATIENT);

        when(helper.findUserByIdForUpdate(2L))
                .thenReturn(user);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> doctorService.createDoctor(request)
        );

        assertEquals(
                "User must have DOCTOR role",
                exception.getMessage()
        );

        verify(doctorRepository, never())
                .save(any(Doctor.class));
    }

    @Test
    void createDoctorRejectsInactiveUser() {
        DoctorRequestDTO request =
                new DoctorRequestDTO(
                        2L,
                        1L,
                        Specialty.CARDIOLOGY,
                        "CAR-12345",
                        9,
                        null
                );

        when(
                helper.normalizeLicense("CAR-12345")
        ).thenReturn("CAR-12345");

        when(user.getIsActive()).thenReturn(false);

        when(helper.findUserByIdForUpdate(2L))
                .thenReturn(user);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> doctorService.createDoctor(request)
        );

        assertEquals(
                "Inactive user cannot be registered as a doctor",
                exception.getMessage()
        );

        verify(doctorRepository, never())
                .save(any(Doctor.class));
    }

    @Test
    void createDoctorRejectsUserAlreadyAssociated() {
        DoctorRequestDTO request =
                new DoctorRequestDTO(
                        2L,
                        1L,
                        Specialty.CARDIOLOGY,
                        "CAR-12345",
                        9,
                        null
                );

        when(
                helper.normalizeLicense("CAR-12345")
        ).thenReturn("CAR-12345");

        when(helper.findUserByIdForUpdate(2L))
                .thenReturn(user);

        when(doctorRepository.existsByUser_Id(2L))
                .thenReturn(true);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> doctorService.createDoctor(request)
        );

        assertEquals(
                "User is already associated with a doctor",
                exception.getMessage()
        );

        verify(
                doctorRepository,
                never()
        ).existsByMedicalLicenseNumberIgnoreCase(
                anyString()
        );

        verify(doctorRepository, never())
                .save(any(Doctor.class));
    }

    @Test
    void createDoctorRejectsDuplicateMedicalLicense() {
        DoctorRequestDTO request =
                new DoctorRequestDTO(
                        2L,
                        1L,
                        Specialty.CARDIOLOGY,
                        " car-12345 ",
                        9,
                        null
                );

        when(
                helper.normalizeLicense(
                        " car-12345 "
                )
        ).thenReturn("CAR-12345");

        when(helper.findUserByIdForUpdate(2L))
                .thenReturn(user);

        when(doctorRepository.existsByUser_Id(2L))
                .thenReturn(false);

        when(
                doctorRepository
                        .existsByMedicalLicenseNumberIgnoreCase(
                                "CAR-12345"
                        )
        ).thenReturn(true);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> doctorService.createDoctor(request)
        );

        assertEquals(
                "Medical license is already registered: CAR-12345",
                exception.getMessage()
        );

        verify(helper, never())
                .findActiveDepartmentById(anyLong());

        verify(doctorRepository, never())
                .save(any(Doctor.class));
    }

    @Test
    void getDoctorByLicenseUsesNormalizedLicense() {
        when(
                helper.normalizeLicense(
                        " car-12345 "
                )
        ).thenReturn("CAR-12345");

        when(
                doctorRepository
                        .findByMedicalLicenseNumberIgnoreCase(
                                "CAR-12345"
                        )
        ).thenReturn(Optional.of(doctor));

        when(doctorMapper.toResponseDTO(doctor))
                .thenReturn(response);

        DoctorResponseDTO result =
                doctorService.getDoctorByLicense(
                        " car-12345 "
                );

        assertSame(response, result);

        verify(doctorRepository)
                .findByMedicalLicenseNumberIgnoreCase(
                        "CAR-12345"
                );
    }

    @Test
    void updateDoctorValidatesAndSavesChanges() {
        Department newDepartment =
                mock(Department.class);

        DoctorUpdateDTO request =
                new DoctorUpdateDTO(
                        2L,
                        Specialty.NEUROSURGERY,
                        12,
                        " Updated biography "
                );

        when(helper.findDoctorByIdForUpdate(1L))
                .thenReturn(doctor);

        when(helper.findActiveDepartmentById(2L))
                .thenReturn(newDepartment);

        when(
                helper.normalizeNullableText(
                        " Updated biography "
                )
        ).thenReturn("Updated biography");

        when(doctorRepository.save(doctor))
                .thenReturn(doctor);

        when(doctorMapper.toResponseDTO(doctor))
                .thenReturn(response);

        DoctorResponseDTO result =
                doctorService.updateDoctor(
                        1L,
                        request
                );

        assertSame(response, result);

        assertEquals(
                "Updated biography",
                doctor.getBiography()
        );

        verify(helper).validateSpecialty(
                Specialty.NEUROSURGERY
        );

        verify(helper).validateExperience(12);

        verify(doctorMapper).updateEntity(
                doctor,
                request,
                newDepartment
        );

        verify(doctorRepository).save(doctor);
    }

    @Test
    void deleteDoctorDeletesDoctorWithoutHistory() {
        when(helper.findDoctorByIdForUpdate(1L))
                .thenReturn(doctor);

        when(
                admissionRepository
                        .countByAttendingDoctor_Id(1L)
        ).thenReturn(0L);

        when(
                appointmentRepository.countByDoctor_Id(1L)
        ).thenReturn(0L);

        doctorService.deleteDoctor(1L);

        verify(doctorRepository).delete(doctor);
        verify(doctorRepository).flush();
    }

    @Test
    void deleteDoctorRejectsAdmissionHistory() {
        when(helper.findDoctorByIdForUpdate(1L))
                .thenReturn(doctor);

        when(
                admissionRepository
                        .countByAttendingDoctor_Id(1L)
        ).thenReturn(1L);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> doctorService.deleteDoctor(1L)
        );

        assertEquals(
                "Doctor cannot be deleted because they have admission history",
                exception.getMessage()
        );

        verify(appointmentRepository, never())
                .countByDoctor_Id(anyLong());

        verify(doctorRepository, never())
                .delete(any(Doctor.class));
    }

    @Test
    void deleteDoctorRejectsAppointmentHistory() {
        when(helper.findDoctorByIdForUpdate(1L))
                .thenReturn(doctor);

        when(
                admissionRepository
                        .countByAttendingDoctor_Id(1L)
        ).thenReturn(0L);

        when(
                appointmentRepository.countByDoctor_Id(1L)
        ).thenReturn(2L);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> doctorService.deleteDoctor(1L)
        );

        assertEquals(
                "Doctor cannot be deleted because they have appointment history",
                exception.getMessage()
        );

        verify(doctorRepository, never())
                .delete(any(Doctor.class));
    }

    @Test
    void deleteDoctorTranslatesDatabaseConstraintViolation() {
        when(helper.findDoctorByIdForUpdate(1L))
                .thenReturn(doctor);

        when(
                admissionRepository
                        .countByAttendingDoctor_Id(1L)
        ).thenReturn(0L);

        when(
                appointmentRepository.countByDoctor_Id(1L)
        ).thenReturn(0L);

        doThrow(
                new DataIntegrityViolationException(
                        "Foreign key constraint"
                )
        ).when(doctorRepository).flush();

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> doctorService.deleteDoctor(1L)
        );

        assertEquals(
                "Doctor cannot be deleted because they are associated with records",
                exception.getMessage()
        );

        verify(doctorRepository).delete(doctor);
        verify(doctorRepository).flush();
    }

    @Test
    void existsByLicenseUsesNormalizedLicense() {
        when(
                helper.normalizeLicense(
                        " car-12345 "
                )
        ).thenReturn("CAR-12345");

        when(
                doctorRepository
                        .existsByMedicalLicenseNumberIgnoreCase(
                                "CAR-12345"
                        )
        ).thenReturn(true);

        boolean result = doctorService.existsByLicense(
                " car-12345 "
        );

        assertTrue(result);

        verify(doctorRepository)
                .existsByMedicalLicenseNumberIgnoreCase(
                        "CAR-12345"
                );
    }

    @SuppressWarnings("unchecked")
    @Test
    void getDoctorsAppliesAllFilters() {
        Pageable pageable = PageRequest.of(
                0,
                20,
                Sort.by("user.lastName").ascending()
        );

        Page<Doctor> doctorPage =
                new PageImpl<>(
                        List.of(doctor),
                        pageable,
                        1
                );

        when(
                doctorRepository.findAll(
                        any(Specification.class),
                        eq(pageable)
                )
        ).thenReturn(doctorPage);

        when(doctorMapper.toResponseDTO(doctor))
                .thenReturn(response);

        Page<DoctorResponseDTO> result =
                doctorService.getDoctors(
                        " laura ",
                        1L,
                        Specialty.CARDIOLOGY,
                        true,
                        0,
                        60,
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

        verify(helper).validateDepartmentExist(1L);

        verify(helper).validateSpecialty(
                Specialty.CARDIOLOGY
        );

        verify(helper).validateExperience(0);
        verify(helper).validateExperience(60);

        verify(doctorRepository).findAll(
                any(Specification.class),
                eq(pageable)
        );
    }
}

package com.hospital.gestion.api.admission.service;

import com.hospital.gestion.api.admission.dto.AdmissionDischargeDTO;
import com.hospital.gestion.api.admission.dto.AdmissionRequestDTO;
import com.hospital.gestion.api.admission.dto.AdmissionResponseDTO;
import com.hospital.gestion.api.admission.dto.AdmissionTransferDTO;
import com.hospital.gestion.api.admission.dto.AdmissionUpdateDTO;
import com.hospital.gestion.api.admission.entity.Admission;
import com.hospital.gestion.api.admission.mapper.AdmissionMapper;
import com.hospital.gestion.api.admission.repository.AdmissionRepository;
import com.hospital.gestion.api.bed.entity.Bed;
import com.hospital.gestion.api.common.enums.AdmissionStatus;
import com.hospital.gestion.api.common.enums.BedStatus;
import com.hospital.gestion.api.common.exception.ConflictException;
import com.hospital.gestion.api.common.helper.HospitalEntityHelper;
import com.hospital.gestion.api.doctor.entity.Doctor;
import com.hospital.gestion.api.patient.entity.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdmissionServiceTest {

    @Mock
    private AdmissionRepository admissionRepository;

    @Mock
    private AdmissionMapper admissionMapper;

    @Mock
    private HospitalEntityHelper helper;

    @InjectMocks
    private AdmissionService admissionService;

    private Patient patient;
    private Doctor doctor;
    private Bed bed;
    private Admission admission;
    private AdmissionResponseDTO response;

    @BeforeEach
    void setUp() {
        patient = mock(Patient.class);
        doctor = mock(Doctor.class);

        lenient().when(patient.getId())
                .thenReturn(1L);

        lenient().when(doctor.getId())
                .thenReturn(1L);

        bed = Bed.builder()
                .id(1L)
                .bedNumber("BED-001")
                .status(BedStatus.AVAILABLE)
                .build();

        admission = Admission.builder()
                .id(1L)
                .patient(patient)
                .bed(bed)
                .attendingDoctor(doctor)
                .status(AdmissionStatus.ACTIVE)
                .admissionReason("Cardiac monitoring")
                .admittedAt(LocalDateTime.now())
                .notes("Initial notes")
                .build();

        response = mock(AdmissionResponseDTO.class);
    }

    @Test
    void createAdmissionOccupiesBedAndSavesAdmission() {
        AdmissionRequestDTO request =
                new AdmissionRequestDTO(
                        1L,
                        1L,
                        1L,
                        " Cardiac monitoring ",
                        " Initial notes "
                );

        when(helper.findPatientByIdForUpdate(1L))
                .thenReturn(patient);

        when(
                admissionRepository
                        .existsByPatient_IdAndStatus(
                                1L,
                                AdmissionStatus.ACTIVE
                        )
        ).thenReturn(false);

        when(helper.findBedByIdForUpdate(1L))
                .thenReturn(bed);

        when(
                admissionRepository
                        .existsByBed_IdAndStatus(
                                1L,
                                AdmissionStatus.ACTIVE
                        )
        ).thenReturn(false);

        when(helper.findDoctorById(1L))
                .thenReturn(doctor);

        when(
                helper.normalizeNullableText(
                        " Initial notes "
                )
        ).thenReturn("Initial notes");

        when(
                admissionRepository.save(
                        any(Admission.class)
                )
        ).thenAnswer(invocation ->
                invocation.getArgument(0)
        );

        when(
                admissionMapper.toResponseDTO(
                        any(Admission.class)
                )
        ).thenReturn(response);

        AdmissionResponseDTO result =
                admissionService.createAdmission(request);

        assertSame(response, result);
        assertEquals(BedStatus.OCCUPIED, bed.getStatus());

        ArgumentCaptor<Admission> captor =
                ArgumentCaptor.forClass(Admission.class);

        verify(admissionRepository)
                .save(captor.capture());

        Admission saved = captor.getValue();

        assertSame(patient, saved.getPatient());
        assertSame(doctor, saved.getAttendingDoctor());
        assertSame(bed, saved.getBed());

        assertEquals(
                "Cardiac monitoring",
                saved.getAdmissionReason()
        );

        assertEquals("Initial notes", saved.getNotes());
        assertEquals(AdmissionStatus.ACTIVE, saved.getStatus());
        assertNotNull(saved.getAdmittedAt());
    }

    @Test
    void createAdmissionRejectsPatientWithActiveAdmission() {
        AdmissionRequestDTO request =
                new AdmissionRequestDTO(
                        1L,
                        1L,
                        1L,
                        "Cardiac monitoring",
                        null
                );

        when(helper.findPatientByIdForUpdate(1L))
                .thenReturn(patient);

        when(
                admissionRepository
                        .existsByPatient_IdAndStatus(
                                1L,
                                AdmissionStatus.ACTIVE
                        )
        ).thenReturn(true);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> admissionService.createAdmission(request)
        );

        assertEquals(
                "Patient already has an active admission",
                exception.getMessage()
        );

        verify(helper, never())
                .findBedByIdForUpdate(anyLong());

        verify(admissionRepository, never())
                .save(any(Admission.class));
    }

    @Test
    void createAdmissionRejectsUnavailableBed() {
        bed = Bed.builder()
                .id(1L)
                .bedNumber("BED-001")
                .status(BedStatus.CLEANING)
                .build();

        AdmissionRequestDTO request =
                new AdmissionRequestDTO(
                        1L,
                        1L,
                        1L,
                        "Cardiac monitoring",
                        null
                );

        when(helper.findPatientByIdForUpdate(1L))
                .thenReturn(patient);

        when(
                admissionRepository
                        .existsByPatient_IdAndStatus(
                                1L,
                                AdmissionStatus.ACTIVE
                        )
        ).thenReturn(false);

        when(helper.findBedByIdForUpdate(1L))
                .thenReturn(bed);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> admissionService.createAdmission(request)
        );

        assertEquals(
                "Bed must be available or reserved",
                exception.getMessage()
        );

        verify(admissionRepository, never())
                .save(any(Admission.class));
    }

    @Test
    void createAdmissionRejectsBedWithActiveAdmission() {
        AdmissionRequestDTO request =
                new AdmissionRequestDTO(
                        1L,
                        1L,
                        1L,
                        "Cardiac monitoring",
                        null
                );

        when(helper.findPatientByIdForUpdate(1L))
                .thenReturn(patient);

        when(
                admissionRepository
                        .existsByPatient_IdAndStatus(
                                1L,
                                AdmissionStatus.ACTIVE
                        )
        ).thenReturn(false);

        when(helper.findBedByIdForUpdate(1L))
                .thenReturn(bed);

        when(
                admissionRepository
                        .existsByBed_IdAndStatus(
                                1L,
                                AdmissionStatus.ACTIVE
                        )
        ).thenReturn(true);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> admissionService.createAdmission(request)
        );

        assertEquals(
                "Bed already has an active admission",
                exception.getMessage()
        );

        assertEquals(BedStatus.AVAILABLE, bed.getStatus());

        verify(admissionRepository, never())
                .save(any(Admission.class));
    }

    @Test
    void updateAdmissionUpdatesDoctorReasonAndNotes() {
        AdmissionUpdateDTO request =
                new AdmissionUpdateDTO(
                        2L,
                        " Extended cardiac monitoring ",
                        " Updated notes "
                );

        Doctor newDoctor = mock(Doctor.class);

        when(helper.findAdmissionByIdForUpdate(1L))
                .thenReturn(admission);

        when(helper.findDoctorById(2L))
                .thenReturn(newDoctor);

        when(
                helper.normalizeNullableText(
                        " Updated notes "
                )
        ).thenReturn("Updated notes");

        when(
                admissionRepository
                        .saveAndFlush(admission)
        ).thenReturn(admission);

        when(admissionMapper.toResponseDTO(admission))
                .thenReturn(response);

        AdmissionResponseDTO result =
                admissionService.updateAdmission(
                        1L,
                        request
                );

        assertSame(response, result);
        assertSame(
                newDoctor,
                admission.getAttendingDoctor()
        );

        assertEquals(
                "Extended cardiac monitoring",
                admission.getAdmissionReason()
        );

        assertEquals(
                "Updated notes",
                admission.getNotes()
        );

        verify(helper).validateAdmissionReason(
                " Extended cardiac monitoring "
        );

        verify(admissionRepository)
                .saveAndFlush(admission);
    }

    @Test
    void updateAdmissionRejectsInactiveAdmission() {
        Admission dischargedAdmission =
                Admission.builder()
                        .id(1L)
                        .patient(patient)
                        .bed(bed)
                        .attendingDoctor(doctor)
                        .status(AdmissionStatus.DISCHARGED)
                        .admissionReason("Monitoring")
                        .admittedAt(LocalDateTime.now())
                        .build();

        AdmissionUpdateDTO request =
                new AdmissionUpdateDTO(
                        2L,
                        "Updated reason",
                        null
                );

        when(helper.findAdmissionByIdForUpdate(1L))
                .thenReturn(dischargedAdmission);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> admissionService.updateAdmission(
                        1L,
                        request
                )
        );

        assertEquals(
                "Admission is not active",
                exception.getMessage()
        );

        verify(helper, never())
                .findDoctorById(anyLong());

        verify(admissionRepository, never())
                .saveAndFlush(any(Admission.class));
    }

    @Test
    void dischargeAdmissionDischargesAndReleasesBed() {
        bed.occupy();

        AdmissionDischargeDTO request =
                new AdmissionDischargeDTO(
                        " Patient discharged "
                );

        when(helper.findAdmissionByIdForUpdate(1L))
                .thenReturn(admission);

        when(helper.findBedByIdForUpdate(1L))
                .thenReturn(bed);

        when(
                helper.normalizeNullableText(
                        "Patient discharged"
                )
        ).thenReturn("Patient discharged");

        when(
                admissionRepository
                        .saveAndFlush(admission)
        ).thenReturn(admission);

        when(admissionMapper.toResponseDTO(admission))
                .thenReturn(response);

        AdmissionResponseDTO result =
                admissionService.dischargeAdmission(
                        1L,
                        request
                );

        assertSame(response, result);

        assertEquals(
                AdmissionStatus.DISCHARGED,
                admission.getStatus()
        );

        assertNotNull(admission.getDischargedAt());
        assertEquals(BedStatus.CLEANING, bed.getStatus());

        assertEquals(
                "Initial notes"
                        + System.lineSeparator()
                        + "Patient discharged",
                admission.getNotes()
        );

        verify(admissionRepository)
                .saveAndFlush(admission);
    }

    @Test
    void transferAdmissionMovesPatientToNewBed() {
        Bed currentBed = Bed.builder()
                .id(1L)
                .bedNumber("BED-001")
                .status(BedStatus.OCCUPIED)
                .build();

        Bed newBed = Bed.builder()
                .id(2L)
                .bedNumber("BED-002")
                .status(BedStatus.AVAILABLE)
                .build();

        Admission currentAdmission =
                Admission.builder()
                        .id(1L)
                        .patient(patient)
                        .bed(currentBed)
                        .attendingDoctor(doctor)
                        .status(AdmissionStatus.ACTIVE)
                        .admissionReason("Monitoring")
                        .admittedAt(LocalDateTime.now())
                        .notes("Initial notes")
                        .build();

        AdmissionTransferDTO request =
                new AdmissionTransferDTO(
                        2L,
                        " Closer observation "
                );

        when(helper.findAdmissionByIdForUpdate(1L))
                .thenReturn(currentAdmission);

        when(helper.findPatientByIdForUpdate(1L))
                .thenReturn(patient);

        when(helper.findBedByIdForUpdate(1L))
                .thenReturn(currentBed);

        when(helper.findBedByIdForUpdate(2L))
                .thenReturn(newBed);

        when(
                admissionRepository
                        .existsByBed_IdAndStatus(
                                2L,
                                AdmissionStatus.ACTIVE
                        )
        ).thenReturn(false);

        when(
                helper.normalizeNullableText(
                        "Transfer: Closer observation"
                )
        ).thenReturn(
                "Transfer: Closer observation"
        );

        when(
                helper.normalizeNullableText(
                        "Transferred from admission 1: "
                                + "Closer observation"
                )
        ).thenReturn(
                "Transferred from admission 1: "
                        + "Closer observation"
        );

        when(
                admissionRepository.saveAndFlush(
                        any(Admission.class)
                )
        ).thenAnswer(invocation ->
                invocation.getArgument(0)
        );

        when(
                admissionMapper.toResponseDTO(
                        any(Admission.class)
                )
        ).thenReturn(response);

        AdmissionResponseDTO result =
                admissionService.transferAdmission(
                        1L,
                        request
                );

        assertSame(response, result);

        assertEquals(
                AdmissionStatus.TRANSFERRED,
                currentAdmission.getStatus()
        );

        assertEquals(
                BedStatus.CLEANING,
                currentBed.getStatus()
        );

        assertEquals(
                BedStatus.OCCUPIED,
                newBed.getStatus()
        );

        ArgumentCaptor<Admission> captor =
                ArgumentCaptor.forClass(Admission.class);

        verify(admissionRepository, times(2))
                .saveAndFlush(captor.capture());

        Admission createdAdmission =
                captor.getAllValues().get(1);

        assertEquals(
                AdmissionStatus.ACTIVE,
                createdAdmission.getStatus()
        );

        assertSame(patient, createdAdmission.getPatient());
        assertSame(newBed, createdAdmission.getBed());
        assertSame(
                doctor,
                createdAdmission.getAttendingDoctor()
        );

        assertEquals(
                "Transferred from admission 1: "
                        + "Closer observation",
                createdAdmission.getNotes()
        );
    }

    @Test
    void transferAdmissionRejectsSameBed() {
        AdmissionTransferDTO request =
                new AdmissionTransferDTO(
                        1L,
                        "No movement"
                );

        when(helper.findAdmissionByIdForUpdate(1L))
                .thenReturn(admission);

        when(helper.findPatientByIdForUpdate(1L))
                .thenReturn(patient);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> admissionService.transferAdmission(
                        1L,
                        request
                )
        );

        assertEquals(
                "Patient is already assigned to this bed",
                exception.getMessage()
        );

        verify(admissionRepository, never())
                .saveAndFlush(any(Admission.class));
    }

    @Test
    void cancelAdmissionCancelsAndReleasesBed() {
        bed.occupy();

        when(helper.findAdmissionByIdForUpdate(1L))
                .thenReturn(admission);

        when(helper.findBedByIdForUpdate(1L))
                .thenReturn(bed);

        when(
                admissionRepository
                        .saveAndFlush(admission)
        ).thenReturn(admission);

        when(admissionMapper.toResponseDTO(admission))
                .thenReturn(response);

        AdmissionResponseDTO result =
                admissionService.cancelAdmission(1L);

        assertSame(response, result);

        assertEquals(
                AdmissionStatus.CANCELLED,
                admission.getStatus()
        );

        assertNotNull(admission.getDischargedAt());
        assertEquals(BedStatus.CLEANING, bed.getStatus());

        verify(admissionRepository)
                .saveAndFlush(admission);
    }

    @Test
    void deleteAdmissionDeletesCancelledAdmission() {
        Admission cancelledAdmission =
                Admission.builder()
                        .id(1L)
                        .patient(patient)
                        .bed(bed)
                        .attendingDoctor(doctor)
                        .status(AdmissionStatus.CANCELLED)
                        .admissionReason("Cancelled")
                        .admittedAt(LocalDateTime.now())
                        .build();

        when(helper.findAdmissionByIdForUpdate(1L))
                .thenReturn(cancelledAdmission);

        admissionService.deleteAdmission(1L);

        verify(admissionRepository)
                .delete(cancelledAdmission);
    }

    @Test
    void deleteAdmissionRejectsActiveAdmission() {
        when(helper.findAdmissionByIdForUpdate(1L))
                .thenReturn(admission);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> admissionService.deleteAdmission(1L)
        );

        assertEquals(
                "Active admission cannot be deleted",
                exception.getMessage()
        );

        verify(admissionRepository, never())
                .delete(any(Admission.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    void getAdmissionsAppliesAllFilters() {
        Pageable pageable = PageRequest.of(
                0,
                20,
                Sort.by("admittedAt").descending()
        );

        LocalDateTime from =
                LocalDateTime.of(2026, 8, 1, 0, 0);

        LocalDateTime to =
                LocalDateTime.of(2026, 9, 30, 23, 59);

        Page<Admission> admissionPage =
                new PageImpl<>(
                        List.of(admission),
                        pageable,
                        1
                );

        when(
                admissionRepository.findAll(
                        any(Specification.class),
                        eq(pageable)
                )
        ).thenReturn(admissionPage);

        when(admissionMapper.toResponseDTO(admission))
                .thenReturn(response);

        Page<AdmissionResponseDTO> result =
                admissionService.getAdmissions(
                        AdmissionStatus.ACTIVE,
                        1L,
                        2L,
                        3L,
                        4L,
                        5L,
                        6L,
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

        verify(helper)
                .validateAdmissionStatus(
                        AdmissionStatus.ACTIVE
                );

        verify(helper).validatePatientExists(1L);
        verify(helper).validateDoctorExists(2L);
        verify(helper).validateBedExists(3L);
        verify(helper).validateRoomExists(4L);
        verify(helper).validateWardExists(5L);
        verify(helper).validateDepartmentExist(6L);
        verify(helper).validateDateRange(from, to);

        verify(admissionRepository).findAll(
                any(Specification.class),
                eq(pageable)
        );
    }
}

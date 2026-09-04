package com.hospital.gestion.api.appointment.service;

import com.hospital.gestion.api.appointment.dto.AppointmentCancelDTO;
import com.hospital.gestion.api.appointment.dto.AppointmentRequestDTO;
import com.hospital.gestion.api.appointment.dto.AppointmentResponseDTO;
import com.hospital.gestion.api.appointment.dto.AppointmentStatusUpdateDTO;
import com.hospital.gestion.api.appointment.dto.AppointmentUpdateDTO;
import com.hospital.gestion.api.appointment.entity.Appointment;
import com.hospital.gestion.api.appointment.mapper.AppointmentMapper;
import com.hospital.gestion.api.appointment.repository.AppointmentRepository;
import com.hospital.gestion.api.common.enums.AppointmentStatus;
import com.hospital.gestion.api.common.exception.ConflictException;
import com.hospital.gestion.api.common.helper.HospitalEntityHelper;
import com.hospital.gestion.api.doctor.entity.Doctor;
import com.hospital.gestion.api.patient.entity.Patient;
import com.hospital.gestion.api.room.entity.Room;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    private static final ZoneId ZONE_MADRID =
            ZoneId.of("Europe/Madrid");

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private AppointmentMapper appointmentMapper;

    @Mock
    private HospitalEntityHelper helper;

    @InjectMocks
    private AppointmentService appointmentService;

    private Doctor doctor;
    private Patient patient;
    private Appointment appointment;
    private AppointmentResponseDTO response;
    private LocalDateTime futureDate;

    @BeforeEach
    void setUp() {
        doctor = mock(Doctor.class);
        patient = mock(Patient.class);
        response = mock(AppointmentResponseDTO.class);

        lenient().when(doctor.getId())
                .thenReturn(1L);

        lenient().when(patient.getId())
                .thenReturn(1L);

        futureDate = LocalDateTime.now(ZONE_MADRID)
                .plusDays(2);

        appointment = Appointment.builder()
                .id(1L)
                .doctor(doctor)
                .patient(patient)
                .room(null)
                .dateTime(futureDate)
                .reason("Cardiology consultation")
                .notes("Initial notes")
                .status(AppointmentStatus.SCHEDULED)
                .build();
    }

    @Test
    void createAppointmentNormalizesAndSavesAppointment() {
        AppointmentRequestDTO request =
                new AppointmentRequestDTO(
                        1L,
                        1L,
                        null,
                        futureDate,
                        " Cardiology consultation ",
                        " Initial notes "
                );

        when(helper.findDoctorByIdForUpdate(1L))
                .thenReturn(doctor);

        when(helper.findPatientByIdForUpdate(1L))
                .thenReturn(patient);

        when(
                appointmentMapper.toEntity(
                        request,
                        doctor,
                        patient,
                        null
                )
        ).thenReturn(appointment);

        when(
                helper.normalizeNullableText(
                        " Initial notes "
                )
        ).thenReturn("Initial notes");

        when(appointmentRepository.save(appointment))
                .thenReturn(appointment);

        when(
                appointmentMapper.toResponseDTO(
                        appointment
                )
        ).thenReturn(response);

        AppointmentResponseDTO result =
                appointmentService.createAppointment(request);

        assertSame(response, result);

        assertEquals(
                "Cardiology consultation",
                appointment.getReason()
        );

        assertEquals(
                "Initial notes",
                appointment.getNotes()
        );

        verify(appointmentRepository).existsDoctorConflict(
                eq(1L),
                eq(futureDate.minusMinutes(30)),
                eq(futureDate.plusMinutes(30)),
                anyCollection()
        );

        verify(appointmentRepository).existsPatientConflict(
                eq(1L),
                eq(futureDate.minusMinutes(30)),
                eq(futureDate.plusMinutes(30)),
                anyCollection()
        );

        verify(appointmentRepository).save(appointment);
    }

    @Test
    void createAppointmentRejectsPastDate() {
        LocalDateTime pastDate =
                LocalDateTime.now(ZONE_MADRID)
                        .minusHours(1);

        AppointmentRequestDTO request =
                new AppointmentRequestDTO(
                        1L,
                        1L,
                        null,
                        pastDate,
                        "Consultation",
                        null
                );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> appointmentService
                        .createAppointment(request)
        );

        assertEquals(
                "Appointment date must be in the future",
                exception.getMessage()
        );

        verifyNoInteractions(
                appointmentRepository,
                appointmentMapper,
                helper
        );
    }

    @Test
    void createAppointmentRejectsDoctorConflict() {
        AppointmentRequestDTO request =
                new AppointmentRequestDTO(
                        1L,
                        1L,
                        null,
                        futureDate,
                        "Consultation",
                        null
                );

        when(helper.findDoctorByIdForUpdate(1L))
                .thenReturn(doctor);

        when(helper.findPatientByIdForUpdate(1L))
                .thenReturn(patient);

        when(
                appointmentRepository.existsDoctorConflict(
                        eq(1L),
                        any(LocalDateTime.class),
                        any(LocalDateTime.class),
                        anyCollection()
                )
        ).thenReturn(true);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> appointmentService
                        .createAppointment(request)
        );

        assertEquals(
                "Doctor already has another appointment during this time",
                exception.getMessage()
        );

        verify(appointmentRepository, never())
                .save(any(Appointment.class));
    }

    @Test
    void createAppointmentRejectsPatientConflict() {
        AppointmentRequestDTO request =
                new AppointmentRequestDTO(
                        1L,
                        1L,
                        null,
                        futureDate,
                        "Consultation",
                        null
                );

        when(helper.findDoctorByIdForUpdate(1L))
                .thenReturn(doctor);

        when(helper.findPatientByIdForUpdate(1L))
                .thenReturn(patient);

        when(
                appointmentRepository.existsPatientConflict(
                        eq(1L),
                        any(LocalDateTime.class),
                        any(LocalDateTime.class),
                        anyCollection()
                )
        ).thenReturn(true);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> appointmentService
                        .createAppointment(request)
        );

        assertEquals(
                "Patient already has another appointment during this time",
                exception.getMessage()
        );

        verify(appointmentRepository, never())
                .save(any(Appointment.class));
    }

    @Test
    void createAppointmentRejectsRoomConflict() {
        Room room = mock(Room.class);

        when(room.getId()).thenReturn(3L);

        AppointmentRequestDTO request =
                new AppointmentRequestDTO(
                        1L,
                        1L,
                        3L,
                        futureDate,
                        "Consultation",
                        null
                );

        when(helper.findDoctorByIdForUpdate(1L))
                .thenReturn(doctor);

        when(helper.findPatientByIdForUpdate(1L))
                .thenReturn(patient);

        when(helper.findRoomByIdForUpdate(3L))
                .thenReturn(room);

        when(
                appointmentRepository.existsRoomConflict(
                        eq(3L),
                        any(LocalDateTime.class),
                        any(LocalDateTime.class),
                        anyCollection()
                )
        ).thenReturn(true);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> appointmentService
                        .createAppointment(request)
        );

        assertEquals(
                "Room is already assigned to another appointment during this time",
                exception.getMessage()
        );

        verify(appointmentRepository, never())
                .save(any(Appointment.class));
    }

    @Test
    void updateAppointmentMarksAppointmentAsRescheduled() {
        Doctor newDoctor = mock(Doctor.class);

        when(newDoctor.getId()).thenReturn(2L);

        LocalDateTime newDate =
                futureDate.plusDays(1);

        AppointmentUpdateDTO request =
                new AppointmentUpdateDTO(
                        2L,
                        null,
                        newDate,
                        " Follow-up consultation ",
                        " Updated notes "
                );

        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointment.setConfirmedAt(
                LocalDateTime.now(ZONE_MADRID)
        );

        when(helper.findAppointmentByIdForUpdate(1L))
                .thenReturn(appointment);

        when(helper.findDoctorByIdForUpdate(2L))
                .thenReturn(newDoctor);

        when(helper.findPatientByIdForUpdate(1L))
                .thenReturn(patient);

        when(
                helper.normalizeNullableText(
                        " Updated notes "
                )
        ).thenReturn("Updated notes");

        when(
                appointmentRepository
                        .saveAndFlush(appointment)
        ).thenReturn(appointment);

        when(
                appointmentMapper.toResponseDTO(
                        appointment
                )
        ).thenReturn(response);

        AppointmentResponseDTO result =
                appointmentService.updateAppointment(
                        1L,
                        request
                );

        assertSame(response, result);

        assertEquals(
                AppointmentStatus.RESCHEDULED,
                appointment.getStatus()
        );

        assertNull(appointment.getConfirmedAt());

        assertEquals(
                "Follow-up consultation",
                appointment.getReason()
        );

        assertEquals(
                "Updated notes",
                appointment.getNotes()
        );

        verify(
                appointmentRepository
        ).existsDoctorConflictExcludingAppointment(
                eq(2L),
                eq(1L),
                eq(newDate.minusMinutes(30)),
                eq(newDate.plusMinutes(30)),
                anyCollection()
        );

        verify(appointmentMapper).updateEntity(
                appointment,
                request,
                newDoctor,
                null
        );

        verify(appointmentRepository)
                .saveAndFlush(appointment);
    }

    @Test
    void updateAppointmentRejectsCompletedAppointment() {
        appointment.setStatus(
                AppointmentStatus.COMPLETED
        );

        AppointmentUpdateDTO request =
                new AppointmentUpdateDTO(
                        1L,
                        null,
                        futureDate.plusDays(1),
                        "Updated consultation",
                        null
                );

        when(helper.findAppointmentByIdForUpdate(1L))
                .thenReturn(appointment);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> appointmentService.updateAppointment(
                        1L,
                        request
                )
        );

        assertEquals(
                "Appointment with status COMPLETED cannot be modified",
                exception.getMessage()
        );

        verify(appointmentRepository, never())
                .saveAndFlush(any(Appointment.class));
    }

    @Test
    void appointmentCompletesValidStatusLifecycle() {
        appointment.setDateTime(
                LocalDateTime.now(ZONE_MADRID)
                        .minusMinutes(10)
        );

        when(helper.findAppointmentByIdForUpdate(1L))
                .thenReturn(appointment);

        when(
                appointmentRepository
                        .saveAndFlush(appointment)
        ).thenReturn(appointment);

        when(
                appointmentMapper.toResponseDTO(
                        appointment
                )
        ).thenReturn(response);

        appointmentService.updateAppointmentStatus(
                1L,
                new AppointmentStatusUpdateDTO(
                        AppointmentStatus.CONFIRMED
                )
        );

        assertEquals(
                AppointmentStatus.CONFIRMED,
                appointment.getStatus()
        );

        assertNotNull(appointment.getConfirmedAt());

        appointmentService.updateAppointmentStatus(
                1L,
                new AppointmentStatusUpdateDTO(
                        AppointmentStatus.IN_PROGRESS
                )
        );

        assertEquals(
                AppointmentStatus.IN_PROGRESS,
                appointment.getStatus()
        );

        appointmentService.updateAppointmentStatus(
                1L,
                new AppointmentStatusUpdateDTO(
                        AppointmentStatus.COMPLETED
                )
        );

        assertEquals(
                AppointmentStatus.COMPLETED,
                appointment.getStatus()
        );

        assertNotNull(appointment.getCompletedAt());

        verify(helper, times(3))
                .findAppointmentByIdForUpdate(1L);

        verify(appointmentRepository, times(3))
                .saveAndFlush(appointment);
    }

    @Test
    void futureAppointmentCannotBeMarkedAsNoShow() {
        when(helper.findAppointmentByIdForUpdate(1L))
                .thenReturn(appointment);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> appointmentService
                        .updateAppointmentStatus(
                                1L,
                                new AppointmentStatusUpdateDTO(
                                        AppointmentStatus.NO_SHOW
                                )
                        )
        );

        assertEquals(
                "A future appointment cannot be marked as no-show",
                exception.getMessage()
        );

        verify(appointmentRepository, never())
                .saveAndFlush(any(Appointment.class));
    }

    @Test
    void cancelAppointmentRegistersReasonAndTimestamp() {
        AppointmentCancelDTO request =
                new AppointmentCancelDTO(
                        " Patient requested cancellation "
                );

        when(helper.findAppointmentByIdForUpdate(1L))
                .thenReturn(appointment);

        when(
                appointmentRepository
                        .saveAndFlush(appointment)
        ).thenReturn(appointment);

        when(
                appointmentMapper.toResponseDTO(
                        appointment
                )
        ).thenReturn(response);

        AppointmentResponseDTO result =
                appointmentService.cancelAppointment(
                        1L,
                        request
                );

        assertSame(response, result);

        assertEquals(
                AppointmentStatus.CANCELLED,
                appointment.getStatus()
        );

        assertEquals(
                "Patient requested cancellation",
                appointment.getCancellationReason()
        );

        assertNotNull(appointment.getCancelledAt());

        verify(appointmentRepository)
                .saveAndFlush(appointment);
    }

    @Test
    void cancelAppointmentRejectsCompletedAppointment() {
        appointment.setStatus(
                AppointmentStatus.COMPLETED
        );

        AppointmentCancelDTO request =
                new AppointmentCancelDTO(
                        "Too late"
                );

        when(helper.findAppointmentByIdForUpdate(1L))
                .thenReturn(appointment);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> appointmentService.cancelAppointment(
                        1L,
                        request
                )
        );

        assertEquals(
                "Appointment with status COMPLETED cannot be cancelled",
                exception.getMessage()
        );

        verify(appointmentRepository, never())
                .saveAndFlush(any(Appointment.class));
    }

    @Test
    void deleteAppointmentDeletesCancelledAppointment() {
        appointment.setStatus(
                AppointmentStatus.CANCELLED
        );

        when(helper.findAppointmentByIdForUpdate(1L))
                .thenReturn(appointment);

        appointmentService.deleteAppointment(1L);

        verify(appointmentRepository)
                .delete(appointment);
    }

    @SuppressWarnings("unchecked")
    @Test
    void getAppointmentsAppliesAllFilters() {
        Pageable pageable = PageRequest.of(
                0,
                20,
                Sort.by("dateTime").ascending()
        );

        LocalDateTime from =
                LocalDateTime.of(2026, 8, 1, 0, 0);

        LocalDateTime to =
                LocalDateTime.of(2026, 9, 30, 23, 59);

        Page<Appointment> appointmentPage =
                new PageImpl<>(
                        List.of(appointment),
                        pageable,
                        1
                );

        when(
                appointmentRepository.findAll(
                        any(Specification.class),
                        eq(pageable)
                )
        ).thenReturn(appointmentPage);

        when(
                appointmentMapper.toResponseDTO(
                        appointment
                )
        ).thenReturn(response);

        Page<AppointmentResponseDTO> result =
                appointmentService.getAppointments(
                        AppointmentStatus.SCHEDULED,
                        1L,
                        2L,
                        3L,
                        4L,
                        from,
                        to,
                        " cardiac ",
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

        verify(helper).validateAppoinmentStatus(
                AppointmentStatus.SCHEDULED
        );

        verify(helper).validatePatientExists(1L);
        verify(helper).validateDoctorExists(2L);
        verify(helper).validateRoomExists(3L);
        verify(helper).validateDepartmentExist(4L);
        verify(helper).validateDateRange(from, to);

        verify(appointmentRepository).findAll(
                any(Specification.class),
                eq(pageable)
        );
    }
}

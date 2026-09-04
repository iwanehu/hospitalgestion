package com.hospital.gestion.api.appointment.service;

import com.hospital.gestion.api.appointment.dto.AppointmentCancelDTO;
import com.hospital.gestion.api.appointment.dto.AppointmentCountResponse;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



import org.springframework.data.jpa.domain.Specification;

import java.util.Set;

import static com.hospital.gestion.api.appointment.specification.AppointmentSpecification.belongsToDepartment;
import static com.hospital.gestion.api.appointment.specification.AppointmentSpecification.belongsToDoctor;
import static com.hospital.gestion.api.appointment.specification.AppointmentSpecification.belongsToPatient;
import static com.hospital.gestion.api.appointment.specification.AppointmentSpecification.belongsToRoom;
import static com.hospital.gestion.api.appointment.specification.AppointmentSpecification.dateTimeFrom;
import static com.hospital.gestion.api.appointment.specification.AppointmentSpecification.dateTimeTo;
import static com.hospital.gestion.api.appointment.specification.AppointmentSpecification.hasStatus;
import static com.hospital.gestion.api.appointment.specification.AppointmentSpecification.reasonContains;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentService {

    private static final ZoneId ZONE_MADRID =
            ZoneId.of("Europe/Madrid");

    private static final int APPOINTMENT_DURATION_MINUTES = 30;
    private static final List<AppointmentStatus>
            BLOCKING_STATUSES = List.of(
            AppointmentStatus.SCHEDULED,
            AppointmentStatus.RESCHEDULED,
            AppointmentStatus.CONFIRMED,
            AppointmentStatus.IN_PROGRESS
    );









    private final AppointmentRepository appointmentRepository;

    private final AppointmentMapper appointmentMapper;
    private final HospitalEntityHelper helper;




    private static final Set<String>
            ALLOWED_SORT_PROPERTIES = Set.of(
            "id",
            "status",
            "doctor.id",
            "doctor.department.id",
            "patient.id",
            "room.id",
            "dateTime",
            "reason",
            "cancelledAt",
            "confirmedAt",
            "completedAt",
            "createdAt",
            "updatedAt"
    );

    // ========================================
    // CREATE
    // ========================================

    @Transactional
    public AppointmentResponseDTO createAppointment(
            AppointmentRequestDTO request
    ) {
        log.info(
                "Creating appointment for patient: {} "
                        + "with doctor: {}",
                request.patientId(),
                request.doctorId()
        );

        validateAppointmentDate(request.dateTime());
        validateReason(request.reason());

        Doctor doctor = helper.findDoctorByIdForUpdate(
                request.doctorId()
        );

        Patient patient = helper.findPatientByIdForUpdate(
                request.patientId()
        );

        Room room = request.roomId() == null
                ? null
                : helper.findRoomByIdForUpdate(request.roomId());

        validateScheduleConflicts(
                null,
                doctor.getId(),
                patient.getId(),
                room == null ? null : room.getId(),
                request.dateTime()
        );

        Appointment appointment =
                appointmentMapper.toEntity(
                        request,
                        doctor,
                        patient,
                        room
                );

        appointment.setReason(request.reason().trim());
        appointment.setNotes(
                helper.normalizeNullableText(request.notes())
        );

        Appointment savedAppointment =
                appointmentRepository.save(appointment);

        log.info(
                "Appointment created successfully with id: {}",
                savedAppointment.getId()
        );

        return appointmentMapper.toResponseDTO(
                savedAppointment
        );
    }

    // ========================================
    // GET ALL
    // ========================================

    @Transactional(readOnly = true)
    public List<AppointmentResponseDTO> getAllAppointments() {
        return appointmentMapper.toResponseDTOList(
                appointmentRepository.findAll()
        );
    }

    @Transactional(readOnly = true)
    public Page<AppointmentResponseDTO> getAllAppointments(
            Pageable pageable
    ) {
        helper.validatePageable(pageable, ALLOWED_SORT_PROPERTIES);

        return appointmentRepository.findAll(pageable)
                .map(appointmentMapper::toResponseDTO);
    }

    // ========================================
    // GET BY ID
    // ========================================

    @Transactional(readOnly = true)
    public AppointmentResponseDTO getAppointmentById(
            Long id
    ) {
        return appointmentMapper.toResponseDTO(
                helper.findAppointmentById(id)
        );
    }

    // ========================================
    // GET BY DOCTOR
    // ========================================

    @Transactional(readOnly = true)
    public List<AppointmentResponseDTO>
    getAppointmentsByDoctor(
            Long doctorId
    ) {
        helper.validateDoctorExists(doctorId);

        return appointmentMapper.toResponseDTOList(
                appointmentRepository.findByDoctor_Id(
                        doctorId
                )
        );
    }

    @Transactional(readOnly = true)
    public Page<AppointmentResponseDTO>
    getAppointmentsByDoctor(
            Long doctorId,
            Pageable pageable
    ) {
        helper.validateDoctorExists(doctorId);
        helper.validatePageable(pageable, ALLOWED_SORT_PROPERTIES);

        return appointmentRepository
                .findByDoctor_Id(doctorId, pageable)
                .map(appointmentMapper::toResponseDTO);
    }

    // ========================================
    // GET BY PATIENT
    // ========================================

    @Transactional(readOnly = true)
    public List<AppointmentResponseDTO>
    getAppointmentsByPatient(
            Long patientId
    ) {
        helper.validatePatientExists(patientId);

        return appointmentMapper.toResponseDTOList(
                appointmentRepository.findByPatient_Id(
                        patientId
                )
        );
    }

    @Transactional(readOnly = true)
    public Page<AppointmentResponseDTO>
    getAppointmentsByPatient(
            Long patientId,
            Pageable pageable
    ) {
        helper.validatePatientExists(patientId);
        helper.validatePageable(pageable, ALLOWED_SORT_PROPERTIES);

        return appointmentRepository
                .findByPatient_Id(patientId, pageable)
                .map(appointmentMapper::toResponseDTO);
    }

    // ========================================
    // GET BY ROOM
    // ========================================

    @Transactional(readOnly = true)
    public List<AppointmentResponseDTO> getAppointmentsByRoom(
            Long roomId
    ) {
        helper.validateRoomExists(roomId);

        return appointmentMapper.toResponseDTOList(
                appointmentRepository.findByRoom_Id(roomId)
        );
    }

    // ========================================
    // GET BY STATUS
    // ========================================

    @Transactional(readOnly = true)
    public List<AppointmentResponseDTO>
    getAppointmentsByStatus(
            AppointmentStatus status
    ) {
        helper.validateAppoinmentStatus(status);

        return appointmentMapper.toResponseDTOList(
                appointmentRepository.findByStatus(status)
        );
    }

    @Transactional(readOnly = true)
    public Page<AppointmentResponseDTO>
    getAppointmentsByStatus(
            AppointmentStatus status,
            Pageable pageable
    ) {
        helper.validateAppoinmentStatus(status);
        helper.validatePageable(pageable, ALLOWED_SORT_PROPERTIES);

        return appointmentRepository
                .findByStatus(status, pageable)
                .map(appointmentMapper::toResponseDTO);
    }

    // ========================================
    // GET BY DATE RANGE
    // ========================================

    @Transactional(readOnly = true)
    public List<AppointmentResponseDTO>
    getAppointmentsByDateRange(
            LocalDateTime start,
            LocalDateTime end
    ) {
        helper.validateDateRange(start, end);

        return appointmentMapper.toResponseDTOList(
                appointmentRepository.findByDateTimeBetween(
                        start,
                        end
                )
        );
    }

    @Transactional(readOnly = true)
    public Page<AppointmentResponseDTO>
    getAppointmentsByDateRange(
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    ) {
        helper.validateDateRange(start, end);
        helper.validatePageable(pageable, ALLOWED_SORT_PROPERTIES);

        return appointmentRepository
                .findByDateTimeBetween(
                        start,
                        end,
                        pageable
                )
                .map(appointmentMapper::toResponseDTO);
    }

    // ========================================
    // GET UPCOMING
    // ========================================

    @Transactional(readOnly = true)
    public List<AppointmentResponseDTO>
    getUpcomingAppointments() {
        LocalDateTime now =
                LocalDateTime.now(ZONE_MADRID);

        return appointmentMapper.toResponseDTOList(
                appointmentRepository
                        .findByDateTimeAfterAndStatusInOrderByDateTimeAsc(
                                now,
                                BLOCKING_STATUSES
                        )
        );
    }

    @Transactional(readOnly = true)
    public Page<AppointmentResponseDTO>
    getUpcomingAppointments(
            Pageable pageable
    ) {
        helper.validatePageable(pageable, ALLOWED_SORT_PROPERTIES);

        return appointmentRepository
                .findByDateTimeAfterAndStatusIn(
                        LocalDateTime.now(ZONE_MADRID),
                        BLOCKING_STATUSES,
                        pageable
                )
                .map(appointmentMapper::toResponseDTO);
    }

    // ========================================
    // GET BY DEPARTMENT
    // ========================================

    @Transactional(readOnly = true)
    public List<AppointmentResponseDTO>
    getAppointmentsByDepartment(
            Long departmentId
    ) {
        helper.validateDepartmentExist(departmentId);

        return appointmentMapper.toResponseDTOList(
                appointmentRepository
                        .findByDoctor_Department_Id(
                                departmentId
                        )
        );
    }

    @Transactional(readOnly = true)
    public Page<AppointmentResponseDTO>
    getAppointmentsByDepartment(
            Long departmentId,
            Pageable pageable
    ) {
        helper.validateDepartmentExist(departmentId);
        helper.validatePageable(pageable, ALLOWED_SORT_PROPERTIES);

        return appointmentRepository
                .findByDoctor_Department_Id(
                        departmentId,
                        pageable
                )
                .map(appointmentMapper::toResponseDTO);
    }

    // ========================================
    // UPDATE / RESCHEDULE
    // ========================================

    @Transactional
    public AppointmentResponseDTO updateAppointment(
            Long id,
            AppointmentUpdateDTO request
    ) {
        log.info("Updating appointment: {}", id);

        Appointment appointment =
                helper.findAppointmentByIdForUpdate(id);

        validateModifiableAppointment(appointment);
        validateAppointmentDate(request.dateTime());
        validateReason(request.reason());

        Doctor doctor = helper.findDoctorByIdForUpdate(
                request.doctorId()
        );

        Patient patient = helper.findPatientByIdForUpdate(
                appointment.getPatient().getId()
        );

        Room room = request.roomId() == null
                ? null
                : helper.findRoomByIdForUpdate(
                request.roomId()
        );

        validateScheduleConflicts(
                appointment.getId(),
                doctor.getId(),
                patient.getId(),
                room == null ? null : room.getId(),
                request.dateTime()
        );

        /*
         * Estos valores deben compararse antes de que el mapper
         * modifique la entidad.
         */
        boolean dateChanged =
                !appointment.getDateTime()
                        .isEqual(request.dateTime());

        boolean doctorChanged =
                !Objects.equals(
                        appointment.getDoctor().getId(),
                        doctor.getId()
                );

        Long currentRoomId =
                appointment.getRoom() == null
                        ? null
                        : appointment.getRoom().getId();

        Long newRoomId =
                room == null
                        ? null
                        : room.getId();

        boolean roomChanged =
                !Objects.equals(
                        currentRoomId,
                        newRoomId
                );

        boolean scheduleChanged =
                dateChanged
                        || doctorChanged
                        || roomChanged;

        appointmentMapper.updateEntity(
                appointment,
                request,
                doctor,
                room
        );

        appointment.setReason(
                request.reason().trim()
        );

        appointment.setNotes(
                helper.normalizeNullableText(
                        request.notes()
                )
        );

        if (scheduleChanged) {
            appointment.setStatus(
                    AppointmentStatus.RESCHEDULED
            );


            appointment.setConfirmedAt(null);
        }

        Appointment updatedAppointment =
                appointmentRepository.saveAndFlush(
                        appointment
                );

        log.info(
                "Appointment updated successfully: {}. "
                        + "Schedule changed: {}",
                updatedAppointment.getId(),
                scheduleChanged
        );

        return appointmentMapper.toResponseDTO(
                updatedAppointment
        );
    }

    // ========================================
    // STATUS UPDATE
    // ========================================

    @Transactional
    public AppointmentResponseDTO updateAppointmentStatus(
            Long id,
            AppointmentStatusUpdateDTO request
    ) {
        log.info(
                "Updating appointment {} to status: {}",
                id,
                request.status()
        );

        Appointment appointment =
                helper.findAppointmentByIdForUpdate(id);

        helper.validateAppoinmentStatus(request.status());

        switch (request.status()) {
            case CONFIRMED ->
                    confirmAppointment(appointment);

            case IN_PROGRESS ->
                    startAppointment(appointment);

            case COMPLETED ->
                    completeAppointment(appointment);

            case NO_SHOW ->
                    markAppointmentAsNoShow(appointment);

            case CANCELLED ->
                    throw new IllegalArgumentException(
                            "Use the cancellation endpoint "
                                    + "to cancel an appointment"
                    );

            case RESCHEDULED ->
                    throw new IllegalArgumentException(
                            "Use the update endpoint "
                                    + "to reschedule an appointment"
                    );

            case SCHEDULED ->
                    throw new IllegalArgumentException(
                            "SCHEDULED cannot be assigned manually"
                    );
        }

        Appointment savedAppointment =
                appointmentRepository.saveAndFlush(appointment);

        log.info(
                "Appointment {} updated to status: {}",
                savedAppointment.getId(),
                savedAppointment.getStatus()
        );

        return appointmentMapper.toResponseDTO(
                savedAppointment
        );
    }

    // ========================================
    // CANCEL
    // ========================================

    @Transactional
    public AppointmentResponseDTO cancelAppointment(
            Long id,
            AppointmentCancelDTO request
    ) {
        log.info("Cancelling appointment: {}", id);

        Appointment appointment =
                helper.findAppointmentByIdForUpdate(id);

        AppointmentStatus currentStatus =
                appointment.getStatus();

        if (currentStatus == AppointmentStatus.CANCELLED) {
            throw new ConflictException(
                    "Appointment is already cancelled"
            );
        }

        if (currentStatus != AppointmentStatus.SCHEDULED
                && currentStatus != AppointmentStatus.RESCHEDULED
                && currentStatus != AppointmentStatus.CONFIRMED) {

            throw new ConflictException(
                    "Appointment with status "
                            + currentStatus
                            + " cannot be cancelled"
            );
        }

        validateCancellationReason(request.reason());

        appointment.setStatus(
                AppointmentStatus.CANCELLED
        );

        appointment.setCancellationReason(
                request.reason().trim()
        );

        appointment.setCancelledAt(
                LocalDateTime.now(ZONE_MADRID)
        );

        Appointment savedAppointment =
                appointmentRepository.saveAndFlush(
                        appointment
                );

        log.info(
                "Appointment cancelled successfully: {}",
                savedAppointment.getId()
        );

        return appointmentMapper.toResponseDTO(
                savedAppointment
        );
    }

    // ========================================
    // DELETE
    // ========================================

    @Transactional
    public void deleteAppointment(Long id) {
        Appointment appointment =
                helper.findAppointmentByIdForUpdate(id);

        if (appointment.getStatus()
                != AppointmentStatus.CANCELLED) {
            throw new ConflictException(
                    "Only cancelled appointments can be deleted"
            );
        }

        appointmentRepository.delete(appointment);
    }

    // ========================================
    // COUNT
    // ========================================

    @Transactional(readOnly = true)
    public long countAllAppointments() {
        return appointmentRepository.count();
    }

    @Transactional(readOnly = true)
    public long countAppointmentsByStatus(
            AppointmentStatus status
    ) {
        helper.validateAppoinmentStatus(status);

        return appointmentRepository.countByStatus(status);
    }

    @Transactional(readOnly = true)
    public long countAppointmentsByDoctor(
            Long doctorId
    ) {
        helper.validateDoctorExists(doctorId);

        return appointmentRepository.countByDoctor_Id(
                doctorId
        );
    }

    @Transactional(readOnly = true)
    public AppointmentCountResponse
    countAppointmentsByDoctorAndStatus(
            Long doctorId,
            AppointmentStatus status
    ) {
        helper.validateDoctorExists(doctorId);
        helper.validateAppoinmentStatus(status);

        long count = appointmentRepository
                .countByDoctor_IdAndStatus(
                        doctorId,
                        status
                );

        return new AppointmentCountResponse(
                doctorId,
                status,
                count
        );
    }

    @Transactional(readOnly = true)
    public long countAppointmentsByPatient(
            Long patientId
    ) {
        helper.validatePatientExists(patientId);

        return appointmentRepository.countByPatient_Id(
                patientId
        );
    }

    @Transactional(readOnly = true)
    public long countAppointmentsByDepartmentAndStatus(
            Long departmentId,
            AppointmentStatus status
    ) {
        helper.validateDepartmentExist(departmentId);
        helper.validateAppoinmentStatus(status);

        return appointmentRepository
                .countByDoctor_Department_IdAndStatus(
                        departmentId,
                        status
                );
    }

    // ========================================
    // PRIVATE: STATUS TRANSITIONS
    // ========================================
    private void confirmAppointment(
            Appointment appointment
    ) {
        AppointmentStatus currentStatus =
                appointment.getStatus();

        if (currentStatus != AppointmentStatus.SCHEDULED
                && currentStatus
                != AppointmentStatus.RESCHEDULED) {

            throw new ConflictException(
                    "Only scheduled or rescheduled appointments "
                            + "can be confirmed"
            );
        }

        appointment.setStatus(
                AppointmentStatus.CONFIRMED
        );

        appointment.setConfirmedAt(
                LocalDateTime.now(ZONE_MADRID)
        );
    }

    private void completeAppointment(
            Appointment appointment
    ) {
        if (appointment.getStatus()
                != AppointmentStatus.IN_PROGRESS) {

            throw new ConflictException(
                    "Only appointments in progress "
                            + "can be completed"
            );
        }

        LocalDateTime now =
                LocalDateTime.now(ZONE_MADRID);

        appointment.setStatus(
                AppointmentStatus.COMPLETED
        );

        appointment.setCompletedAt(now);
    }

    // ========================================
    // PRIVATE: SCHEDULE CONFLICTS
    // ========================================

    private void validateScheduleConflicts(
            Long appointmentId,
            Long doctorId,
            Long patientId,
            Long roomId,
            LocalDateTime dateTime
    ) {
        LocalDateTime windowStart =
                dateTime.minusMinutes(
                        APPOINTMENT_DURATION_MINUTES
                );

        LocalDateTime windowEnd =
                dateTime.plusMinutes(
                        APPOINTMENT_DURATION_MINUTES
                );

        boolean doctorConflict;
        boolean patientConflict;
        boolean roomConflict = false;

        if (appointmentId == null) {
            doctorConflict = appointmentRepository
                    .existsDoctorConflict(
                            doctorId,
                            windowStart,
                            windowEnd,
                            BLOCKING_STATUSES
                    );

            patientConflict = appointmentRepository
                    .existsPatientConflict(
                            patientId,
                            windowStart,
                            windowEnd,
                            BLOCKING_STATUSES
                    );

            if (roomId != null) {
                roomConflict = appointmentRepository
                        .existsRoomConflict(
                                roomId,
                                windowStart,
                                windowEnd,
                                BLOCKING_STATUSES
                        );
            }
        } else {
            doctorConflict = appointmentRepository
                    .existsDoctorConflictExcludingAppointment(
                            doctorId,
                            appointmentId,
                            windowStart,
                            windowEnd,
                            BLOCKING_STATUSES
                    );

            patientConflict = appointmentRepository
                    .existsPatientConflictExcludingAppointment(
                            patientId,
                            appointmentId,
                            windowStart,
                            windowEnd,
                            BLOCKING_STATUSES
                    );

            if (roomId != null) {
                roomConflict = appointmentRepository
                        .existsRoomConflictExcludingAppointment(
                                roomId,
                                appointmentId,
                                windowStart,
                                windowEnd,
                                BLOCKING_STATUSES
                        );
            }
        }

        if (doctorConflict) {
            throw new ConflictException(
                    "Doctor already has another appointment "
                            + "during this time"
            );
        }

        if (patientConflict) {
            throw new ConflictException(
                    "Patient already has another appointment "
                            + "during this time"
            );
        }

        if (roomConflict) {
            throw new ConflictException(
                    "Room is already assigned to another "
                            + "appointment during this time"
            );
        }
    }











    // ========================================
    // PRIVATE: VALIDATION
    // ========================================


    private void validateModifiableAppointment(
            Appointment appointment
    ) {
        AppointmentStatus status =
                appointment.getStatus();

        if (status != AppointmentStatus.SCHEDULED
                && status != AppointmentStatus.RESCHEDULED
                && status != AppointmentStatus.CONFIRMED) {

            throw new ConflictException(
                    "Appointment with status "
                            + status
                            + " cannot be modified"
            );
        }
    }


    private void validateAppointmentDate(
            LocalDateTime dateTime
    ) {
        if (dateTime == null) {
            throw new IllegalArgumentException(
                    "Appointment date is required"
            );
        }

        if (!dateTime.isAfter(
                LocalDateTime.now(ZONE_MADRID)
        )) {
            throw new IllegalArgumentException(
                    "Appointment date must be in the future"
            );
        }
    }

    private void validateReason(String reason) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException(
                    "Appointment reason cannot be empty"
            );
        }

        if (reason.trim().length() > 200) {
            throw new IllegalArgumentException(
                    "Appointment reason cannot exceed "
                            + "200 characters"
            );
        }
    }

    private void validateCancellationReason(
            String reason
    ) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException(
                    "Cancellation reason cannot be empty"
            );
        }

        if (reason.trim().length() > 200) {
            throw new IllegalArgumentException(
                    "Cancellation reason cannot exceed "
                            + "200 characters"
            );
        }
    }


    private void startAppointment(
            Appointment appointment
    ) {
        if (appointment.getStatus()
                != AppointmentStatus.CONFIRMED) {

            throw new ConflictException(
                    "Only confirmed appointments "
                            + "can be started"
            );
        }

        LocalDateTime now =
                LocalDateTime.now(ZONE_MADRID);

        if (appointment.getDateTime().isAfter(now)) {
            throw new ConflictException(
                    "A future appointment cannot be started"
            );
        }

        appointment.setStatus(
                AppointmentStatus.IN_PROGRESS
        );
    }




    private void markAppointmentAsNoShow(
            Appointment appointment
    ) {
        AppointmentStatus currentStatus =
                appointment.getStatus();

        if (currentStatus != AppointmentStatus.SCHEDULED
                && currentStatus
                != AppointmentStatus.CONFIRMED
                && currentStatus
                != AppointmentStatus.RESCHEDULED) {

            throw new ConflictException(
                    "Only pending appointments "
                            + "can be marked as no-show"
            );
        }

        LocalDateTime now =
                LocalDateTime.now(ZONE_MADRID);

        if (appointment.getDateTime().isAfter(now)) {
            throw new ConflictException(
                    "A future appointment cannot be "
                            + "marked as no-show"
            );
        }

        appointment.setStatus(
                AppointmentStatus.NO_SHOW
        );
    }


    @Transactional(readOnly = true)
    public Page<AppointmentResponseDTO> getAppointments(
            AppointmentStatus status,
            Long patientId,
            Long doctorId,
            Long roomId,
            Long departmentId,
            LocalDateTime dateTimeFromValue,
            LocalDateTime dateTimeToValue,
            String reason,
            Pageable pageable
    ) {
        helper.validatePageable(
                pageable,
                ALLOWED_SORT_PROPERTIES
        );

        if (status != null) {
            helper.validateAppoinmentStatus(status);
        }

        if (patientId != null) {
            helper.validatePatientExists(patientId);
        }

        if (doctorId != null) {
            helper.validateDoctorExists(doctorId);
        }

        if (roomId != null) {
            helper.validateRoomExists(roomId);
        }

        if (departmentId != null) {
            helper.validateDepartmentExist(departmentId);
        }

        if (dateTimeFromValue != null
                && dateTimeToValue != null) {
            helper.validateDateRange(
                    dateTimeFromValue,
                    dateTimeToValue
            );
        }

        String normalizedReason =
                normalizeOptionalFilter(reason);

        log.info(
                "Fetching appointments with filters: "
                        + "status={}, patientId={}, doctorId={}, "
                        + "roomId={}, departmentId={}, from={}, "
                        + "to={}, reason={}",
                status,
                patientId,
                doctorId,
                roomId,
                departmentId,
                dateTimeFromValue,
                dateTimeToValue,
                normalizedReason
        );

        Specification<Appointment> specification =
                hasStatus(status)
                        .and(belongsToPatient(patientId))
                        .and(belongsToDoctor(doctorId))
                        .and(belongsToRoom(roomId))
                        .and(belongsToDepartment(
                                departmentId
                        ))
                        .and(dateTimeFrom(
                                dateTimeFromValue
                        ))
                        .and(dateTimeTo(
                                dateTimeToValue
                        ))
                        .and(reasonContains(
                                normalizedReason
                        ));

        return appointmentRepository
                .findAll(specification, pageable)
                .map(appointmentMapper::toResponseDTO);
    }

    private String normalizeOptionalFilter(
            String value
    ) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }



}
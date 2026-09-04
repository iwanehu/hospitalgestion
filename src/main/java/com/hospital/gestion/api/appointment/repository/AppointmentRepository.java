package com.hospital.gestion.api.appointment.repository;

import com.hospital.gestion.api.appointment.entity.Appointment;
import com.hospital.gestion.api.common.enums.AppointmentStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository
        extends JpaRepository<Appointment, Long>,
        JpaSpecificationExecutor<Appointment> {

    // ========================================
    // BY DOCTOR
    // ========================================

    List<Appointment> findByDoctor_Id(
            Long doctorId
    );

    Page<Appointment> findByDoctor_Id(
            Long doctorId,
            Pageable pageable
    );

    List<Appointment> findByDoctor_IdAndStatus(
            Long doctorId,
            AppointmentStatus status
    );

    Page<Appointment> findByDoctor_IdAndStatus(
            Long doctorId,
            AppointmentStatus status,
            Pageable pageable
    );

    // ========================================
    // BY PATIENT
    // ========================================

    List<Appointment> findByPatient_Id(
            Long patientId
    );

    Page<Appointment> findByPatient_Id(
            Long patientId,
            Pageable pageable
    );

    List<Appointment> findByPatient_IdAndStatus(
            Long patientId,
            AppointmentStatus status
    );

    Page<Appointment> findByPatient_IdAndStatus(
            Long patientId,
            AppointmentStatus status,
            Pageable pageable
    );

    // ========================================
    // BY ROOM
    // ========================================

    List<Appointment> findByRoom_Id(
            Long roomId
    );

    Page<Appointment> findByRoom_Id(
            Long roomId,
            Pageable pageable
    );

    List<Appointment> findByRoom_IdAndStatus(
            Long roomId,
            AppointmentStatus status
    );

    // ========================================
    // BY STATUS
    // ========================================

    List<Appointment> findByStatus(
            AppointmentStatus status
    );

    Page<Appointment> findByStatus(
            AppointmentStatus status,
            Pageable pageable
    );

    // ========================================
    // BY DATE
    // ========================================

    List<Appointment> findByDateTimeBetween(
            LocalDateTime start,
            LocalDateTime end
    );

    Page<Appointment> findByDateTimeBetween(
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    );

    List<Appointment>
    findByDateTimeAfterAndStatusInOrderByDateTimeAsc(
            LocalDateTime dateTime,
            Collection<AppointmentStatus> statuses
    );

    Page<Appointment>
    findByDateTimeAfterAndStatusIn(
            LocalDateTime dateTime,
            Collection<AppointmentStatus> statuses,
            Pageable pageable
    );

    // ========================================
    // BY DOCTOR AND DATE RANGE
    // ========================================

    List<Appointment>
    findByDoctor_IdAndDateTimeBetween(
            Long doctorId,
            LocalDateTime start,
            LocalDateTime end
    );

    Page<Appointment>
    findByDoctor_IdAndDateTimeBetween(
            Long doctorId,
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    );

    // ========================================
    // BY PATIENT AND DATE RANGE
    // ========================================

    List<Appointment>
    findByPatient_IdAndDateTimeBetween(
            Long patientId,
            LocalDateTime start,
            LocalDateTime end
    );

    // ========================================
    // BY DEPARTMENT
    // ========================================

    List<Appointment> findByDoctor_Department_Id(
            Long departmentId
    );

    Page<Appointment> findByDoctor_Department_Id(
            Long departmentId,
            Pageable pageable
    );

    List<Appointment>
    findByDoctor_Department_IdAndStatus(
            Long departmentId,
            AppointmentStatus status
    );

    // ========================================
    // DOCTOR SCHEDULE CONFLICT
    // ========================================

    @Query("""
            SELECT CASE WHEN COUNT(appointment) > 0
                        THEN true
                        ELSE false
                   END
            FROM Appointment appointment
            WHERE appointment.doctor.id = :doctorId
              AND appointment.status IN :statuses
              AND appointment.dateTime > :windowStart
              AND appointment.dateTime < :windowEnd
            """)
    boolean existsDoctorConflict(
            @Param("doctorId") Long doctorId,
            @Param("windowStart") LocalDateTime windowStart,
            @Param("windowEnd") LocalDateTime windowEnd,
            @Param("statuses")
            Collection<AppointmentStatus> statuses
    );

    @Query("""
            SELECT CASE WHEN COUNT(appointment) > 0
                        THEN true
                        ELSE false
                   END
            FROM Appointment appointment
            WHERE appointment.doctor.id = :doctorId
              AND appointment.id <> :appointmentId
              AND appointment.status IN :statuses
              AND appointment.dateTime > :windowStart
              AND appointment.dateTime < :windowEnd
            """)
    boolean existsDoctorConflictExcludingAppointment(
            @Param("doctorId") Long doctorId,
            @Param("appointmentId") Long appointmentId,
            @Param("windowStart") LocalDateTime windowStart,
            @Param("windowEnd") LocalDateTime windowEnd,
            @Param("statuses")
            Collection<AppointmentStatus> statuses
    );

    // ========================================
    // PATIENT SCHEDULE CONFLICT
    // ========================================

    @Query("""
            SELECT CASE WHEN COUNT(appointment) > 0
                        THEN true
                        ELSE false
                   END
            FROM Appointment appointment
            WHERE appointment.patient.id = :patientId
              AND appointment.status IN :statuses
              AND appointment.dateTime > :windowStart
              AND appointment.dateTime < :windowEnd
            """)
    boolean existsPatientConflict(
            @Param("patientId") Long patientId,
            @Param("windowStart") LocalDateTime windowStart,
            @Param("windowEnd") LocalDateTime windowEnd,
            @Param("statuses")
            Collection<AppointmentStatus> statuses
    );

    @Query("""
            SELECT CASE WHEN COUNT(appointment) > 0
                        THEN true
                        ELSE false
                   END
            FROM Appointment appointment
            WHERE appointment.patient.id = :patientId
              AND appointment.id <> :appointmentId
              AND appointment.status IN :statuses
              AND appointment.dateTime > :windowStart
              AND appointment.dateTime < :windowEnd
            """)
    boolean existsPatientConflictExcludingAppointment(
            @Param("patientId") Long patientId,
            @Param("appointmentId") Long appointmentId,
            @Param("windowStart") LocalDateTime windowStart,
            @Param("windowEnd") LocalDateTime windowEnd,
            @Param("statuses")
            Collection<AppointmentStatus> statuses
    );

    // ========================================
    // ROOM SCHEDULE CONFLICT
    // ========================================

    @Query("""
            SELECT CASE WHEN COUNT(appointment) > 0
                        THEN true
                        ELSE false
                   END
            FROM Appointment appointment
            WHERE appointment.room.id = :roomId
              AND appointment.status IN :statuses
              AND appointment.dateTime > :windowStart
              AND appointment.dateTime < :windowEnd
            """)
    boolean existsRoomConflict(
            @Param("roomId") Long roomId,
            @Param("windowStart") LocalDateTime windowStart,
            @Param("windowEnd") LocalDateTime windowEnd,
            @Param("statuses")
            Collection<AppointmentStatus> statuses
    );

    @Query("""
            SELECT CASE WHEN COUNT(appointment) > 0
                        THEN true
                        ELSE false
                   END
            FROM Appointment appointment
            WHERE appointment.room.id = :roomId
              AND appointment.id <> :appointmentId
              AND appointment.status IN :statuses
              AND appointment.dateTime > :windowStart
              AND appointment.dateTime < :windowEnd
            """)
    boolean existsRoomConflictExcludingAppointment(
            @Param("roomId") Long roomId,
            @Param("appointmentId") Long appointmentId,
            @Param("windowStart") LocalDateTime windowStart,
            @Param("windowEnd") LocalDateTime windowEnd,
            @Param("statuses")
            Collection<AppointmentStatus> statuses
    );

    // ========================================
    // COUNT
    // ========================================

    long countByStatus(
            AppointmentStatus status
    );

    long countByDoctor_Id(
            Long doctorId
    );

    long countByDoctor_IdAndStatus(
            Long doctorId,
            AppointmentStatus status
    );

    long countByPatient_Id(
            Long patientId
    );

    long countByPatient_IdAndStatus(
            Long patientId,
            AppointmentStatus status
    );

    long countByRoom_IdAndStatus(
            Long roomId,
            AppointmentStatus status
    );

    long countByDoctor_Department_IdAndStatus(
            Long departmentId,
            AppointmentStatus status
    );

    // ========================================
    // PESSIMISTIC LOCK
    // ========================================

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT appointment
            FROM Appointment appointment
            WHERE appointment.id = :id
            """)
    Optional<Appointment> findByIdForUpdate(
            @Param("id") Long id
    );


    boolean existsByIdAndPatient_User_Id(
            Long appointmentId,
            Long userId
    );
}
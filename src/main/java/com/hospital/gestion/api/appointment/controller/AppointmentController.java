package com.hospital.gestion.api.appointment.controller;

import com.hospital.gestion.api.appointment.dto.AppointmentCancelDTO;
import com.hospital.gestion.api.appointment.dto.AppointmentCountResponse;
import com.hospital.gestion.api.appointment.dto.AppointmentRequestDTO;
import com.hospital.gestion.api.appointment.dto.AppointmentResponseDTO;
import com.hospital.gestion.api.appointment.dto.AppointmentStatusUpdateDTO;
import com.hospital.gestion.api.appointment.dto.AppointmentUpdateDTO;
import com.hospital.gestion.api.appointment.service.AppointmentService;
import com.hospital.gestion.api.common.dto.PageResponseDTO;
import com.hospital.gestion.api.common.enums.AppointmentStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize(
        "hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST')"
)
public class AppointmentController {

    private final AppointmentService appointmentService;

    // ========================================
    // CREATE
    // ========================================

    @PostMapping
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'RECEPTIONIST') "
                    + "or (hasRole('PATIENT') "
                    + "and @hospitalAuthorization.ownsPatient("
                    + "#request.patientId(), authentication))"
    )
    public ResponseEntity<AppointmentResponseDTO>
    createAppointment(
            @Valid @RequestBody
            AppointmentRequestDTO request
    ) {
        log.info(
                "REST request to create appointment "
                        + "for patient: {} with doctor: {}",
                request.patientId(),
                request.doctorId()
        );

        AppointmentResponseDTO response =
                appointmentService.createAppointment(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(response);
    }

    // ========================================
    // GET ALL
    // ========================================

    @GetMapping
    public ResponseEntity<List<AppointmentResponseDTO>>
    getAllAppointments() {
        log.info("REST request to get all appointments");

        return ResponseEntity.ok(
                appointmentService.getAllAppointments()
        );
    }
    @GetMapping("/page")
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST')"
    )
    public ResponseEntity<PageResponseDTO<AppointmentResponseDTO>>
    getAppointmentsPaginated(
            @RequestParam(required = false)
            AppointmentStatus status,

            @RequestParam(required = false)
            Long patientId,

            @RequestParam(required = false)
            Long doctorId,

            @RequestParam(required = false)
            Long roomId,

            @RequestParam(required = false)
            Long departmentId,

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE_TIME
            )
            LocalDateTime dateTimeFrom,

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE_TIME
            )
            LocalDateTime dateTimeTo,

            @RequestParam(required = false)
            String reason,

            @PageableDefault(
                    size = 20,
                    sort = "dateTime",
                    direction = Sort.Direction.ASC
            )
            Pageable pageable
    ) {
        log.info(
                "REST request to get appointments "
                        + "with filters and pagination: {}",
                pageable
        );

        Page<AppointmentResponseDTO> result =
                appointmentService.getAppointments(
                        status,
                        patientId,
                        doctorId,
                        roomId,
                        departmentId,
                        dateTimeFrom,
                        dateTimeTo,
                        reason,
                        pageable
                );

        return ResponseEntity.ok(
                PageResponseDTO.from(result)
        );
    }
    // ========================================
    // GET BY ID
    // ========================================

    @GetMapping("/{id}")
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST') "
                    + "or (hasRole('PATIENT') "
                    + "and @hospitalAuthorization.ownsAppointment("
                    + "#id, authentication))"
    )
    public ResponseEntity<AppointmentResponseDTO>
    getAppointmentById(
            @PathVariable("id") Long id
    ) {
        log.info(
                "REST request to get appointment by id: {}",
                id
        );

        return ResponseEntity.ok(
                appointmentService.getAppointmentById(id)
        );
    }
    // ========================================
    // GET BY DOCTOR
    // ========================================

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<AppointmentResponseDTO>>
    getAppointmentsByDoctor(
            @PathVariable("doctorId") Long doctorId
    ) {
        log.info(
                "REST request to get appointments "
                        + "by doctor: {}",
                doctorId
        );

        return ResponseEntity.ok(
                appointmentService.getAppointmentsByDoctor(
                        doctorId
                )
        );
    }

    @GetMapping("/doctor/{doctorId}/page")
    public ResponseEntity<Page<AppointmentResponseDTO>>
    getAppointmentsByDoctorPaginated(
            @PathVariable("doctorId") Long doctorId,
            @PageableDefault(
                    size = 20,
                    sort = "dateTime"
            )
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                appointmentService.getAppointmentsByDoctor(
                        doctorId,
                        pageable
                )
        );
    }

    // ========================================
    // GET BY PATIENT
    // ========================================

    @GetMapping("/patient/{patientId}")
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST') "
                    + "or (hasRole('PATIENT') "
                    + "and @hospitalAuthorization.ownsPatient("
                    + "#patientId, authentication))"
    )
    public ResponseEntity<List<AppointmentResponseDTO>>
    getAppointmentsByPatient(
            @PathVariable("patientId") Long patientId
    ) {
        log.info(
                "REST request to get appointments "
                        + "by patient: {}",
                patientId
        );

        return ResponseEntity.ok(
                appointmentService.getAppointmentsByPatient(
                        patientId
                )
        );
    }

    @GetMapping("/patient/{patientId}/page")
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST') "
                    + "or (hasRole('PATIENT') "
                    + "and @hospitalAuthorization.ownsPatient("
                    + "#patientId, authentication))"
    )
    public ResponseEntity<Page<AppointmentResponseDTO>>
    getAppointmentsByPatientPaginated(
            @PathVariable("patientId") Long patientId,
            @PageableDefault(
                    size = 20,
                    sort = "dateTime"
            )
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                appointmentService.getAppointmentsByPatient(
                        patientId,
                        pageable
                )
        );
    }

    // ========================================
    // GET BY ROOM
    // ========================================

    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<AppointmentResponseDTO>>
    getAppointmentsByRoom(
            @PathVariable("roomId") Long roomId
    ) {
        log.info(
                "REST request to get appointments "
                        + "by room: {}",
                roomId
        );

        return ResponseEntity.ok(
                appointmentService.getAppointmentsByRoom(
                        roomId
                )
        );
    }

    // ========================================
    // GET BY STATUS
    // ========================================

    @GetMapping("/status/{status}")
    public ResponseEntity<List<AppointmentResponseDTO>>
    getAppointmentsByStatus(
            @PathVariable("status") AppointmentStatus status
    ) {
        log.info(
                "REST request to get appointments "
                        + "by status: {}",
                status
        );

        return ResponseEntity.ok(
                appointmentService.getAppointmentsByStatus(
                        status
                )
        );
    }

    @GetMapping("/status/{status}/page")
    public ResponseEntity<Page<AppointmentResponseDTO>>
    getAppointmentsByStatusPaginated(
            @PathVariable("status") AppointmentStatus status,
            @PageableDefault(
                    size = 20,
                    sort = "dateTime"
            )
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                appointmentService.getAppointmentsByStatus(
                        status,
                        pageable
                )
        );
    }

    // ========================================
    // GET BY DATE RANGE
    // ========================================

    @GetMapping("/date-range")
    public ResponseEntity<List<AppointmentResponseDTO>>
    getAppointmentsByDateRange(
            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE_TIME
            )
            LocalDateTime start,

            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE_TIME
            )
            LocalDateTime end
    ) {
        log.info(
                "REST request to get appointments "
                        + "between: {} and {}",
                start,
                end
        );

        return ResponseEntity.ok(
                appointmentService.getAppointmentsByDateRange(
                        start,
                        end
                )
        );
    }

    @GetMapping("/date-range/page")
    public ResponseEntity<Page<AppointmentResponseDTO>>
    getAppointmentsByDateRangePaginated(
            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE_TIME
            )
            LocalDateTime start,

            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE_TIME
            )
            LocalDateTime end,

            @PageableDefault(
                    size = 20,
                    sort = "dateTime"
            )
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                appointmentService.getAppointmentsByDateRange(
                        start,
                        end,
                        pageable
                )
        );
    }

    // ========================================
    // GET UPCOMING
    // ========================================

    @GetMapping("/upcoming")
    public ResponseEntity<List<AppointmentResponseDTO>>
    getUpcomingAppointments() {
        log.info(
                "REST request to get upcoming appointments"
        );

        return ResponseEntity.ok(
                appointmentService.getUpcomingAppointments()
        );
    }

    @GetMapping("/upcoming/page")
    public ResponseEntity<Page<AppointmentResponseDTO>>
    getUpcomingAppointmentsPaginated(
            @PageableDefault(
                    size = 20,
                    sort = "dateTime"
            )
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                appointmentService.getUpcomingAppointments(
                        pageable
                )
        );
    }

    // ========================================
    // GET BY DEPARTMENT
    // ========================================

    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<AppointmentResponseDTO>>
    getAppointmentsByDepartment(
            @PathVariable("departmentId") Long departmentId
    ) {
        log.info(
                "REST request to get appointments "
                        + "by department: {}",
                departmentId
        );

        return ResponseEntity.ok(
                appointmentService.getAppointmentsByDepartment(
                        departmentId
                )
        );
    }

    @GetMapping("/department/{departmentId}/page")
    public ResponseEntity<Page<AppointmentResponseDTO>>
    getAppointmentsByDepartmentPaginated(
            @PathVariable("departmentId") Long departmentId,
            @PageableDefault(
                    size = 20,
                    sort = "dateTime"
            )
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                appointmentService
                        .getAppointmentsByDepartment(
                                departmentId,
                                pageable
                        )
        );
    }

    // ========================================
    // UPDATE / RESCHEDULE
    // ========================================

    @PutMapping("/{id}")
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'RECEPTIONIST') "
                    + "or (hasRole('PATIENT') "
                    + "and @hospitalAuthorization.ownsAppointment("
                    + "#id, authentication))"
    )
    public ResponseEntity<AppointmentResponseDTO>
    updateAppointment(
            @PathVariable("id") Long id,
            @Valid @RequestBody
            AppointmentUpdateDTO request
    ) {
        log.info(
                "REST request to update appointment: {}",
                id
        );

        return ResponseEntity.ok(
                appointmentService.updateAppointment(
                        id,
                        request
                )
        );
    }

    // ========================================
    // UPDATE STATUS
    // ========================================

    @PatchMapping("/{id}/status")
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'DOCTOR') "
                    + "or (hasRole('RECEPTIONIST') "
                    + "and #request.status().name() == 'CONFIRMED')"
    )
    public ResponseEntity<AppointmentResponseDTO>
    updateAppointmentStatus(
            @PathVariable("id") Long id,
            @Valid @RequestBody
            AppointmentStatusUpdateDTO request
    ) {
        log.info(
                "REST request to update status "
                        + "of appointment: {} to: {}",
                id,
                request.status()
        );

        return ResponseEntity.ok(
                appointmentService.updateAppointmentStatus(
                        id,
                        request
                )
        );
    }

    // ========================================
    // CANCEL
    // ========================================

    @PatchMapping("/{id}/cancel")
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'RECEPTIONIST') "
                    + "or (hasRole('PATIENT') "
                    + "and @hospitalAuthorization.ownsAppointment("
                    + "#id, authentication))"
    )
    public ResponseEntity<AppointmentResponseDTO>
    cancelAppointment(
            @PathVariable("id") Long id,
            @Valid @RequestBody
            AppointmentCancelDTO request
    ) {
        log.info(
                "REST request to cancel appointment: {}",
                id
        );

        return ResponseEntity.ok(
                appointmentService.cancelAppointment(
                        id,
                        request
                )
        );
    }

    // ========================================
    // DELETE
    // ========================================

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAppointment(
            @PathVariable("id") Long id
    ) {
        log.info(
                "REST request to delete appointment: {}",
                id
        );

        appointmentService.deleteAppointment(id);

        return ResponseEntity.noContent().build();
    }

    // ========================================
    // COUNT ALL
    // ========================================

    @GetMapping("/count")
    public ResponseEntity<Long> countAllAppointments() {
        return ResponseEntity.ok(
                appointmentService.countAllAppointments()
        );
    }

    // ========================================
    // COUNT BY STATUS
    // ========================================

    @GetMapping("/count/status/{status}")
    public ResponseEntity<Long> countAppointmentsByStatus(
            @PathVariable("status") AppointmentStatus status
    ) {
        return ResponseEntity.ok(
                appointmentService.countAppointmentsByStatus(
                        status
                )
        );
    }

    // ========================================
    // COUNT BY DOCTOR
    // ========================================

    @GetMapping("/count/doctor/{doctorId}")
    public ResponseEntity<Long> countAppointmentsByDoctor(
            @PathVariable("doctorId") Long doctorId
    ) {
        return ResponseEntity.ok(
                appointmentService.countAppointmentsByDoctor(
                        doctorId
                )
        );
    }

    // ========================================
    // COUNT BY DOCTOR AND STATUS
    // ========================================

    @GetMapping(
            "/count/doctor/{doctorId}/status/{status}"
    )
    public ResponseEntity<AppointmentCountResponse>
    countAppointmentsByDoctorAndStatus(
            @PathVariable("doctorId") Long doctorId,
            @PathVariable("status") AppointmentStatus status
    ) {
        return ResponseEntity.ok(
                appointmentService
                        .countAppointmentsByDoctorAndStatus(
                                doctorId,
                                status
                        )
        );
    }

    // ========================================
    // COUNT BY PATIENT
    // ========================================

    @GetMapping("/count/patient/{patientId}")
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST') "
                    + "or (hasRole('PATIENT') "
                    + "and @hospitalAuthorization.ownsPatient("
                    + "#patientId, authentication))"
    )
    public ResponseEntity<Long> countAppointmentsByPatient(
            @PathVariable("patientId") Long patientId
    ) {
        return ResponseEntity.ok(
                appointmentService.countAppointmentsByPatient(
                        patientId
                )
        );
    }

    // ========================================
    // COUNT BY DEPARTMENT AND STATUS
    // ========================================

    @GetMapping(
            "/count/department/{departmentId}/status/{status}"
    )
    public ResponseEntity<Long>
    countAppointmentsByDepartmentAndStatus(
            @PathVariable("departmentId") Long departmentId,
            @PathVariable("status") AppointmentStatus status
    ) {
        return ResponseEntity.ok(
                appointmentService
                        .countAppointmentsByDepartmentAndStatus(
                                departmentId,
                                status
                        )
        );
    }
}
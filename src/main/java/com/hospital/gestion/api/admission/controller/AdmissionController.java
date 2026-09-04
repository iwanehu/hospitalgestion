package com.hospital.gestion.api.admission.controller;

import com.hospital.gestion.api.admission.dto.AdmissionDischargeDTO;
import com.hospital.gestion.api.admission.dto.AdmissionRequestDTO;
import com.hospital.gestion.api.admission.dto.AdmissionResponseDTO;
import com.hospital.gestion.api.admission.dto.AdmissionTransferDTO;
import com.hospital.gestion.api.admission.dto.AdmissionUpdateDTO;
import com.hospital.gestion.api.admission.service.AdmissionService;
import com.hospital.gestion.api.common.dto.PageResponseDTO;
import com.hospital.gestion.api.common.enums.AdmissionStatus;
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
@RequestMapping("/api/admissions")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize(
        "hasAnyRole('ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST')"
)
public class AdmissionController {

    private final AdmissionService admissionService;

    // ========================================
    // CREATE
    // ========================================

    @PostMapping
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST')"
    )
    public ResponseEntity<AdmissionResponseDTO>
    createAdmission(
            @Valid @RequestBody AdmissionRequestDTO request
    ) {
        log.info(
                "REST request to create admission "
                        + "for patient: {} in bed: {}",
                request.patientId(),
                request.bedId()
        );

        AdmissionResponseDTO response =
                admissionService.createAdmission(request);

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
    public ResponseEntity<List<AdmissionResponseDTO>>
    getAllAdmissions() {
        log.info("REST request to get all admissions");

        return ResponseEntity.ok(
                admissionService.getAllAdmissions()
        );
    }

    @GetMapping("/page")
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST')"
    )
    public ResponseEntity<PageResponseDTO<AdmissionResponseDTO>>
    getAdmissionsPaginated(
            @RequestParam(required = false)
            AdmissionStatus status,

            @RequestParam(required = false)
            Long patientId,

            @RequestParam(required = false)
            Long doctorId,

            @RequestParam(required = false)
            Long bedId,

            @RequestParam(required = false)
            Long roomId,

            @RequestParam(required = false)
            Long wardId,

            @RequestParam(required = false)
            Long departmentId,

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE_TIME
            )
            LocalDateTime admittedFrom,

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE_TIME
            )
            LocalDateTime admittedTo,

            @PageableDefault(
                    size = 20,
                    sort = "admittedAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {
        log.info(
                "REST request to get admissions "
                        + "with filters and pagination: {}",
                pageable
        );

        Page<AdmissionResponseDTO> result =
                admissionService.getAdmissions(
                        status,
                        patientId,
                        doctorId,
                        bedId,
                        roomId,
                        wardId,
                        departmentId,
                        admittedFrom,
                        admittedTo,
                        pageable
                );

        return ResponseEntity.ok(
                PageResponseDTO.from(result)
        );
    }
    // ========================================
    // GET BY ID
    // ========================================

    @GetMapping("/{id:[0-9]+}")
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST') "
                    + "or (hasRole('PATIENT') "
                    + "and @hospitalAuthorization.ownsAdmission("
                    + "#id, authentication))"
    )    public ResponseEntity<AdmissionResponseDTO>
    getAdmissionById(
            @PathVariable("id") Long id
    ) {
        log.info(
                "REST request to get admission by id: {}",
                id
        );

        return ResponseEntity.ok(
                admissionService.getAdmissionById(id)
        );
    }

    // ========================================
    // GET ACTIVE ADMISSION BY PATIENT
    // ========================================


    @GetMapping("/patient/{patientId}/active")
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST') "
                    + "or (hasRole('PATIENT') "
                    + "and @hospitalAuthorization.ownsPatient("
                    + "#patientId, authentication))"
    )
    public ResponseEntity<AdmissionResponseDTO>
    getActiveAdmissionByPatient(
            @PathVariable("patientId") Long patientId
    ) {
        log.info(
                "REST request to get active admission "
                        + "for patient: {}",
                patientId
        );

        return ResponseEntity.ok(
                admissionService
                        .getActiveAdmissionByPatient(patientId)
        );
    }

    // ========================================
    // GET BY PATIENT
    // ========================================

    @GetMapping("/patient/{patientId}")
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST') "
                    + "or (hasRole('PATIENT') "
                    + "and @hospitalAuthorization.ownsPatient("
                    + "#patientId, authentication))"
    )
    public ResponseEntity<List<AdmissionResponseDTO>>
    getAdmissionsByPatient(
            @PathVariable("patientId") Long patientId
    ) {
        log.info(
                "REST request to get admissions "
                        + "by patient: {}",
                patientId
        );

        return ResponseEntity.ok(
                admissionService
                        .getAdmissionsByPatient(patientId)
        );
    }

    @GetMapping("/patient/{patientId}/page")
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST') "
                    + "or (hasRole('PATIENT') "
                    + "and @hospitalAuthorization.ownsPatient("
                    + "#patientId, authentication))"
    )
    public ResponseEntity<Page<AdmissionResponseDTO>>
    getAdmissionsByPatientPaginated(
            @PathVariable("patientId") Long patientId,
            @PageableDefault(
                    size = 20,
                    sort = "admittedAt"
            )
            Pageable pageable
    ) {
        log.info(
                "REST request to get admissions "
                        + "by patient: {} with pagination",
                patientId
        );

        return ResponseEntity.ok(
                admissionService.getAdmissionsByPatient(
                        patientId,
                        pageable
                )
        );
    }

    // ========================================
    // GET BY STATUS
    // ========================================

    @GetMapping("/status/{status}")
    public ResponseEntity<List<AdmissionResponseDTO>>
    getAdmissionsByStatus(
            @PathVariable("status") AdmissionStatus status
    ) {
        log.info(
                "REST request to get admissions "
                        + "by status: {}",
                status
        );

        return ResponseEntity.ok(
                admissionService.getAdmissionsByStatus(status)
        );
    }

    @GetMapping("/status/{status}/page")
    public ResponseEntity<Page<AdmissionResponseDTO>>
    getAdmissionsByStatusPaginated(
            @PathVariable("status") AdmissionStatus status,
            @PageableDefault(
                    size = 20,
                    sort = "admittedAt"
            )
            Pageable pageable
    ) {
        log.info(
                "REST request to get admissions by status: {} "
                        + "with pagination",
                status
        );

        return ResponseEntity.ok(
                admissionService.getAdmissionsByStatus(
                        status,
                        pageable
                )
        );
    }

    // ========================================
    // GET BY DOCTOR
    // ========================================

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<AdmissionResponseDTO>>
    getAdmissionsByDoctor(
            @PathVariable("doctorId") Long doctorId
    ) {
        log.info(
                "REST request to get admissions "
                        + "by doctor: {}",
                doctorId
        );

        return ResponseEntity.ok(
                admissionService.getAdmissionsByDoctor(
                        doctorId
                )
        );
    }

    @GetMapping("/doctor/{doctorId}/page")
    public ResponseEntity<Page<AdmissionResponseDTO>>
    getAdmissionsByDoctorPaginated(
            @PathVariable("doctorId") Long doctorId,
            @PageableDefault(
                    size = 20,
                    sort = "admittedAt"
            )
            Pageable pageable
    ) {
        log.info(
                "REST request to get admissions "
                        + "by doctor: {} with pagination",
                doctorId
        );

        return ResponseEntity.ok(
                admissionService.getAdmissionsByDoctor(
                        doctorId,
                        pageable
                )
        );
    }

    // ========================================
    // GET BY BED
    // ========================================

    @GetMapping("/bed/{bedId}")
    public ResponseEntity<List<AdmissionResponseDTO>>
    getAdmissionsByBed(
            @PathVariable("bedId") Long bedId
    ) {
        log.info(
                "REST request to get admissions by bed: {}",
                bedId
        );

        return ResponseEntity.ok(
                admissionService.getAdmissionsByBed(bedId)
        );
    }

    // ========================================
    // GET BY ROOM
    // ========================================

    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<AdmissionResponseDTO>>
    getAdmissionsByRoom(
            @PathVariable("roomId") Long roomId
    ) {
        log.info(
                "REST request to get admissions by room: {}",
                roomId
        );

        return ResponseEntity.ok(
                admissionService.getAdmissionsByRoom(roomId)
        );
    }

    // ========================================
    // GET BY WARD
    // ========================================

    @GetMapping("/ward/{wardId}")
    public ResponseEntity<List<AdmissionResponseDTO>>
    getAdmissionsByWard(
            @PathVariable("wardId") Long wardId
    ) {
        log.info(
                "REST request to get admissions by ward: {}",
                wardId
        );

        return ResponseEntity.ok(
                admissionService.getAdmissionsByWard(wardId)
        );
    }

    // ========================================
    // GET BY DEPARTMENT
    // ========================================

    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<AdmissionResponseDTO>>
    getAdmissionsByDepartment(
            @PathVariable("departmentId") Long departmentId
    ) {
        log.info(
                "REST request to get admissions "
                        + "by department: {}",
                departmentId
        );

        return ResponseEntity.ok(
                admissionService.getAdmissionsByDepartment(
                        departmentId
                )
        );
    }

    // ========================================
    // GET BY DATE RANGE
    // ========================================

    @GetMapping("/date-range")
    public ResponseEntity<List<AdmissionResponseDTO>>
    getAdmissionsByDateRange(
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
                "REST request to get admissions "
                        + "between: {} and {}",
                start,
                end
        );

        return ResponseEntity.ok(
                admissionService.getAdmissionsByDateRange(
                        start,
                        end
                )
        );
    }

    @GetMapping("/date-range/page")
    public ResponseEntity<Page<AdmissionResponseDTO>>
    getAdmissionsByDateRangePaginated(
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
                    sort = "admittedAt"
            )
            Pageable pageable
    ) {
        log.info(
                "REST request to get admissions "
                        + "between: {} and {} with pagination",
                start,
                end
        );

        return ResponseEntity.ok(
                admissionService.getAdmissionsByDateRange(
                        start,
                        end,
                        pageable
                )
        );
    }

    // ========================================
    // UPDATE
    // ========================================

    @PutMapping("/{id:[0-9]+}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<AdmissionResponseDTO>
    updateAdmission(
            @PathVariable("id") Long id,
            @Valid @RequestBody AdmissionUpdateDTO request
    ) {
        log.info(
                "REST request to update admission: {}",
                id
        );

        return ResponseEntity.ok(
                admissionService.updateAdmission(
                        id,
                        request
                )
        );
    }

    // ========================================
    // DISCHARGE
    // ACTIVE -> DISCHARGED
    // BED: OCCUPIED -> CLEANING
    // ========================================

    @PatchMapping("/{id:[0-9]+}/discharge")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<AdmissionResponseDTO>
    dischargeAdmission(
            @PathVariable("id") Long id,
            @Valid @RequestBody
            AdmissionDischargeDTO request
    ) {
        log.info(
                "REST request to discharge admission: {}",
                id
        );

        return ResponseEntity.ok(
                admissionService.dischargeAdmission(
                        id,
                        request
                )
        );
    }

    // ========================================
    // TRANSFER
    // ========================================

    @PostMapping("/{id:[0-9]+}/transfer")
    @PreAuthorize("hasAnyRole('ADMIN', 'NURSE')")
    public ResponseEntity<AdmissionResponseDTO>
    transferAdmission(
            @PathVariable("id") Long id,
            @Valid @RequestBody
            AdmissionTransferDTO request
    ) {
        log.info(
                "REST request to transfer admission: {} "
                        + "to bed: {}",
                id,
                request.newBedId()
        );

        AdmissionResponseDTO response =
                admissionService.transferAdmission(
                        id,
                        request
                );

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/admissions/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(response);
    }

    // ========================================
    // CANCEL
    // ACTIVE -> CANCELLED
    // BED: OCCUPIED -> CLEANING
    // ========================================

    @PatchMapping("/{id:[0-9]+}/cancel")
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'RECEPTIONIST')"
    )
    public ResponseEntity<AdmissionResponseDTO>
    cancelAdmission(
            @PathVariable("id") Long id
    ) {
        log.info(
                "REST request to cancel admission: {}",
                id
        );

        return ResponseEntity.ok(
                admissionService.cancelAdmission(id)
        );
    }

    // ========================================
    // DELETE
    // ========================================

    @DeleteMapping("/{id:[0-9]+}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAdmission(
            @PathVariable("id") Long id
    ) {
        log.info(
                "REST request to delete admission: {}",
                id
        );

        admissionService.deleteAdmission(id);

        return ResponseEntity.noContent().build();
    }

    // ========================================
    // EXISTS: ACTIVE ADMISSION BY PATIENT
    // ========================================

    @GetMapping("/exists/patient/{patientId}/active")
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST') "
                    + "or (hasRole('PATIENT') "
                    + "and @hospitalAuthorization.ownsPatient("
                    + "#patientId, authentication))"
    )
    public ResponseEntity<Boolean>
    patientHasActiveAdmission(
            @PathVariable("patientId") Long patientId
    ) {
        return ResponseEntity.ok(
                admissionService.patientHasActiveAdmission(
                        patientId
                )
        );
    }

    // ========================================
    // EXISTS: ACTIVE ADMISSION BY BED
    // ========================================

    @GetMapping("/exists/bed/{bedId}/active")
    public ResponseEntity<Boolean> bedHasActiveAdmission(
            @PathVariable("bedId") Long bedId
    ) {
        return ResponseEntity.ok(
                admissionService.bedHasActiveAdmission(bedId)
        );
    }

    // ========================================
    // COUNT ALL
    // ========================================

    @GetMapping("/count")
    public ResponseEntity<Long> countAllAdmissions() {
        return ResponseEntity.ok(
                admissionService.countAllAdmissions()
        );
    }

    // ========================================
    // COUNT BY STATUS
    // ========================================

    @GetMapping("/count/status/{status}")
    public ResponseEntity<Long> countAdmissionsByStatus(
            @PathVariable("status") AdmissionStatus status
    ) {
        return ResponseEntity.ok(
                admissionService.countAdmissionsByStatus(
                        status
                )
        );
    }

    // ========================================
    // COUNT BY PATIENT
    // ========================================

    @GetMapping("/count/patient/{patientId}")
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST') "
                    + "or (hasRole('PATIENT') "
                    + "and @hospitalAuthorization.ownsPatient("
                    + "#patientId, authentication))"
    )
    public ResponseEntity<Long> countAdmissionsByPatient(
            @PathVariable("patientId") Long patientId
    ) {
        return ResponseEntity.ok(
                admissionService.countAdmissionsByPatient(
                        patientId
                )
        );
    }

    // ========================================
    // COUNT BY DOCTOR
    // ========================================

    @GetMapping("/count/doctor/{doctorId}")
    public ResponseEntity<Long> countAdmissionsByDoctor(
            @PathVariable("doctorId") Long doctorId
    ) {
        return ResponseEntity.ok(
                admissionService.countAdmissionsByDoctor(
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
    public ResponseEntity<Long>
    countAdmissionsByDoctorAndStatus(
            @PathVariable("doctorId") Long doctorId,
            @PathVariable("status") AdmissionStatus status
    ) {
        return ResponseEntity.ok(
                admissionService
                        .countAdmissionsByDoctorAndStatus(
                                doctorId,
                                status
                        )
        );
    }

    // ========================================
    // COUNT BY ROOM AND STATUS
    // ========================================

    @GetMapping(
            "/count/room/{roomId}/status/{status}"
    )
    public ResponseEntity<Long>
    countAdmissionsByRoomAndStatus(
            @PathVariable("roomId") Long roomId,
            @PathVariable("status") AdmissionStatus status
    ) {
        return ResponseEntity.ok(
                admissionService
                        .countAdmissionsByRoomAndStatus(
                                roomId,
                                status
                        )
        );
    }

    // ========================================
    // COUNT BY WARD AND STATUS
    // ========================================

    @GetMapping(
            "/count/ward/{wardId}/status/{status}"
    )
    public ResponseEntity<Long>
    countAdmissionsByWardAndStatus(
            @PathVariable Long wardId,
            @PathVariable AdmissionStatus status
    ) {
        return ResponseEntity.ok(
                admissionService
                        .countAdmissionsByWardAndStatus(
                                wardId,
                                status
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
    countAdmissionsByDepartmentAndStatus(
            @PathVariable("departmentId") Long departmentId,
            @PathVariable("status") AdmissionStatus status
    ) {
        return ResponseEntity.ok(
                admissionService
                        .countAdmissionsByDepartmentAndStatus(
                                departmentId,
                                status
                        )
        );
    }
}
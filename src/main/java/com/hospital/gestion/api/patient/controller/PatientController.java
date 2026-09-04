package com.hospital.gestion.api.patient.controller;

import com.hospital.gestion.api.common.dto.PageResponseDTO;
import com.hospital.gestion.api.common.enums.BloodType;
import com.hospital.gestion.api.patient.dto.PatientRequestDTO;
import com.hospital.gestion.api.patient.dto.PatientResponseDTO;
import com.hospital.gestion.api.patient.dto.PatientUpdateDTO;
import com.hospital.gestion.api.patient.service.PatientService;
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
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize(
        "hasAnyRole('ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST')"
)
public class PatientController {

    private final PatientService patientService;

    // ========================================
    // CREATE
    // ========================================

    @PostMapping
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'RECEPTIONIST')"
    )
    public ResponseEntity<PatientResponseDTO> createPatient(
            @Valid @RequestBody PatientRequestDTO request
    ) {
        log.info(
                "REST request to create patient "
                        + "for user: {}",
                request.userId()
        );

        PatientResponseDTO response =
                patientService.createPatient(request);

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
    public ResponseEntity<List<PatientResponseDTO>>
    getAllPatients() {
        log.info("REST request to get all patients");

        return ResponseEntity.ok(
                patientService.getAllPatients()
        );
    }
    @GetMapping("/page")
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST')"
    )
    public ResponseEntity<PageResponseDTO<PatientResponseDTO>>
    getPatientsPaginated(
            @RequestParam(required = false)
            String text,

            @RequestParam(required = false)
            BloodType bloodType,

            @RequestParam(required = false)
            Boolean hasHealthInsurance,

            @RequestParam(required = false)
            String insuranceProvider,

            @RequestParam(required = false)
            Boolean isActive,

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate birthDateFrom,

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate birthDateTo,

            @PageableDefault(
                    size = 20,
                    sort = "user.lastName",
                    direction = Sort.Direction.ASC
            )
            Pageable pageable
    ) {
        log.info(
                "REST request to get patients "
                        + "with filters and pagination: {}",
                pageable
        );

        Page<PatientResponseDTO> result =
                patientService.getPatients(
                        text,
                        bloodType,
                        hasHealthInsurance,
                        insuranceProvider,
                        isActive,
                        birthDateFrom,
                        birthDateTo,
                        pageable
                );

        return ResponseEntity.ok(
                PageResponseDTO.from(result)
        );
    }
    // ========================================
    // GET ORDERED
    // ========================================

    @GetMapping("/ordered")
    public ResponseEntity<List<PatientResponseDTO>>
    getAllPatientsOrdered() {
        log.info(
                "REST request to get patients ordered by name"
        );

        return ResponseEntity.ok(
                patientService.getAllPatientsOrdered()
        );
    }

    @GetMapping("/active/ordered")
    public ResponseEntity<List<PatientResponseDTO>>
    getActivePatientsOrdered() {
        log.info(
                "REST request to get active patients ordered"
        );

        return ResponseEntity.ok(
                patientService.getActivePatientsOrdered()
        );
    }

    // ========================================
    // GET BY ID
    // ========================================

    @GetMapping("/{id:[0-9]+}")
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST') "
                    + "or (hasRole('PATIENT') "
                    + "and @hospitalAuthorization.ownsPatient("
                    + "#id, authentication))"
    )
    public ResponseEntity<PatientResponseDTO> getPatientById(
            @PathVariable("id") Long id
    ) {
        log.info(
                "REST request to get patient by id: {}",
                id
        );

        return ResponseEntity.ok(
                patientService.getPatientById(id)
        );
    }

    // ========================================
    // GET BY USER ID
    // ========================================

    @GetMapping("/user/{userId}")
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST') "
                    + "or (hasRole('PATIENT') "
                    + "and @hospitalAuthorization.isCurrentUser("
                    + "#userId, authentication))"
    )
    public ResponseEntity<PatientResponseDTO>
    getPatientByUserId(
            @PathVariable("userId") Long userId
    ) {
        log.info(
                "REST request to get patient by user: {}",
                userId
        );

        return ResponseEntity.ok(
                patientService.getPatientByUserId(userId)
        );
    }

    // ========================================
    // GET BY EMAIL
    // ========================================

    @GetMapping("/email")
    public ResponseEntity<PatientResponseDTO>
    getPatientByEmail(
            @RequestParam String email
    ) {
        log.info(
                "REST request to get patient by email: {}",
                email
        );

        return ResponseEntity.ok(
                patientService.getPatientByEmail(email)
        );
    }

    // ========================================
    // GET BY DOCUMENT
    // ========================================

    @GetMapping("/document/{documentId}")
    public ResponseEntity<PatientResponseDTO>
    getPatientByDocumentId(
            @PathVariable("documentId") String documentId
    ) {
        log.info(
                "REST request to get patient by document: {}",
                documentId
        );

        return ResponseEntity.ok(
                patientService.getPatientByDocumentId(
                        documentId
                )
        );
    }

    // ========================================
    // GET BY BLOOD TYPE
    // ========================================

    @GetMapping("/blood-type/{bloodType}")
    public ResponseEntity<List<PatientResponseDTO>>
    getPatientsByBloodType(
            @PathVariable("bloodType") BloodType bloodType
    ) {
        log.info(
                "REST request to get patients "
                        + "by blood type: {}",
                bloodType
        );

        return ResponseEntity.ok(
                patientService.getPatientsByBloodType(
                        bloodType
                )
        );
    }

    @GetMapping("/blood-type/{bloodType}/page")
    public ResponseEntity<Page<PatientResponseDTO>>
    getPatientsByBloodTypePaginated(
            @PathVariable("bloodType") BloodType bloodType,
            @PageableDefault(
                    size = 20,
                    sort = "user.lastName"
            )
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                patientService.getPatientsByBloodType(
                        bloodType,
                        pageable
                )
        );
    }

    // ========================================
    // GET BY INSURANCE STATUS
    // ========================================

    @GetMapping("/insurance")
    public ResponseEntity<List<PatientResponseDTO>>
    getPatientsByInsuranceStatus(
            @RequestParam Boolean hasHealthInsurance
    ) {
        log.info(
                "REST request to get patients "
                        + "by insurance status: {}",
                hasHealthInsurance
        );

        return ResponseEntity.ok(
                patientService.getPatientsByInsuranceStatus(
                        hasHealthInsurance
                )
        );
    }

    @GetMapping("/insurance/page")
    public ResponseEntity<Page<PatientResponseDTO>>
    getPatientsByInsuranceStatusPaginated(
            @RequestParam Boolean hasHealthInsurance,
            @PageableDefault(
                    size = 20,
                    sort = "user.lastName"
            )
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                patientService.getPatientsByInsuranceStatus(
                        hasHealthInsurance,
                        pageable
                )
        );
    }

    // ========================================
    // SEARCH BY INSURANCE PROVIDER
    // ========================================

    @GetMapping("/search/insurance-provider")
    public ResponseEntity<List<PatientResponseDTO>>
    searchPatientsByInsuranceProvider(
            @RequestParam String provider
    ) {
        log.info(
                "REST request to search patients "
                        + "by insurance provider: {}",
                provider
        );

        return ResponseEntity.ok(
                patientService
                        .searchPatientsByInsuranceProvider(
                                provider
                        )
        );
    }

    // ========================================
    // GET BY ACTIVE STATUS
    // ========================================

    @GetMapping("/status")
    public ResponseEntity<List<PatientResponseDTO>>
    getPatientsByActiveStatus(
            @RequestParam Boolean isActive
    ) {
        log.info(
                "REST request to get patients "
                        + "by active status: {}",
                isActive
        );

        return ResponseEntity.ok(
                patientService.getPatientsByActiveStatus(
                        isActive
                )
        );
    }

    @GetMapping("/status/page")
    public ResponseEntity<Page<PatientResponseDTO>>
    getPatientsByActiveStatusPaginated(
            @RequestParam Boolean isActive,
            @PageableDefault(
                    size = 20,
                    sort = "user.lastName"
            )
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                patientService.getPatientsByActiveStatus(
                        isActive,
                        pageable
                )
        );
    }

    // ========================================
    // BLOOD TYPE AND ACTIVE STATUS
    // ========================================

    @GetMapping("/blood-type/{bloodType}/status")
    public ResponseEntity<List<PatientResponseDTO>>
    getPatientsByBloodTypeAndActiveStatus(
            @PathVariable("bloodType") BloodType bloodType,
            @RequestParam Boolean isActive
    ) {
        log.info(
                "REST request to get patients "
                        + "by blood type: {} and status: {}",
                bloodType,
                isActive
        );

        return ResponseEntity.ok(
                patientService
                        .getPatientsByBloodTypeAndActiveStatus(
                                bloodType,
                                isActive
                        )
        );
    }

    @GetMapping(
            "/blood-type/{bloodType}/status/page"
    )
    public ResponseEntity<Page<PatientResponseDTO>>
    getPatientsByBloodTypeAndActiveStatusPaginated(
            @PathVariable("bloodType") BloodType bloodType,
            @RequestParam Boolean isActive,
            @PageableDefault(
                    size = 20,
                    sort = "user.lastName"
            )
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                patientService
                        .getPatientsByBloodTypeAndActiveStatus(
                                bloodType,
                                isActive,
                                pageable
                        )
        );
    }

    // ========================================
    // GET BY BIRTH DATE RANGE
    // ========================================

    @GetMapping("/birth-date-range")
    public ResponseEntity<List<PatientResponseDTO>>
    getPatientsByBirthDateRange(
            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate start,

            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate end
    ) {
        log.info(
                "REST request to get patients born "
                        + "between: {} and {}",
                start,
                end
        );

        return ResponseEntity.ok(
                patientService.getPatientsByBirthDateRange(
                        start,
                        end
                )
        );
    }

    @GetMapping("/birth-date-range/page")
    public ResponseEntity<Page<PatientResponseDTO>>
    getPatientsByBirthDateRangePaginated(
            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate start,

            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate end,

            @PageableDefault(
                    size = 20,
                    sort = "birthDate"
            )
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                patientService.getPatientsByBirthDateRange(
                        start,
                        end,
                        pageable
                )
        );
    }

    // ========================================
    // SEARCH
    // ========================================

    @GetMapping("/search")
    public ResponseEntity<List<PatientResponseDTO>>
    searchPatients(
            @RequestParam String text
    ) {
        log.info(
                "REST request to search patients: {}",
                text
        );

        return ResponseEntity.ok(
                patientService.searchPatients(text)
        );
    }

    @GetMapping("/search/page")
    public ResponseEntity<Page<PatientResponseDTO>>
    searchPatientsPaginated(
            @RequestParam String text,
            @PageableDefault(
                    size = 20,
                    sort = "user.lastName"
            )
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                patientService.searchPatients(
                        text,
                        pageable
                )
        );
    }

    // ========================================
    // UPDATE PARTIALLY
    // ========================================

    @PatchMapping("/{id:[0-9]+}")
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST') "
                    + "or (hasRole('PATIENT') "
                    + "and @hospitalAuthorization.ownsPatient("
                    + "#id, authentication))"
    )
    public ResponseEntity<PatientResponseDTO>
    updatePatient(
            @PathVariable("id") Long id,
            @Valid @RequestBody PatientUpdateDTO request
    ) {
        log.info(
                "REST request to update patient: {}",
                id
        );

        return ResponseEntity.ok(
                patientService.updatePatient(id, request)
        );
    }

    // ========================================
    // DELETE
    // ========================================

    @DeleteMapping("/{id:[0-9]+}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePatient(
            @PathVariable("id") Long id
    ) {
        log.info(
                "REST request to delete patient: {}",
                id
        );

        patientService.deletePatient(id);

        return ResponseEntity.noContent().build();
    }

    // ========================================
    // EXISTS BY USER
    // ========================================

    @GetMapping("/exists/user/{userId}")
    public ResponseEntity<Boolean> existsByUserId(
            @PathVariable("userId") Long userId
    ) {
        return ResponseEntity.ok(
                patientService.existsByUserId(userId)
        );
    }

    // ========================================
    // EXISTS BY EMAIL
    // ========================================

    @GetMapping("/exists/email")
    public ResponseEntity<Boolean> existsByEmail(
            @RequestParam String email
    ) {
        return ResponseEntity.ok(
                patientService.existsByEmail(email)
        );
    }

    // ========================================
    // EXISTS BY DOCUMENT
    // ========================================

    @GetMapping("/exists/document/{documentId}")
    public ResponseEntity<Boolean> existsByDocumentId(
            @PathVariable("documentId") String documentId
    ) {
        return ResponseEntity.ok(
                patientService.existsByDocumentId(
                        documentId
                )
        );
    }

    // ========================================
    // COUNT ALL
    // ========================================

    @GetMapping("/count")
    public ResponseEntity<Long> countAllPatients() {
        return ResponseEntity.ok(
                patientService.countAllPatients()
        );
    }

    // ========================================
    // COUNT BY BLOOD TYPE
    // ========================================

    @GetMapping("/count/blood-type/{bloodType}")
    public ResponseEntity<Long> countPatientsByBloodType(
            @PathVariable("bloodType") BloodType bloodType
    ) {
        return ResponseEntity.ok(
                patientService.countPatientsByBloodType(
                        bloodType
                )
        );
    }

    // ========================================
    // COUNT BY INSURANCE STATUS
    // ========================================

    @GetMapping("/count/insurance")
    public ResponseEntity<Long>
    countPatientsByInsuranceStatus(
            @RequestParam Boolean hasHealthInsurance
    ) {
        return ResponseEntity.ok(
                patientService
                        .countPatientsByInsuranceStatus(
                                hasHealthInsurance
                        )
        );
    }

    // ========================================
    // COUNT BY ACTIVE STATUS
    // ========================================

    @GetMapping("/count/status")
    public ResponseEntity<Long>
    countPatientsByActiveStatus(
            @RequestParam Boolean isActive
    ) {
        return ResponseEntity.ok(
                patientService.countPatientsByActiveStatus(
                        isActive
                )
        );
    }

    // ========================================
    // COUNT BY BLOOD TYPE AND STATUS
    // ========================================

    @GetMapping(
            "/count/blood-type/{bloodType}/status"
    )
    public ResponseEntity<Long>
    countPatientsByBloodTypeAndActiveStatus(
            @PathVariable("bloodType") BloodType bloodType,
            @RequestParam Boolean isActive
    ) {
        return ResponseEntity.ok(
                patientService
                        .countPatientsByBloodTypeAndActiveStatus(
                                bloodType,
                                isActive
                        )
        );
    }
}
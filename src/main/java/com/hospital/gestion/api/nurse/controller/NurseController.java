package com.hospital.gestion.api.nurse.controller;

import com.hospital.gestion.api.common.dto.PageResponseDTO;
import com.hospital.gestion.api.common.enums.NurseSpecialty;
import com.hospital.gestion.api.common.enums.ShiftType;
import com.hospital.gestion.api.nurse.dto.NurseRequestDTO;
import com.hospital.gestion.api.nurse.dto.NurseResponseDTO;
import com.hospital.gestion.api.nurse.dto.NurseStatsResponse;
import com.hospital.gestion.api.nurse.dto.NurseUpdateDTO;
import com.hospital.gestion.api.nurse.service.NurseService;
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
@RequestMapping("/api/nurses")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize(
        "hasAnyRole('ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST')"
)
public class NurseController {

    private final NurseService nurseService;

    // ============================================================
    // CREATE
    // ============================================================

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NurseResponseDTO> createNurse(
            @Valid @RequestBody NurseRequestDTO request
    ) {
        log.info(
                "REST request to create nurse for user: {}",
                request.userId()
        );

        NurseResponseDTO response =
                nurseService.createNurse(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(response);
    }

    // ============================================================
    // GET ALL
    // ============================================================

    @GetMapping
    public ResponseEntity<List<NurseResponseDTO>>
    getAllNurses() {
        return ResponseEntity.ok(
                nurseService.getAllNurses()
        );
    }

    @GetMapping("/page")
    public ResponseEntity<PageResponseDTO<NurseResponseDTO>>
    getNursesPaginated(
            @RequestParam(required = false)
            String text,

            @RequestParam(required = false)
            Long departmentId,

            @RequestParam(required = false)
            NurseSpecialty specialty,

            @RequestParam(required = false)
            ShiftType shiftType,

            @RequestParam(required = false)
            Boolean isActive,

            @RequestParam(required = false)
            Boolean isChargeNurse,

            @RequestParam(required = false)
            Integer minimumExperience,

            @RequestParam(required = false)
            Integer maximumExperience,

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate hiredFrom,

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate hiredTo,

            @PageableDefault(
                    size = 20,
                    sort = "user.lastName",
                    direction = Sort.Direction.ASC
            )
            Pageable pageable
    ) {
        Page<NurseResponseDTO> result =
                nurseService.getNurses(
                        text,
                        departmentId,
                        specialty,
                        shiftType,
                        isActive,
                        isChargeNurse,
                        minimumExperience,
                        maximumExperience,
                        hiredFrom,
                        hiredTo,
                        pageable
                );

        return ResponseEntity.ok(
                PageResponseDTO.from(result)
        );
    }

    @GetMapping("/ordered")
    public ResponseEntity<List<NurseResponseDTO>>
    getAllNursesOrdered() {
        return ResponseEntity.ok(
                nurseService.getAllNursesOrdered()
        );
    }

    @GetMapping("/active/ordered")
    public ResponseEntity<List<NurseResponseDTO>>
    getActiveNursesOrdered() {
        return ResponseEntity.ok(
                nurseService.getActiveNursesOrdered()
        );
    }

    // ============================================================
    // GET BY ID
    // ============================================================

    @GetMapping("/{id}")
    public ResponseEntity<NurseResponseDTO> getNurseById(
            @PathVariable("id") Long id
    ) {
        return ResponseEntity.ok(
                nurseService.getNurseById(id)
        );
    }

    // ============================================================
    // GET BY USER
    // ============================================================

    @GetMapping("/user/{userId}")
    public ResponseEntity<NurseResponseDTO> getNurseByUserId(
            @PathVariable("userId") Long userId
    ) {
        return ResponseEntity.ok(
                nurseService.getNurseByUserId(userId)
        );
    }

    @GetMapping("/email")
    public ResponseEntity<NurseResponseDTO> getNurseByEmail(
            @RequestParam String email
    ) {
        return ResponseEntity.ok(
                nurseService.getNurseByEmail(email)
        );
    }

    @GetMapping("/document/{documentId}")
    public ResponseEntity<NurseResponseDTO>
    getNurseByDocumentId(
            @PathVariable("documentId") String documentId
    ) {
        return ResponseEntity.ok(
                nurseService.getNurseByDocumentId(documentId)
        );
    }

    // ============================================================
    // GET BY LICENSE
    // ============================================================

    @GetMapping("/license/{licenseNumber}")
    public ResponseEntity<NurseResponseDTO> getNurseByLicense(
            @PathVariable("licenseNumber") String licenseNumber
    ) {
        return ResponseEntity.ok(
                nurseService.getNurseByLicense(licenseNumber)
        );
    }

    // ============================================================
    // BY DEPARTMENT
    // ============================================================

    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<NurseResponseDTO>>
    getNursesByDepartment(
            @PathVariable("departmentId") Long departmentId
    ) {
        return ResponseEntity.ok(
                nurseService.getNursesByDepartment(
                        departmentId
                )
        );
    }

    @GetMapping("/department/{departmentId}/page")
    public ResponseEntity<Page<NurseResponseDTO>>
    getNursesByDepartmentPaginated(
            @PathVariable("departmentId") Long departmentId,

            @PageableDefault(
                    size = 20,
                    sort = "user.lastName",
                    direction = Sort.Direction.ASC
            )
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                nurseService.getNursesByDepartment(
                        departmentId,
                        pageable
                )
        );
    }

    // ============================================================
    // BY SPECIALTY
    // ============================================================

    @GetMapping("/specialty/{specialty}")
    public ResponseEntity<List<NurseResponseDTO>>
    getNursesBySpecialty(
            @PathVariable("specialty") NurseSpecialty specialty
    ) {
        return ResponseEntity.ok(
                nurseService.getNursesBySpecialty(specialty)
        );
    }

    @GetMapping("/specialty/{specialty}/page")
    public ResponseEntity<Page<NurseResponseDTO>>
    getNursesBySpecialtyPaginated(
            @PathVariable("specialty") NurseSpecialty specialty,

            @PageableDefault(
                    size = 20,
                    sort = "user.lastName",
                    direction = Sort.Direction.ASC
            )
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                nurseService.getNursesBySpecialty(
                        specialty,
                        pageable
                )
        );
    }

    // ============================================================
    // BY SHIFT
    // ============================================================

    @GetMapping("/shift/{shiftType}")
    public ResponseEntity<List<NurseResponseDTO>>
    getNursesByShift(
            @PathVariable("shiftType") ShiftType shiftType
    ) {
        return ResponseEntity.ok(
                nurseService.getNursesByShift(shiftType)
        );
    }

    @GetMapping("/shift/{shiftType}/page")
    public ResponseEntity<Page<NurseResponseDTO>>
    getNursesByShiftPaginated(
            @PathVariable("shiftType") ShiftType shiftType,

            @PageableDefault(
                    size = 20,
                    sort = "user.lastName",
                    direction = Sort.Direction.ASC
            )
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                nurseService.getNursesByShift(
                        shiftType,
                        pageable
                )
        );
    }

    // ============================================================
    // BY ACTIVE STATUS
    // ============================================================

    @GetMapping("/status")
    public ResponseEntity<List<NurseResponseDTO>>
    getNursesByActiveStatus(
            @RequestParam Boolean isActive
    ) {
        return ResponseEntity.ok(
                nurseService.getNursesByActiveStatus(
                        isActive
                )
        );
    }

    @GetMapping("/status/page")
    public ResponseEntity<Page<NurseResponseDTO>>
    getNursesByActiveStatusPaginated(
            @RequestParam Boolean isActive,

            @PageableDefault(
                    size = 20,
                    sort = "user.lastName",
                    direction = Sort.Direction.ASC
            )
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                nurseService.getNursesByActiveStatus(
                        isActive,
                        pageable
                )
        );
    }

    // ============================================================
    // CHARGE NURSES
    // ============================================================

    @GetMapping("/charge")
    public ResponseEntity<List<NurseResponseDTO>>
    getChargeNurses(
            @RequestParam(
                    defaultValue = "true"
            )
            Boolean isChargeNurse
    ) {
        return ResponseEntity.ok(
                nurseService.getChargeNurses(
                        isChargeNurse
                )
        );
    }

    // ============================================================
    // COMBINED FILTERS
    // ============================================================

    @GetMapping(
            "/department/{departmentId}/specialty/{specialty}"
    )
    public ResponseEntity<List<NurseResponseDTO>>
    getNursesByDepartmentAndSpecialty(
            @PathVariable("departmentId") Long departmentId,
            @PathVariable("specialty") NurseSpecialty specialty
    ) {
        return ResponseEntity.ok(
                nurseService
                        .getNursesByDepartmentAndSpecialty(
                                departmentId,
                                specialty
                        )
        );
    }

    @GetMapping(
            "/department/{departmentId}/shift/{shiftType}"
    )
    public ResponseEntity<List<NurseResponseDTO>>
    getNursesByDepartmentAndShift(
            @PathVariable("departmentId") Long departmentId,
            @PathVariable("shiftType") ShiftType shiftType
    ) {
        return ResponseEntity.ok(
                nurseService.getNursesByDepartmentAndShift(
                        departmentId,
                        shiftType
                )
        );
    }

    @GetMapping(
            "/specialty/{specialty}/shift/{shiftType}"
    )
    public ResponseEntity<List<NurseResponseDTO>>
    getNursesBySpecialtyAndShift(
            @PathVariable("specialty") NurseSpecialty specialty,
            @PathVariable("shiftType") ShiftType shiftType
    ) {
        return ResponseEntity.ok(
                nurseService.getNursesBySpecialtyAndShift(
                        specialty,
                        shiftType
                )
        );
    }

    @GetMapping("/specialty/{specialty}/status")
    public ResponseEntity<List<NurseResponseDTO>>
    getNursesBySpecialtyAndActiveStatus(
            @PathVariable("specialty") NurseSpecialty specialty,
            @RequestParam Boolean isActive
    ) {
        return ResponseEntity.ok(
                nurseService
                        .getNursesBySpecialtyAndActiveStatus(
                                specialty,
                                isActive
                        )
        );
    }

    // ============================================================
    // SEARCH
    // ============================================================

    @GetMapping("/search")
    public ResponseEntity<List<NurseResponseDTO>>
    searchNurses(
            @RequestParam String text
    ) {
        return ResponseEntity.ok(
                nurseService.searchNurses(text)
        );
    }

    @GetMapping("/search/page")
    public ResponseEntity<Page<NurseResponseDTO>>
    searchNursesPaginated(
            @RequestParam String text,

            @PageableDefault(
                    size = 20,
                    sort = "user.lastName",
                    direction = Sort.Direction.ASC
            )
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                nurseService.searchNurses(
                        text,
                        pageable
                )
        );
    }

    // ============================================================
    // UPDATE
    // ============================================================

    @PutMapping("/{id}")
    @PreAuthorize(
            "hasRole('ADMIN') "
                    + "or (hasRole('NURSE') "
                    + "and @hospitalAuthorization.ownsNurse("
                    + "#id, authentication))"
    )
    public ResponseEntity<NurseResponseDTO> updateNurse(
            @PathVariable("id") Long id,
            @Valid @RequestBody NurseUpdateDTO request
    ) {
        log.info(
                "REST request to update nurse: {}",
                id
        );

        return ResponseEntity.ok(
                nurseService.updateNurse(id, request)
        );
    }

    // ============================================================
    // DELETE
    // ============================================================

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteNurse(
            @PathVariable("id") Long id
    ) {
        log.info(
                "REST request to delete nurse: {}",
                id
        );

        nurseService.deleteNurse(id);

        return ResponseEntity.noContent().build();
    }

    // ============================================================
    // EXISTS
    // ============================================================

    @GetMapping("/exists/user/{userId}")
    public ResponseEntity<Boolean> existsByUserId(
            @PathVariable("userId") Long userId
    ) {
        return ResponseEntity.ok(
                nurseService.existsByUserId(userId)
        );
    }

    @GetMapping("/exists/license/{licenseNumber}")
    public ResponseEntity<Boolean> existsByLicense(
            @PathVariable("licenseNumber") String licenseNumber
    ) {
        return ResponseEntity.ok(
                nurseService.existsByLicense(
                        licenseNumber
                )
        );
    }

    // ============================================================
    // COUNT
    // ============================================================

    @GetMapping("/count")
    public ResponseEntity<Long> countAllNurses() {
        return ResponseEntity.ok(
                nurseService.countAllNurses()
        );
    }

    @GetMapping("/count/department/{departmentId}")
    public ResponseEntity<Long> countNursesByDepartment(
            @PathVariable("departmentId") Long departmentId
    ) {
        return ResponseEntity.ok(
                nurseService.countNursesByDepartment(
                        departmentId
                )
        );
    }

    @GetMapping("/count/specialty/{specialty}")
    public ResponseEntity<Long> countNursesBySpecialty(
            @PathVariable("specialty") NurseSpecialty specialty
    ) {
        return ResponseEntity.ok(
                nurseService.countNursesBySpecialty(
                        specialty
                )
        );
    }

    @GetMapping("/count/shift/{shiftType}")
    public ResponseEntity<Long> countNursesByShift(
            @PathVariable("shiftType") ShiftType shiftType
    ) {
        return ResponseEntity.ok(
                nurseService.countNursesByShift(
                        shiftType
                )
        );
    }

    @GetMapping("/count/status")
    public ResponseEntity<Long> countNursesByActiveStatus(
            @RequestParam Boolean isActive
    ) {
        return ResponseEntity.ok(
                nurseService.countNursesByActiveStatus(
                        isActive
                )
        );
    }

    @GetMapping("/count/charge")
    public ResponseEntity<Long> countChargeNurses(
            @RequestParam(
                    defaultValue = "true"
            )
            Boolean isChargeNurse
    ) {
        return ResponseEntity.ok(
                nurseService.countChargeNurses(
                        isChargeNurse
                )
        );
    }

    // ============================================================
    // STATS
    // ============================================================

    @GetMapping("/stats")
    public ResponseEntity<NurseStatsResponse> getNurseStats() {
        return ResponseEntity.ok(
                nurseService.getNurseStats()
        );
    }
}
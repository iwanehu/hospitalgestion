package com.hospital.gestion.api.doctor.controller;

import com.hospital.gestion.api.common.dto.PageResponseDTO;
import com.hospital.gestion.api.common.enums.Specialty;
import com.hospital.gestion.api.doctor.dto.DoctorRequestDTO;
import com.hospital.gestion.api.doctor.dto.DoctorResponseDTO;
import com.hospital.gestion.api.doctor.dto.DoctorUpdateDTO;
import com.hospital.gestion.api.doctor.service.DoctorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("isAuthenticated()")
public class DoctorController {

    private final DoctorService doctorService;

    // ========================================
    // CREATE
    // ========================================

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DoctorResponseDTO> createDoctor(
            @Valid @RequestBody DoctorRequestDTO request
    ) {
        log.info(
                "REST request to create doctor "
                        + "for user: {}",
                request.userId()
        );

        DoctorResponseDTO response =
                doctorService.createDoctor(request);

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
    public ResponseEntity<List<DoctorResponseDTO>>
    getAllDoctors() {
        log.info("REST request to get all doctors");

        return ResponseEntity.ok(
                doctorService.getAllDoctors()
        );
    }

    @GetMapping("/page")
    public ResponseEntity<PageResponseDTO<DoctorResponseDTO>>
    getDoctorsPaginated(
            @RequestParam(required = false)
            String text,

            @RequestParam(required = false)
            Long departmentId,

            @RequestParam(required = false)
            Specialty specialty,

            @RequestParam(required = false)
            Boolean isActive,

            @RequestParam(required = false)
            Integer minimumExperience,

            @RequestParam(required = false)
            Integer maximumExperience,

            @PageableDefault(
                    size = 20,
                    sort = "user.lastName",
                    direction = Sort.Direction.ASC
            )
            Pageable pageable
    ) {
        log.info(
                "REST request to get doctors "
                        + "with filters and pagination: {}",
                pageable
        );

        Page<DoctorResponseDTO> result =
                doctorService.getDoctors(
                        text,
                        departmentId,
                        specialty,
                        isActive,
                        minimumExperience,
                        maximumExperience,
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
    public ResponseEntity<List<DoctorResponseDTO>>
    getAllDoctorsOrdered() {
        log.info(
                "REST request to get doctors ordered by name"
        );

        return ResponseEntity.ok(
                doctorService.getAllDoctorsOrdered()
        );
    }

    @GetMapping("/active/ordered")
    public ResponseEntity<List<DoctorResponseDTO>>
    getActiveDoctorsOrdered() {
        log.info(
                "REST request to get active doctors ordered"
        );

        return ResponseEntity.ok(
                doctorService.getActiveDoctorsOrdered()
        );
    }

    // ========================================
    // GET BY ID
    // ========================================

    @GetMapping("/{id:[0-9]+}")
    public ResponseEntity<DoctorResponseDTO> getDoctorById(
            @PathVariable("id") Long id
    ) {
        log.info(
                "REST request to get doctor by id: {}",
                id
        );

        return ResponseEntity.ok(
                doctorService.getDoctorById(id)
        );
    }

    // ========================================
    // GET BY USER
    // ========================================

    @GetMapping("/user/{userId}")
    public ResponseEntity<DoctorResponseDTO>
    getDoctorByUserId(
            @PathVariable("userId") Long userId
    ) {
        log.info(
                "REST request to get doctor by user: {}",
                userId
        );

        return ResponseEntity.ok(
                doctorService.getDoctorByUserId(userId)
        );
    }

    // ========================================
    // GET BY EMAIL
    // ========================================

    @GetMapping("/email")
    public ResponseEntity<DoctorResponseDTO> getDoctorByEmail(
            @RequestParam String email
    ) {
        log.info(
                "REST request to get doctor by email: {}",
                email
        );

        return ResponseEntity.ok(
                doctorService.getDoctorByEmail(email)
        );
    }

    // ========================================
    // GET BY DOCUMENT
    // ========================================

    @GetMapping("/document/{documentId}")
    public ResponseEntity<DoctorResponseDTO>
    getDoctorByDocumentId(
            @PathVariable("documentId") String documentId
    ) {
        log.info(
                "REST request to get doctor by document: {}",
                documentId
        );

        return ResponseEntity.ok(
                doctorService.getDoctorByDocumentId(
                        documentId
                )
        );
    }

    // ========================================
    // GET BY LICENSE
    // ========================================

    @GetMapping("/license/{license}")
    public ResponseEntity<DoctorResponseDTO>
    getDoctorByLicense(
            @PathVariable("license") String license
    ) {
        log.info(
                "REST request to get doctor by license: {}",
                license
        );

        return ResponseEntity.ok(
                doctorService.getDoctorByLicense(license)
        );
    }

    // ========================================
    // GET BY SPECIALTY
    // ========================================

    @GetMapping("/specialty/{specialty}")
    public ResponseEntity<List<DoctorResponseDTO>>
    getDoctorsBySpecialty(
            @PathVariable("specialty") Specialty specialty
    ) {
        log.info(
                "REST request to get doctors "
                        + "by specialty: {}",
                specialty
        );

        return ResponseEntity.ok(
                doctorService.getDoctorsBySpecialty(
                        specialty
                )
        );
    }

    @GetMapping("/specialty/{specialty}/page")
    public ResponseEntity<Page<DoctorResponseDTO>>
    getDoctorsBySpecialtyPaginated(
            @PathVariable("specialty") Specialty specialty,
            @PageableDefault(
                    size = 20,
                    sort = "user.lastName"
            )
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                doctorService.getDoctorsBySpecialty(
                        specialty,
                        pageable
                )
        );
    }

    // ========================================
    // GET BY DEPARTMENT
    // ========================================

    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<DoctorResponseDTO>>
    getDoctorsByDepartment(
            @PathVariable("departmentId") Long departmentId
    ) {
        log.info(
                "REST request to get doctors "
                        + "by department: {}",
                departmentId
        );

        return ResponseEntity.ok(
                doctorService.getDoctorsByDepartment(
                        departmentId
                )
        );
    }

    @GetMapping("/department/{departmentId}/page")
    public ResponseEntity<Page<DoctorResponseDTO>>
    getDoctorsByDepartmentPaginated(
            @PathVariable("departmentId") Long departmentId,
            @PageableDefault(
                    size = 20,
                    sort = "user.lastName"
            )
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                doctorService.getDoctorsByDepartment(
                        departmentId,
                        pageable
                )
        );
    }

    // ========================================
    // DEPARTMENT AND SPECIALTY
    // ========================================

    @GetMapping(
            "/department/{departmentId}/specialty/{specialty}"
    )
    public ResponseEntity<List<DoctorResponseDTO>>
    getDoctorsByDepartmentAndSpecialty(
            @PathVariable("departmentId") Long departmentId,
            @PathVariable("specialty") Specialty specialty
    ) {
        log.info(
                "REST request to get doctors "
                        + "by department: {} and specialty: {}",
                departmentId,
                specialty
        );

        return ResponseEntity.ok(
                doctorService
                        .getDoctorsByDepartmentAndSpecialty(
                                departmentId,
                                specialty
                        )
        );
    }

    @GetMapping(
            "/department/{departmentId}"
                    + "/specialty/{specialty}/page"
    )
    public ResponseEntity<Page<DoctorResponseDTO>>
    getDoctorsByDepartmentAndSpecialtyPaginated(
            @PathVariable("departmentId") Long departmentId,
            @PathVariable("specialty") Specialty specialty,
            @PageableDefault(
                    size = 20,
                    sort = "user.lastName"
            )
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                doctorService
                        .getDoctorsByDepartmentAndSpecialty(
                                departmentId,
                                specialty,
                                pageable
                        )
        );
    }

    // ========================================
    // GET BY ACTIVE STATUS
    // ========================================

    @GetMapping("/status")
    public ResponseEntity<List<DoctorResponseDTO>>
    getDoctorsByActiveStatus(
            @RequestParam Boolean isActive
    ) {
        log.info(
                "REST request to get doctors "
                        + "by active status: {}",
                isActive
        );

        return ResponseEntity.ok(
                doctorService.getDoctorsByActiveStatus(
                        isActive
                )
        );
    }

    @GetMapping("/status/page")
    public ResponseEntity<Page<DoctorResponseDTO>>
    getDoctorsByActiveStatusPaginated(
            @RequestParam Boolean isActive,
            @PageableDefault(
                    size = 20,
                    sort = "user.lastName"
            )
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                doctorService.getDoctorsByActiveStatus(
                        isActive,
                        pageable
                )
        );
    }

    // ========================================
    // SPECIALTY AND ACTIVE STATUS
    // ========================================

    @GetMapping("/specialty/{specialty}/status")
    public ResponseEntity<List<DoctorResponseDTO>>
    getDoctorsBySpecialtyAndActiveStatus(
            @PathVariable("specialty") Specialty specialty,
            @RequestParam Boolean isActive
    ) {
        log.info(
                "REST request to get doctors "
                        + "by specialty: {} and status: {}",
                specialty,
                isActive
        );

        return ResponseEntity.ok(
                doctorService
                        .getDoctorsBySpecialtyAndActiveStatus(
                                specialty,
                                isActive
                        )
        );
    }

    @GetMapping("/specialty/{specialty}/status/page")
    public ResponseEntity<Page<DoctorResponseDTO>>
    getDoctorsBySpecialtyAndActiveStatusPaginated(
            @PathVariable("specialty") Specialty specialty,
            @RequestParam Boolean isActive,
            @PageableDefault(
                    size = 20,
                    sort = "user.lastName"
            )
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                doctorService
                        .getDoctorsBySpecialtyAndActiveStatus(
                                specialty,
                                isActive,
                                pageable
                        )
        );
    }

    // ========================================
    // DEPARTMENT AND ACTIVE STATUS
    // ========================================

    @GetMapping("/department/{departmentId}/status")
    public ResponseEntity<List<DoctorResponseDTO>>
    getDoctorsByDepartmentAndActiveStatus(
            @PathVariable("departmentId") Long departmentId,
            @RequestParam Boolean isActive
    ) {
        log.info(
                "REST request to get doctors "
                        + "by department: {} and status: {}",
                departmentId,
                isActive
        );

        return ResponseEntity.ok(
                doctorService
                        .getDoctorsByDepartmentAndActiveStatus(
                                departmentId,
                                isActive
                        )
        );
    }

    // ========================================
    // SEARCH
    // ========================================

    @GetMapping("/search")
    public ResponseEntity<List<DoctorResponseDTO>>
    searchDoctors(
            @RequestParam String text
    ) {
        log.info(
                "REST request to search doctors: {}",
                text
        );

        return ResponseEntity.ok(
                doctorService.searchDoctors(text)
        );
    }

    @GetMapping("/search/page")
    public ResponseEntity<Page<DoctorResponseDTO>>
    searchDoctorsPaginated(
            @RequestParam String text,
            @PageableDefault(
                    size = 20,
                    sort = "user.lastName"
            )
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                doctorService.searchDoctors(
                        text,
                        pageable
                )
        );
    }

    // ========================================
    // UPDATE
    // ========================================

    @PutMapping("/{id:[0-9]+}")
    @PreAuthorize(
            "hasRole('ADMIN') "
                    + "or (hasRole('DOCTOR') "
                    + "and @hospitalAuthorization.ownsDoctor("
                    + "#id, authentication))"
    )
    public ResponseEntity<DoctorResponseDTO> updateDoctor(
            @PathVariable("id") Long id,
            @Valid @RequestBody DoctorUpdateDTO request
    ) {
        log.info(
                "REST request to update doctor: {}",
                id
        );

        return ResponseEntity.ok(
                doctorService.updateDoctor(id, request)
        );
    }

    // ========================================
    // DELETE
    // ========================================

    @DeleteMapping("/{id:[0-9]+}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteDoctor(
            @PathVariable("id") Long id
    ) {
        log.info(
                "REST request to delete doctor: {}",
                id
        );

        doctorService.deleteDoctor(id);

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
                doctorService.existsByUserId(userId)
        );
    }

    // ========================================
    // EXISTS BY LICENSE
    // ========================================

    @GetMapping("/exists/license/{license}")
    public ResponseEntity<Boolean> existsByLicense(
            @PathVariable("license") String license
    ) {
        return ResponseEntity.ok(
                doctorService.existsByLicense(license)
        );
    }

    // ========================================
    // COUNT ALL
    // ========================================

    @GetMapping("/count")
    public ResponseEntity<Long> countAllDoctors() {
        return ResponseEntity.ok(
                doctorService.countAllDoctors()
        );
    }

    // ========================================
    // COUNT BY SPECIALTY
    // ========================================

    @GetMapping("/count/specialty/{specialty}")
    public ResponseEntity<Long> countDoctorsBySpecialty(
            @PathVariable("specialty") Specialty specialty
    ) {
        return ResponseEntity.ok(
                doctorService.countDoctorsBySpecialty(
                        specialty
                )
        );
    }

    // ========================================
    // COUNT BY DEPARTMENT
    // ========================================

    @GetMapping("/count/department/{departmentId}")
    public ResponseEntity<Long> countDoctorsByDepartment(
            @PathVariable("departmentId") Long departmentId
    ) {
        return ResponseEntity.ok(
                doctorService.countDoctorsByDepartment(
                        departmentId
                )
        );
    }

    // ========================================
    // COUNT BY ACTIVE STATUS
    // ========================================

    @GetMapping("/count/status")
    public ResponseEntity<Long>
    countDoctorsByActiveStatus(
            @RequestParam Boolean isActive
    ) {
        return ResponseEntity.ok(
                doctorService.countDoctorsByActiveStatus(
                        isActive
                )
        );
    }

    // ========================================
    // COUNT BY SPECIALTY AND STATUS
    // ========================================

    @GetMapping(
            "/count/specialty/{specialty}/status"
    )
    public ResponseEntity<Long>
    countDoctorsBySpecialtyAndActiveStatus(
            @PathVariable("specialty") Specialty specialty,
            @RequestParam Boolean isActive
    ) {
        return ResponseEntity.ok(
                doctorService
                        .countDoctorsBySpecialtyAndActiveStatus(
                                specialty,
                                isActive
                        )
        );
    }

    // ========================================
    // COUNT BY DEPARTMENT AND SPECIALTY
    // ========================================

    @GetMapping(
            "/count/department/{departmentId}"
                    + "/specialty/{specialty}"
    )
    public ResponseEntity<Long>
    countDoctorsByDepartmentAndSpecialty(
            @PathVariable("departmentId") Long departmentId,
            @PathVariable("specialty") Specialty specialty
    ) {
        return ResponseEntity.ok(
                doctorService
                        .countDoctorsByDepartmentAndSpecialty(
                                departmentId,
                                specialty
                        )
        );
    }
}
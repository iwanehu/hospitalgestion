package com.hospital.gestion.api.receptionist.controller;

import com.hospital.gestion.api.common.dto.PageResponseDTO;
import com.hospital.gestion.api.common.enums.ShiftType;
import com.hospital.gestion.api.receptionist.dto.ReceptionistRequestDTO;
import com.hospital.gestion.api.receptionist.dto.ReceptionistResponseDTO;
import com.hospital.gestion.api.receptionist.dto.ReceptionistUpdateDTO;
import com.hospital.gestion.api.receptionist.service.ReceptionistService;
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
@RequestMapping("/api/receptionists")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize(
        "hasAnyRole('ADMIN', 'RECEPTIONIST')"
)
public class ReceptionistController {

    private final ReceptionistService receptionistService;

    // ============================================================
    // CREATE
    // ============================================================

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReceptionistResponseDTO>
    createReceptionist(
            @Valid
            @RequestBody ReceptionistRequestDTO request
    ) {
        log.info(
                "REST request to create receptionist for user: {}",
                request.userId()
        );

        ReceptionistResponseDTO response =
                receptionistService.createReceptionist(
                        request
                );

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
    public ResponseEntity<List<ReceptionistResponseDTO>>
    getAllReceptionists() {
        return ResponseEntity.ok(
                receptionistService.getAllReceptionists()
        );
    }

    @GetMapping("/page")
    public ResponseEntity<PageResponseDTO<ReceptionistResponseDTO>>
    getReceptionistsPaginated(
            @RequestParam(required = false)
            String text,

            @RequestParam(required = false)
            Long departmentId,

            @RequestParam(required = false)
            ShiftType shiftType,

            @RequestParam(required = false)
            Boolean isActive,

            @RequestParam(required = false)
            String deskNumber,

            @PageableDefault(
                    size = 20,
                    sort = "user.lastName",
                    direction = Sort.Direction.ASC
            )
            Pageable pageable
    ) {
        Page<ReceptionistResponseDTO> result =
                receptionistService.getReceptionists(
                        text,
                        departmentId,
                        shiftType,
                        isActive,
                        deskNumber,
                        pageable
                );

        return ResponseEntity.ok(
                PageResponseDTO.from(result)
        );
    }

    // ============================================================
    // ORDERED
    // ============================================================

    @GetMapping("/ordered")
    public ResponseEntity<List<ReceptionistResponseDTO>>
    getAllReceptionistsOrdered() {
        return ResponseEntity.ok(
                receptionistService
                        .getAllReceptionistsOrdered()
        );
    }

    @GetMapping("/active/ordered")
    public ResponseEntity<List<ReceptionistResponseDTO>>
    getActiveReceptionistsOrdered() {
        return ResponseEntity.ok(
                receptionistService
                        .getActiveReceptionistsOrdered()
        );
    }

    // ============================================================
    // GET BY ID
    // ============================================================

    @GetMapping("/{id}")
    public ResponseEntity<ReceptionistResponseDTO>
    getReceptionistById(
            @PathVariable("id") Long id
    ) {
        return ResponseEntity.ok(
                receptionistService.getReceptionistById(id)
        );
    }

    // ============================================================
    // GET BY USER
    // ============================================================

    @GetMapping("/user/{userId}")
    public ResponseEntity<ReceptionistResponseDTO>
    getReceptionistByUserId(
            @PathVariable("userId") Long userId
    ) {
        return ResponseEntity.ok(
                receptionistService
                        .getReceptionistByUserId(userId)
        );
    }

    @GetMapping("/email")
    public ResponseEntity<ReceptionistResponseDTO>
    getReceptionistByEmail(
            @RequestParam String email
    ) {
        return ResponseEntity.ok(
                receptionistService
                        .getReceptionistByEmail(email)
        );
    }

    @GetMapping("/document/{documentId}")
    public ResponseEntity<ReceptionistResponseDTO>
    getReceptionistByDocumentId(
            @PathVariable("documentId") String documentId
    ) {
        return ResponseEntity.ok(
                receptionistService
                        .getReceptionistByDocumentId(documentId)
        );
    }

    // ============================================================
    // BY DEPARTMENT
    // ============================================================

    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<ReceptionistResponseDTO>>
    getReceptionistsByDepartment(
            @PathVariable("departmentId") Long departmentId
    ) {
        return ResponseEntity.ok(
                receptionistService
                        .getReceptionistsByDepartment(
                                departmentId
                        )
        );
    }

    @GetMapping("/department/{departmentId}/page")
    public ResponseEntity<Page<ReceptionistResponseDTO>>
    getReceptionistsByDepartmentPaginated(
            @PathVariable("departmentId") Long departmentId,

            @PageableDefault(
                    size = 20,
                    sort = "user.lastName",
                    direction = Sort.Direction.ASC
            )
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                receptionistService
                        .getReceptionistsByDepartment(
                                departmentId,
                                pageable
                        )
        );
    }

    // ============================================================
    // BY SHIFT
    // ============================================================

    @GetMapping("/shift/{shiftType}")
    public ResponseEntity<List<ReceptionistResponseDTO>>
    getReceptionistsByShift(
            @PathVariable("shiftType") ShiftType shiftType
    ) {
        return ResponseEntity.ok(
                receptionistService
                        .getReceptionistsByShift(shiftType)
        );
    }

    @GetMapping("/shift/{shiftType}/page")
    public ResponseEntity<Page<ReceptionistResponseDTO>>
    getReceptionistsByShiftPaginated(
            @PathVariable("shiftType") ShiftType shiftType,

            @PageableDefault(
                    size = 20,
                    sort = "user.lastName",
                    direction = Sort.Direction.ASC
            )
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                receptionistService
                        .getReceptionistsByShift(
                                shiftType,
                                pageable
                        )
        );
    }

    // ============================================================
    // BY DEPARTMENT AND SHIFT
    // ============================================================

    @GetMapping(
            "/department/{departmentId}/shift/{shiftType}"
    )
    public ResponseEntity<List<ReceptionistResponseDTO>>
    getReceptionistsByDepartmentAndShift(
            @PathVariable("departmentId") Long departmentId,
            @PathVariable("shiftType") ShiftType shiftType
    ) {
        return ResponseEntity.ok(
                receptionistService
                        .getReceptionistsByDepartmentAndShift(
                                departmentId,
                                shiftType
                        )
        );
    }

    @GetMapping(
            "/department/{departmentId}/shift/{shiftType}/page"
    )
    public ResponseEntity<Page<ReceptionistResponseDTO>>
    getReceptionistsByDepartmentAndShiftPaginated(
            @PathVariable("departmentId") Long departmentId,
            @PathVariable("shiftType") ShiftType shiftType,

            @PageableDefault(
                    size = 20,
                    sort = "user.lastName",
                    direction = Sort.Direction.ASC
            )
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                receptionistService
                        .getReceptionistsByDepartmentAndShift(
                                departmentId,
                                shiftType,
                                pageable
                        )
        );
    }

    // ============================================================
    // ACTIVE STATUS
    // ============================================================

    @GetMapping("/status")
    public ResponseEntity<List<ReceptionistResponseDTO>>
    getReceptionistsByActiveStatus(
            @RequestParam Boolean isActive
    ) {
        return ResponseEntity.ok(
                receptionistService
                        .getReceptionistsByActiveStatus(
                                isActive
                        )
        );
    }

    @GetMapping("/status/page")
    public ResponseEntity<Page<ReceptionistResponseDTO>>
    getReceptionistsByActiveStatusPaginated(
            @RequestParam Boolean isActive,

            @PageableDefault(
                    size = 20,
                    sort = "user.lastName",
                    direction = Sort.Direction.ASC
            )
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                receptionistService
                        .getReceptionistsByActiveStatus(
                                isActive,
                                pageable
                        )
        );
    }

    // ============================================================
    // BY DESK
    // ============================================================

    @GetMapping("/desk")
    public ResponseEntity<List<ReceptionistResponseDTO>>
    getReceptionistsByDeskNumber(
            @RequestParam String deskNumber
    ) {
        return ResponseEntity.ok(
                receptionistService
                        .getReceptionistsByDeskNumber(
                                deskNumber
                        )
        );
    }

    @GetMapping("/department/{departmentId}/desk")
    public ResponseEntity<List<ReceptionistResponseDTO>>
    getReceptionistsByDepartmentAndDeskNumber(
            @PathVariable("departmentId") Long departmentId,
            @RequestParam String deskNumber
    ) {
        return ResponseEntity.ok(
                receptionistService
                        .getReceptionistsByDepartmentAndDeskNumber(
                                departmentId,
                                deskNumber
                        )
        );
    }

    // ============================================================
    // SEARCH
    // ============================================================

    @GetMapping("/search")
    public ResponseEntity<List<ReceptionistResponseDTO>>
    searchReceptionists(
            @RequestParam String text
    ) {
        return ResponseEntity.ok(
                receptionistService.searchReceptionists(
                        text
                )
        );
    }

    @GetMapping("/search/page")
    public ResponseEntity<Page<ReceptionistResponseDTO>>
    searchReceptionistsPaginated(
            @RequestParam String text,

            @PageableDefault(
                    size = 20,
                    sort = "user.lastName",
                    direction = Sort.Direction.ASC
            )
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                receptionistService.searchReceptionists(
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
                    + "or (hasRole('RECEPTIONIST') "
                    + "and @hospitalAuthorization.ownsReceptionist("
                    + "#id, authentication))"
    )
    public ResponseEntity<ReceptionistResponseDTO>
    updateReceptionist(
            @PathVariable("id") Long id,

            @Valid
            @RequestBody ReceptionistUpdateDTO request
    ) {
        log.info(
                "REST request to update receptionist: {}",
                id
        );

        return ResponseEntity.ok(
                receptionistService.updateReceptionist(
                        id,
                        request
                )
        );
    }

    // ============================================================
    // DELETE
    // ============================================================

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteReceptionist(
            @PathVariable("id") Long id
    ) {
        log.info(
                "REST request to delete receptionist: {}",
                id
        );

        receptionistService.deleteReceptionist(id);

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
                receptionistService.existsByUserId(userId)
        );
    }

    // ============================================================
    // COUNT
    // ============================================================

    @GetMapping("/count")
    public ResponseEntity<Long> countAllReceptionists() {
        return ResponseEntity.ok(
                receptionistService.countAllReceptionists()
        );
    }

    @GetMapping("/count/department/{departmentId}")
    public ResponseEntity<Long>
    countReceptionistsByDepartment(
            @PathVariable("departmentId") Long departmentId
    ) {
        return ResponseEntity.ok(
                receptionistService
                        .countReceptionistsByDepartment(
                                departmentId
                        )
        );
    }

    @GetMapping("/count/shift/{shiftType}")
    public ResponseEntity<Long> countReceptionistsByShift(
            @PathVariable("shiftType") ShiftType shiftType
    ) {
        return ResponseEntity.ok(
                receptionistService
                        .countReceptionistsByShift(shiftType)
        );
    }

    @GetMapping(
            "/count/department/{departmentId}/shift/{shiftType}"
    )
    public ResponseEntity<Long>
    countReceptionistsByDepartmentAndShift(
            @PathVariable("departmentId") Long departmentId,
            @PathVariable("shiftType") ShiftType shiftType
    ) {
        return ResponseEntity.ok(
                receptionistService
                        .countReceptionistsByDepartmentAndShift(
                                departmentId,
                                shiftType
                        )
        );
    }

    @GetMapping("/count/status")
    public ResponseEntity<Long>
    countReceptionistsByActiveStatus(
            @RequestParam Boolean isActive
    ) {
        return ResponseEntity.ok(
                receptionistService
                        .countReceptionistsByActiveStatus(
                                isActive
                        )
        );
    }
}
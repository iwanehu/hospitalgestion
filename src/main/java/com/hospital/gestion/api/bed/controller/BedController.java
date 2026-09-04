package com.hospital.gestion.api.bed.controller;

import com.hospital.gestion.api.bed.dto.BedRequestDTO;
import com.hospital.gestion.api.bed.dto.BedResponseDTO;
import com.hospital.gestion.api.bed.dto.BedUpdateDTO;
import com.hospital.gestion.api.bed.service.BedService;
import com.hospital.gestion.api.common.dto.PageResponseDTO;
import com.hospital.gestion.api.common.enums.BedStatus;
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
@RequestMapping("/api/beds")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("isAuthenticated()")
public class BedController {

    private final BedService bedService;

    // ========================================
    // CREATE
    // ========================================

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BedResponseDTO> createBed(
            @Valid @RequestBody BedRequestDTO request
    ) {
        log.info(
                "REST request to create bed: {} in room: {}",
                request.bedNumber(),
                request.roomId()
        );

        BedResponseDTO response =
                bedService.createBed(request);

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
    public ResponseEntity<List<BedResponseDTO>> getAllBeds() {
        log.info("REST request to get all beds");

        return ResponseEntity.ok(
                bedService.getAllBeds()
        );
    }

    @GetMapping("/page")
    public ResponseEntity<PageResponseDTO<BedResponseDTO>>
    getBedsPaginated(
            @RequestParam(required = false)
            String bedNumber,

            @RequestParam(required = false)
            BedStatus status,

            @RequestParam(required = false)
            Long roomId,

            @RequestParam(required = false)
            Long wardId,

            @RequestParam(required = false)
            Long departmentId,

            @PageableDefault(
                    size = 20,
                    sort = "bedNumber",
                    direction = Sort.Direction.ASC
            )
            Pageable pageable
    ) {
        log.info(
                "REST request to get beds with filters: "
                        + "bedNumber={}, status={}, roomId={}, "
                        + "wardId={}, departmentId={}, pageable={}",
                bedNumber,
                status,
                roomId,
                wardId,
                departmentId,
                pageable
        );

        Page<BedResponseDTO> result =
                bedService.getBeds(
                        bedNumber,
                        status,
                        roomId,
                        wardId,
                        departmentId,
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
    public ResponseEntity<BedResponseDTO> getBedById(
            @PathVariable("id") Long id
    ) {
        log.info(
                "REST request to get bed by id: {}",
                id
        );

        return ResponseEntity.ok(
                bedService.getBedById(id)
        );
    }

    // ========================================
    // GET BY EXACT NUMBER AND ROOM
    // ========================================

    @GetMapping("/search/exact")
    public ResponseEntity<BedResponseDTO>
    getBedByNumberAndRoom(
            @RequestParam String bedNumber,
            @RequestParam Long roomId
    ) {
        log.info(
                "REST request to get bed: {} in room: {}",
                bedNumber,
                roomId
        );

        return ResponseEntity.ok(
                bedService.getBedByNumberAndRoom(
                        bedNumber,
                        roomId
                )
        );
    }

    // ========================================
    // SEARCH BY NUMBER
    // ========================================

    @GetMapping("/search/number")
    public ResponseEntity<List<BedResponseDTO>>
    searchBedsByNumber(
            @RequestParam String bedNumber
    ) {
        log.info(
                "REST request to search beds by number: {}",
                bedNumber
        );

        return ResponseEntity.ok(
                bedService.searchBedsByNumber(bedNumber)
        );
    }

    @GetMapping("/search/number/page")
    public ResponseEntity<Page<BedResponseDTO>>
    searchBedsByNumberPaginated(
            @RequestParam String bedNumber,
            @PageableDefault(
                    size = 20,
                    sort = "bedNumber"
            )
            Pageable pageable
    ) {
        log.info(
                "REST request to search beds by number: {} with pagination",
                bedNumber
        );

        return ResponseEntity.ok(
                bedService.searchBedsByNumber(
                        bedNumber,
                        pageable
                )
        );
    }

    // ========================================
    // GET BY ROOM
    // ========================================

    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<BedResponseDTO>> getBedsByRoom(
            @PathVariable("roomId") Long roomId
    ) {
        log.info(
                "REST request to get beds by room: {}",
                roomId
        );

        return ResponseEntity.ok(
                bedService.getBedsByRoom(roomId)
        );
    }

    @GetMapping("/room/{roomId}/page")
    public ResponseEntity<Page<BedResponseDTO>>
    getBedsByRoomPaginated(
            @PathVariable("roomId") Long roomId,
            @PageableDefault(
                    size = 20,
                    sort = "bedNumber"
            )
            Pageable pageable
    ) {
        log.info(
                "REST request to get beds by room: {} with pagination",
                roomId
        );

        return ResponseEntity.ok(
                bedService.getBedsByRoom(
                        roomId,
                        pageable
                )
        );
    }

    // ========================================
    // GET BY STATUS
    // ========================================

    @GetMapping("/status/{status}")
    public ResponseEntity<List<BedResponseDTO>> getBedsByStatus(
            @PathVariable("status") BedStatus status
    ) {
        log.info(
                "REST request to get beds by status: {}",
                status
        );

        return ResponseEntity.ok(
                bedService.getBedsByStatus(status)
        );
    }

    @GetMapping("/status/{status}/page")
    public ResponseEntity<Page<BedResponseDTO>>
    getBedsByStatusPaginated(
            @PathVariable BedStatus status,
            @PageableDefault(
                    size = 20,
                    sort = "bedNumber"
            )
            Pageable pageable
    ) {
        log.info(
                "REST request to get beds by status: {} with pagination",
                status
        );

        return ResponseEntity.ok(
                bedService.getBedsByStatus(
                        status,
                        pageable
                )
        );
    }

    // ========================================
    // GET BY ROOM AND STATUS
    // ========================================

    @GetMapping("/room/{roomId}/status/{status}")
    public ResponseEntity<List<BedResponseDTO>>
    getBedsByRoomAndStatus(
            @PathVariable("roomId") Long roomId,
            @PathVariable("status") BedStatus status
    ) {
        log.info(
                "REST request to get beds by room: {} and status: {}",
                roomId,
                status
        );

        return ResponseEntity.ok(
                bedService.getBedsByRoomAndStatus(
                        roomId,
                        status
                )
        );
    }

    @GetMapping("/room/{roomId}/status/{status}/page")
    public ResponseEntity<Page<BedResponseDTO>>
    getBedsByRoomAndStatusPaginated(
            @PathVariable("roomId") Long roomId,
            @PathVariable("status") BedStatus status,
            @PageableDefault(
                    size = 20,
                    sort = "bedNumber"
            )
            Pageable pageable
    ) {
        log.info(
                "REST request to get beds by room: {},status: {} with pagination",
                roomId,
                status
        );

        return ResponseEntity.ok(
                bedService.getBedsByRoomAndStatus(
                        roomId,
                        status,
                        pageable
                )
        );
    }

    // ========================================
    // GET BY WARD
    // ========================================

    @GetMapping("/ward/{wardId}")
    public ResponseEntity<List<BedResponseDTO>> getBedsByWard(
            @PathVariable("wardId") Long wardId
    ) {
        log.info(
                "REST request to get beds by ward: {}",
                wardId
        );

        return ResponseEntity.ok(
                bedService.getBedsByWard(wardId)
        );
    }

    @GetMapping("/ward/{wardId}/page")
    public ResponseEntity<Page<BedResponseDTO>>
    getBedsByWardPaginated(
            @PathVariable("wardId") Long wardId,
            @PageableDefault(
                    size = 20,
                    sort = "bedNumber"
            )
            Pageable pageable
    ) {
        log.info(
                "REST request to get beds by ward: {} "
                        + "with pagination",
                wardId
        );

        return ResponseEntity.ok(
                bedService.getBedsByWard(
                        wardId,
                        pageable
                )
        );
    }

    // ========================================
    // GET BY WARD AND STATUS
    // ========================================

    @GetMapping("/ward/{wardId}/status/{status}")
    public ResponseEntity<List<BedResponseDTO>>
    getBedsByWardAndStatus(
            @PathVariable("wardId") Long wardId,
            @PathVariable("status") BedStatus status
    ) {
        log.info(
                "REST request to get beds by ward: {} "
                        + "and status: {}",
                wardId,
                status
        );

        return ResponseEntity.ok(
                bedService.getBedsByWardAndStatus(
                        wardId,
                        status
                )
        );
    }

    @GetMapping("/ward/{wardId}/status/{status}/page")
    public ResponseEntity<Page<BedResponseDTO>>
    getBedsByWardAndStatusPaginated(
            @PathVariable("wardId") Long wardId,
            @PathVariable("status") BedStatus status,
            @PageableDefault(
                    size = 20,
                    sort = "bedNumber"
            )
            Pageable pageable
    ) {
        log.info(
                "REST request to get beds by ward: {}, "
                        + "status: {} with pagination",
                wardId,
                status
        );

        return ResponseEntity.ok(
                bedService.getBedsByWardAndStatus(
                        wardId,
                        status,
                        pageable
                )
        );
    }

    // ========================================
    // GET BY DEPARTMENT
    // ========================================

    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<BedResponseDTO>>
    getBedsByDepartment(
            @PathVariable("departmentId") Long departmentId
    ) {
        log.info(
                "REST request to get beds by department: {}",
                departmentId
        );

        return ResponseEntity.ok(
                bedService.getBedsByDepartment(departmentId)
        );
    }

    @GetMapping("/department/{departmentId}/page")
    public ResponseEntity<Page<BedResponseDTO>>
    getBedsByDepartmentPaginated(
            @PathVariable("departmentId") Long departmentId,
            @PageableDefault(
                    size = 20,
                    sort = "bedNumber"
            )
            Pageable pageable
    ) {
        log.info(
                "REST request to get beds by department: {} "
                        + "with pagination",
                departmentId
        );

        return ResponseEntity.ok(
                bedService.getBedsByDepartment(
                        departmentId,
                        pageable
                )
        );
    }

    // ========================================
    // GET BY DEPARTMENT AND STATUS
    // ========================================

    @GetMapping(
            "/department/{departmentId}/status/{status}"
    )
    public ResponseEntity<List<BedResponseDTO>>
    getBedsByDepartmentAndStatus(
            @PathVariable("departmentId") Long departmentId,
            @PathVariable("status") BedStatus status
    ) {
        log.info(
                "REST request to get beds by department: {} "
                        + "and status: {}",
                departmentId,
                status
        );

        return ResponseEntity.ok(
                bedService.getBedsByDepartmentAndStatus(
                        departmentId,
                        status
                )
        );
    }

    @GetMapping(
            "/department/{departmentId}/status/{status}/page"
    )
    public ResponseEntity<Page<BedResponseDTO>>
    getBedsByDepartmentAndStatusPaginated(
            @PathVariable("departmentId") Long departmentId,
            @PathVariable("status") BedStatus status,
            @PageableDefault(
                    size = 20,
                    sort = "bedNumber"
            )
            Pageable pageable
    ) {
        log.info(
                "REST request to get beds by department: {}, "
                        + "status: {} with pagination",
                departmentId,
                status
        );

        return ResponseEntity.ok(
                bedService.getBedsByDepartmentAndStatus(
                        departmentId,
                        status,
                        pageable
                )
        );
    }

    // ========================================
    // UPDATE
    // ========================================

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BedResponseDTO> updateBed(
            @PathVariable("id") Long id,
            @Valid @RequestBody BedUpdateDTO request
    ) {
        log.info(
                "REST request to update bed: {}",
                id
        );

        return ResponseEntity.ok(
                bedService.updateBedById(id, request)
        );
    }

    // ========================================
    // RESERVE
    // AVAILABLE -> RESERVED
    // ========================================

    @PatchMapping("/{id}/reserve")
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'NURSE', 'RECEPTIONIST')"
    )
    public ResponseEntity<BedResponseDTO> reserveBed(
            @PathVariable("id") Long id
    ) {
        log.info(
                "REST request to reserve bed: {}",
                id
        );

        return ResponseEntity.ok(
                bedService.reserveBed(id)
        );
    }

    // ========================================
    // OCCUPY
    // AVAILABLE/RESERVED -> OCCUPIED
    // ========================================

    @PatchMapping("/{id}/occupy")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BedResponseDTO> occupyBed(
            @PathVariable("id") Long id
    ) {
        log.info(
                "REST request to occupy bed: {}",
                id
        );

        return ResponseEntity.ok(
                bedService.occupyBed(id)
        );
    }

    // ========================================
    // RELEASE
    // OCCUPIED -> CLEANING
    // ========================================

    @PatchMapping("/{id}/release")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BedResponseDTO> releaseBed(
            @PathVariable("id") Long id
    ) {
        log.info(
                "REST request to release bed: {}",
                id
        );

        return ResponseEntity.ok(
                bedService.releaseBed(id)
        );
    }

    // ========================================
    // FINISH CLEANING
    // CLEANING -> AVAILABLE
    // ========================================

    @PatchMapping("/{id}/finish-cleaning")
    @PreAuthorize("hasAnyRole('ADMIN', 'NURSE')")
    public ResponseEntity<BedResponseDTO> finishBedCleaning(
            @PathVariable("id") Long id
    ) {
        log.info(
                "REST request to finish cleaning bed: {}",
                id
        );

        return ResponseEntity.ok(
                bedService.finishBedCleaning(id)
        );
    }

    // ========================================
    // MAINTENANCE
    // ========================================

    @PatchMapping("/{id}/maintenance")
    @PreAuthorize("hasAnyRole('ADMIN', 'NURSE')")
    public ResponseEntity<BedResponseDTO> sendBedToMaintenance(
            @PathVariable("id") Long id
    ) {
        log.info(
                "REST request to send bed to maintenance: {}",
                id
        );

        return ResponseEntity.ok(
                bedService.sendBedToMaintenance(id)
        );
    }

    // ========================================
    // FINISH MAINTENANCE
    // MAINTENANCE -> AVAILABLE
    // ========================================

    @PatchMapping("/{id}/finish-maintenance")
    @PreAuthorize("hasAnyRole('ADMIN', 'NURSE')")
    public ResponseEntity<BedResponseDTO>
    finishBedMaintenance(
            @PathVariable("id") Long id
    ) {
        log.info(
                "REST request to finish maintenance of bed: {}",
                id
        );

        return ResponseEntity.ok(
                bedService.finishBedMaintenance(id)
        );
    }

    // ========================================
    // CANCEL RESERVATION
    // RESERVED -> AVAILABLE
    // ========================================

    @PatchMapping("/{id}/cancel-reservation")
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'NURSE', 'RECEPTIONIST')"
    )
    public ResponseEntity<BedResponseDTO>
    cancelBedReservation(
            @PathVariable("id") Long id
    ) {
        log.info(
                "REST request to cancel reservation of bed: {}",
                id
        );

        return ResponseEntity.ok(
                bedService.cancelBedReservation(id)
        );
    }

    // ========================================
    // DELETE
    // ========================================

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBed(
            @PathVariable("id") Long id
    ) {
        log.info(
                "REST request to delete bed: {}",
                id
        );

        bedService.deleteBed(id);

        return ResponseEntity.noContent().build();
    }

    // ========================================
    // EXISTS
    // ========================================

    @GetMapping("/exists")
    public ResponseEntity<Boolean> existsByNumberAndRoom(
            @RequestParam String bedNumber,
            @RequestParam Long roomId
    ) {
        log.info(
                "REST request to check bed: {} in room: {}",
                bedNumber,
                roomId
        );

        return ResponseEntity.ok(
                bedService.existsByNumberAndRoom(
                        bedNumber,
                        roomId
                )
        );
    }

    // ========================================
    // COUNT ALL
    // ========================================

    @GetMapping("/count")
    public ResponseEntity<Long> countAllBeds() {
        return ResponseEntity.ok(
                bedService.countAllBeds()
        );
    }

    // ========================================
    // COUNT BY STATUS
    // ========================================

    @GetMapping("/count/status/{status}")
    public ResponseEntity<Long> countBedsByStatus(
            @PathVariable("status") BedStatus status
    ) {
        return ResponseEntity.ok(
                bedService.countBedsByStatus(status)
        );
    }

    // ========================================
    // COUNT BY ROOM
    // ========================================

    @GetMapping("/count/room/{roomId}")
    public ResponseEntity<Long> countBedsByRoom(
            @PathVariable("roomId") Long roomId
    ) {
        return ResponseEntity.ok(
                bedService.countBedsByRoom(roomId)
        );
    }

    // ========================================
    // COUNT BY ROOM AND STATUS
    // ========================================

    @GetMapping("/count/room/{roomId}/status/{status}")
    public ResponseEntity<Long> countBedsByRoomAndStatus(
            @PathVariable("roomId") Long roomId,
            @PathVariable("status") BedStatus status
    ) {
        return ResponseEntity.ok(
                bedService.countBedsByRoomAndStatus(
                        roomId,
                        status
                )
        );
    }

    // ========================================
    // COUNT BY WARD
    // ========================================

    @GetMapping("/count/ward/{wardId}")
    public ResponseEntity<Long> countBedsByWard(
            @PathVariable("wardId") Long wardId
    ) {
        return ResponseEntity.ok(
                bedService.countBedsByWard(wardId)
        );
    }

    // ========================================
    // COUNT BY WARD AND STATUS
    // ========================================

    @GetMapping("/count/ward/{wardId}/status/{status}")
    public ResponseEntity<Long> countBedsByWardAndStatus(
            @PathVariable("wardId") Long wardId,
            @PathVariable("status") BedStatus status
    ) {
        return ResponseEntity.ok(
                bedService.countBedsByWardAndStatus(
                        wardId,
                        status
                )
        );
    }

    // ========================================
    // COUNT BY DEPARTMENT
    // ========================================

    @GetMapping("/count/department/{departmentId}")
    public ResponseEntity<Long> countBedsByDepartment(
            @PathVariable("departmentId") Long departmentId
    ) {
        return ResponseEntity.ok(
                bedService.countBedsByDepartment(
                        departmentId
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
    countBedsByDepartmentAndStatus(
            @PathVariable("departmentId") Long departmentId,
            @PathVariable("status") BedStatus status
    ) {
        return ResponseEntity.ok(
                bedService.countBedsByDepartmentAndStatus(
                        departmentId,
                        status
                )
        );
    }
}
package com.hospital.gestion.api.ward.controller;


import com.hospital.gestion.api.common.dto.PageResponseDTO;
import com.hospital.gestion.api.ward.dto.WardRequestDTO;
import com.hospital.gestion.api.ward.dto.WardResponseDTO;
import com.hospital.gestion.api.ward.dto.WardUpdateDTO;
import com.hospital.gestion.api.ward.service.WardService;
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
@RequestMapping("/api/wards")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("isAuthenticated()")
public class WardController {

    private final WardService wardService;

    private static final String WITH_PAGINATION = " with pagination";


    //========
    //CREATE
    //===========

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WardResponseDTO> createWard(@Valid @RequestBody WardRequestDTO request){
        log.info("Rest request to create Ward : {}", request.name());

        WardResponseDTO response =
                wardService.createWard(request);

        URI location= ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);

    }


    //==========
    //GET ALL
    //=========

    @GetMapping
    public ResponseEntity<List<WardResponseDTO>> getAllWards(){
        log.info("Rest request to get all wards");

        return ResponseEntity.ok(wardService.getAllWards());
    }

    @GetMapping("/page")
    public ResponseEntity<PageResponseDTO<WardResponseDTO>>
    getWardsPaginated(
            @RequestParam(required = false)
            String name,

            @RequestParam(required = false)
            String description,

            @RequestParam(required = false)
            Boolean isActive,

            @RequestParam(required = false)
            Long departmentId,

            @PageableDefault(
                    size = 20,
                    sort = "name",
                    direction = Sort.Direction.ASC
            )
            Pageable pageable
    ) {
        log.info(
                "REST request to get wards with filters: "
                        + "name={}, description={}, "
                        + "active={}, departmentId={}, pageable={}",
                name,
                description,
                isActive,
                departmentId,
                pageable
        );

        Page<WardResponseDTO> result =
                wardService.getWards(
                        name,
                        description,
                        isActive,
                        departmentId,
                        pageable
                );

        return ResponseEntity.ok(
                PageResponseDTO.from(result)
        );
    }


    //========
    //GET BY ID
    //============



    @GetMapping("/{id:[0-9]+}")
    public ResponseEntity<WardResponseDTO> getWardById(
            @PathVariable Long id
    ) {
        log.info(
                "REST request to get ward: {}",
                id
        );

        return ResponseEntity.ok(
                wardService.getWardById(id)
        );
    }


    // ========================================
    // GET BY ACTIVE STATUS
    // ========================================


    @GetMapping("/status")
    public ResponseEntity<List<WardResponseDTO>> getWardsByActiveStatus(
            @RequestParam Boolean isActive
    ){
        log.info(
                "REST request to get wards by active status: {}",
                isActive
        );

        return ResponseEntity.ok(wardService.getWardsByActiveStatus(isActive));
    }

    @GetMapping("/status/page")
    public ResponseEntity<Page<WardResponseDTO>>
    getWardsByActiveStatusPaginated(
            @RequestParam Boolean isActive,
            @PageableDefault(size = 20, sort = "name")
            Pageable pageable
    ) {
        log.info(
                "REST request to get wards by status: {} "
                        + WITH_PAGINATION,
                isActive
        );

        return ResponseEntity.ok(
                wardService.getWardsByActiveStatus(
                        isActive,
                        pageable
                )
        );
    }


    //========
    //GET BY DEPARTMENT
    //===================

    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<WardResponseDTO>>
    getWardsByDepartment(
            @PathVariable("departmentId") Long departmentId
    ) {
        log.info(
                "REST request to get wards by department: {}",
                departmentId
        );

        return ResponseEntity.ok(
                wardService.getWardsByDepartment(departmentId)
        );
    }

    @GetMapping("/department/{departmentId}/page")
    public ResponseEntity<Page<WardResponseDTO>>
    getWardsByDepartmentPaginated(
            @PathVariable Long departmentId,
            @PageableDefault(size = 20, sort = "name")
            Pageable pageable
    ) {
        log.info(
                "REST request to get wards by department: {} "
                        + WITH_PAGINATION,
                departmentId
        );

        return ResponseEntity.ok(
                wardService.getWardsByDepartment(
                        departmentId,
                        pageable
                )
        );
    }

    // ========================================
    // GET BY DEPARTMENT AND ACTIVE STATUS
    // ========================================

    @GetMapping("/department/{departmentId}/status")
    public ResponseEntity<List<WardResponseDTO>>
    getWardsByDepartmentAndActiveStatus(
            @PathVariable Long departmentId,
            @RequestParam Boolean isActive
    ) {
        log.info(
                "REST request to get wards by department: {} "
                        + "and status: {}",
                departmentId,
                isActive
        );

        return ResponseEntity.ok(
                wardService.getWardsByDepartmentAndActiveStatus(
                        departmentId,
                        isActive
                )
        );
    }

    @GetMapping("/department/{departmentId}/status/page")
    public ResponseEntity<Page<WardResponseDTO>>
    getWardsByDepartmentAndActiveStatusPaginated(
            @PathVariable Long departmentId,
            @RequestParam Boolean isActive,
            @PageableDefault(size = 20, sort = "name")
            Pageable pageable
    ) {
        log.info(
                "REST request to get wards by department: {}, "
                        + "status: {} with pagination",
                departmentId,
                isActive
        );

        return ResponseEntity.ok(
                wardService.getWardsByDepartmentAndActiveStatus(
                        departmentId,
                        isActive,
                        pageable
                )
        );
    }

    // ========================================
    // GET BY EXACT NAME AND DEPARTMENT
    // ========================================

    @GetMapping("/search/exact")
    public ResponseEntity<WardResponseDTO>
    getWardByNameAndDepartment(
            @RequestParam String name,
            @RequestParam Long departmentId
    ) {
        log.info(
                "REST request to get ward by name: {} "
                        + "and department: {}",
                name,
                departmentId
        );

        return ResponseEntity.ok(
                wardService.getWardByNameAndDepartment(
                        name,
                        departmentId
                )
        );
    }


    // ========================================
    // SEARCH BY NAME
    // ========================================

    @GetMapping("/search/name")
    public ResponseEntity<List<WardResponseDTO>>
    searchWardsByName(
            @RequestParam String name
    ) {
        log.info(
                "REST request to search wards by name: {}",
                name
        );

        return ResponseEntity.ok(
                wardService.searchWardsByName(name)
        );
    }

    @GetMapping("/search/name/page")
    public ResponseEntity<Page<WardResponseDTO>>
    searchWardsByNamePaginated(
            @RequestParam String name,
            @PageableDefault(size = 20, sort = "name")
            Pageable pageable
    ) {
        log.info(
                "REST request to search wards by name: {} "
                        + "with pagination",
                name
        );

        return ResponseEntity.ok(
                wardService.searchWardsByName(
                        name,
                        pageable
                )
        );
    }



    // ========================================
    // UPDATE
    // ========================================

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WardResponseDTO> updateWard(
            @PathVariable Long id,
            @Valid @RequestBody WardUpdateDTO request
    ) {
        log.info("REST request to update ward: {}", id);

        return ResponseEntity.ok(
                wardService.updateWardById(id, request)
        );
    }

    // ========================================
    // DELETE
    // ========================================

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteWard(
            @PathVariable Long id
    ) {
        log.info("REST request to delete ward: {}", id);

        wardService.deleteWard(id);

        return ResponseEntity.noContent().build();
    }


    // ========================================
    // ACTIVATE
    // ========================================

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> activateWard(
            @PathVariable Long id
    ) {
        log.info("REST request to activate ward: {}", id);

        wardService.activateWard(id);

        return ResponseEntity.noContent().build();
    }

    // ========================================
    // DEACTIVATE
    // ========================================

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivateWard(
            @PathVariable Long id
    ) {
        log.info("REST request to deactivate ward: {}", id);

        wardService.deactivateWard(id);

        return ResponseEntity.noContent().build();
    }

    // ========================================
    // EXISTS
    // ========================================

    @GetMapping("/exists")
    public ResponseEntity<Boolean> existsByNameAndDepartment(
            @RequestParam String name,
            @RequestParam Long departmentId
    ) {
        log.info(
                "REST request to check ward name: {} "
                        + "in department: {}",
                name,
                departmentId
        );

        return ResponseEntity.ok(
                wardService.existsByNameAndDepartment(
                        name,
                        departmentId
                )
        );
    }

    // ========================================
    // COUNT ALL
    // ========================================

    @GetMapping("/count")
    public ResponseEntity<Long> countAllWards() {
        log.info("REST request to count all wards");

        return ResponseEntity.ok(
                wardService.countAllWards()
        );
    }

    // ========================================
    // COUNT BY STATUS
    // ========================================

    @GetMapping("/count/status")
    public ResponseEntity<Long> countWardsByActiveStatus(
            @RequestParam Boolean isActive
    ) {
        log.info(
                "REST request to count wards by status: {}",
                isActive
        );

        return ResponseEntity.ok(
                wardService.countWardsByActiveStatus(isActive)
        );
    }

    // ========================================
    // COUNT BY DEPARTMENT
    // ========================================

    @GetMapping("/count/department/{departmentId}")
    public ResponseEntity<Long> countWardsByDepartment(
            @PathVariable Long departmentId
    ) {
        log.info(
                "REST request to count wards by department: {}",
                departmentId
        );

        return ResponseEntity.ok(
                wardService.countWardsByDepartment(departmentId)
        );
    }

    // ========================================
    // COUNT BY DEPARTMENT AND STATUS
    // ========================================

    @GetMapping("/count/department/{departmentId}/status")
    public ResponseEntity<Long>
    countWardsByDepartmentAndActiveStatus(
            @PathVariable Long departmentId,
            @RequestParam Boolean isActive
    ) {
        log.info(
                "REST request to count wards by department: {} "
                        + "and status: {}",
                departmentId,
                isActive
        );

        return ResponseEntity.ok(
                wardService.countWardsByDepartmentAndActiveStatus(
                        departmentId,
                        isActive
                )
        );
    }



}


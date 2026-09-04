package com.hospital.gestion.api.admin.controller;

import com.hospital.gestion.api.admin.dto.AdminRequestDTO;
import com.hospital.gestion.api.admin.dto.AdminResponseDTO;
import com.hospital.gestion.api.admin.dto.AdminUpdateDTO;
import com.hospital.gestion.api.admin.service.AdminService;
import com.hospital.gestion.api.common.dto.PageResponseDTO;
import com.hospital.gestion.api.common.enums.AdminLevel;
import com.hospital.gestion.api.common.enums.AdminPermission;

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
@RequestMapping("/api/admins")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {


    private final AdminService adminService;


    // ============================================================
    // CREATE
    // ============================================================

    @PostMapping
    @PreAuthorize(
            "@hospitalAuthorization.isSuperAdmin(authentication) "
                    + "or @hospitalAuthorization.hasAdminPermission("
                    + "T(com.hospital.gestion.api.common.enums.AdminPermission)"
                    + ".MANAGE_ROLES, authentication)"
    )
    public ResponseEntity<AdminResponseDTO> createAdmin(
            @Valid @RequestBody AdminRequestDTO request
    ) {
        log.info(
                "REST request to create admin for user: {}",
                request.userId()
        );

        AdminResponseDTO response =
                adminService.createAdmin(request);

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
    public ResponseEntity<List<AdminResponseDTO>>
    getAllAdmins() {
        return ResponseEntity.ok(
                adminService.getAllAdmins()
        );
    }

    @GetMapping("/page")
    public ResponseEntity<PageResponseDTO<AdminResponseDTO>>
    getAdminsPage(
            @RequestParam(required = false)
            String text,

            @RequestParam(required = false)
            AdminLevel adminLevel,

            @RequestParam(required = false)
            Long departmentId,

            @RequestParam(required = false)
            AdminPermission permission,

            @RequestParam(required = false)
            Boolean isActive,

            @RequestParam(required = false)
            Boolean isSuperAdmin,

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE_TIME
            )
            LocalDateTime lastLoginFrom,

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE_TIME
            )
            LocalDateTime lastLoginTo,

            @PageableDefault(
                    size = 20,
                    sort = "user.lastName",
                    direction = Sort.Direction.ASC
            )
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                adminService.searchAdmins(
                        text,
                        adminLevel,
                        departmentId,
                        permission,
                        isActive,
                        isSuperAdmin,
                        lastLoginFrom,
                        lastLoginTo,
                        pageable
                )
        );
    }

    // ============================================================
    // ORDERED
    // ============================================================

    @GetMapping("/ordered")
    public ResponseEntity<List<AdminResponseDTO>>
    getAllAdminsOrdered() {
        return ResponseEntity.ok(
                adminService.getAllAdminsOrdered()
        );
    }

    @GetMapping("/active/ordered")
    public ResponseEntity<List<AdminResponseDTO>>
    getActiveAdminsOrdered() {
        return ResponseEntity.ok(
                adminService.getActiveAdminsOrdered()
        );
    }

    // ============================================================
    // GET BY ID
    // ============================================================

    @GetMapping("/{id}")
    public ResponseEntity<AdminResponseDTO> getAdminById(
            @PathVariable("id") Long id
    ) {
        return ResponseEntity.ok(
                adminService.getAdminById(id)
        );
    }

    // ============================================================
    // GET BY USER
    // ============================================================

    @GetMapping("/user/{userId}")
    public ResponseEntity<AdminResponseDTO> getAdminByUserId(
            @PathVariable("userId") Long userId
    ) {
        return ResponseEntity.ok(
                adminService.getAdminByUserId(userId)
        );
    }

    @GetMapping("/email")
    public ResponseEntity<AdminResponseDTO> getAdminByEmail(
            @RequestParam String email
    ) {
        return ResponseEntity.ok(
                adminService.getAdminByEmail(email)
        );
    }

    @GetMapping("/document/{documentId}")
    public ResponseEntity<AdminResponseDTO>
    getAdminByDocumentId(
            @PathVariable("documentId") String documentId
    ) {
        return ResponseEntity.ok(
                adminService.getAdminByDocumentId(
                        documentId
                )
        );
    }

    // ============================================================
    // BY ADMIN LEVEL
    // ============================================================

    @GetMapping("/level/{adminLevel}")
    public ResponseEntity<List<AdminResponseDTO>>
    getAdminsByLevel(
            @PathVariable("adminLevel") AdminLevel adminLevel
    ) {
        return ResponseEntity.ok(
                adminService.getAdminsByLevel(adminLevel)
        );
    }

    @GetMapping("/level/{adminLevel}/page")
    public ResponseEntity<Page<AdminResponseDTO>>
    getAdminsByLevelPaginated(
            @PathVariable("adminLevel") AdminLevel adminLevel,

            @PageableDefault(
                    size = 20,
                    sort = "user.lastName",
                    direction = Sort.Direction.ASC
            )
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                adminService.getAdminsByLevel(
                        adminLevel,
                        pageable
                )
        );
    }

    // ============================================================
    // BY DEPARTMENT
    // ============================================================

    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<AdminResponseDTO>>
    getAdminsByDepartment(
            @PathVariable("departmentId") Long departmentId
    ) {
        return ResponseEntity.ok(
                adminService.getAdminsByDepartment(
                        departmentId
                )
        );
    }

    @GetMapping("/department/{departmentId}/page")
    public ResponseEntity<Page<AdminResponseDTO>>
    getAdminsByDepartmentPaginated(
            @PathVariable("departmentId") Long departmentId,

            @PageableDefault(
                    size = 20,
                    sort = "user.lastName",
                    direction = Sort.Direction.ASC
            )
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                adminService.getAdminsByDepartment(
                        departmentId,
                        pageable
                )
        );
    }

    // ============================================================
    // BY DEPARTMENT AND LEVEL
    // ============================================================

    @GetMapping(
            "/department/{departmentId}/level/{adminLevel}"
    )
    public ResponseEntity<List<AdminResponseDTO>>
    getAdminsByDepartmentAndLevel(
            @PathVariable("departmentId") Long departmentId,
            @PathVariable("adminLevel") AdminLevel adminLevel
    ) {
        return ResponseEntity.ok(
                adminService.getAdminsByDepartmentAndLevel(
                        departmentId,
                        adminLevel
                )
        );
    }

    @GetMapping(
            "/department/{departmentId}/level/{adminLevel}/page"
    )
    public ResponseEntity<Page<AdminResponseDTO>>
    getAdminsByDepartmentAndLevelPaginated(
            @PathVariable("departmentId") Long departmentId,
            @PathVariable("adminLevel") AdminLevel adminLevel,

            @PageableDefault(
                    size = 20,
                    sort = "user.lastName",
                    direction = Sort.Direction.ASC
            )
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                adminService.getAdminsByDepartmentAndLevel(
                        departmentId,
                        adminLevel,
                        pageable
                )
        );
    }

    // ============================================================
    // SUPER ADMINS
    // ============================================================

    @GetMapping("/super")
    public ResponseEntity<List<AdminResponseDTO>>
    getSuperAdmins(
            @RequestParam(
                    defaultValue = "true"
            )
            boolean isSuperAdmin
    ) {
        return ResponseEntity.ok(
                adminService.getSuperAdmins(isSuperAdmin)
        );
    }

    @GetMapping("/super/page")
    public ResponseEntity<Page<AdminResponseDTO>>
    getSuperAdminsPaginated(
            @RequestParam(
                    defaultValue = "true"
            )
            boolean isSuperAdmin,

            @PageableDefault(
                    size = 20,
                    sort = "user.lastName",
                    direction = Sort.Direction.ASC
            )
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                adminService.getSuperAdmins(
                        isSuperAdmin,
                        pageable
                )
        );
    }

    // ============================================================
    // ACTIVE STATUS
    // ============================================================

    @GetMapping("/status")
    public ResponseEntity<List<AdminResponseDTO>>
    getAdminsByActiveStatus(
            @RequestParam Boolean isActive
    ) {
        return ResponseEntity.ok(
                adminService.getAdminsByActiveStatus(
                        isActive
                )
        );
    }

    @GetMapping("/status/page")
    public ResponseEntity<Page<AdminResponseDTO>>
    getAdminsByActiveStatusPaginated(
            @RequestParam Boolean isActive,

            @PageableDefault(
                    size = 20,
                    sort = "user.lastName",
                    direction = Sort.Direction.ASC
            )
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                adminService.getAdminsByActiveStatus(
                        isActive,
                        pageable
                )
        );
    }

    // ============================================================
    // BY PERMISSION
    // ============================================================

    @GetMapping("/permission/{permission}")
    public ResponseEntity<List<AdminResponseDTO>>
    getAdminsByPermission(
            @PathVariable("permission") AdminPermission permission
    ) {
        return ResponseEntity.ok(
                adminService.getAdminsByPermission(
                        permission
                )
        );
    }

    @GetMapping("/permission/{permission}/page")
    public ResponseEntity<Page<AdminResponseDTO>>
    getAdminsByPermissionPaginated(
            @PathVariable("permission") AdminPermission permission,

            @PageableDefault(
                    size = 20,
                    sort = "user.lastName",
                    direction = Sort.Direction.ASC
            )
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                adminService.getAdminsByPermission(
                        permission,
                        pageable
                )
        );
    }

    // ============================================================
    // PERMISSION MANAGEMENT
    // ============================================================

    @GetMapping("/{adminId}/permissions/{permission}/exists")
    public ResponseEntity<Boolean> hasPermission(
            @PathVariable("adminId") Long adminId,
            @PathVariable("permission") AdminPermission permission
    ) {
        return ResponseEntity.ok(
                adminService.hasPermission(
                        adminId,
                        permission
                )
        );
    }

    @PatchMapping(
            "/{adminId}/permissions/{permission}/grant"
    )
    @PreAuthorize(
            "@hospitalAuthorization.isSuperAdmin(authentication) "
                    + "or @hospitalAuthorization.hasAdminPermission("
                    + "T(com.hospital.gestion.api.common.enums.AdminPermission)"
                    + ".MANAGE_ROLES, authentication)"
    )
    public ResponseEntity<AdminResponseDTO> grantPermission(
            @PathVariable("adminId") Long adminId,
            @PathVariable("permission") AdminPermission permission
    ) {
        log.info(
                "REST request to grant permission {} to admin {}",
                permission,
                adminId
        );

        return ResponseEntity.ok(
                adminService.grantPermission(
                        adminId,
                        permission
                )
        );
    }

    @PatchMapping(
            "/{adminId}/permissions/{permission}/revoke"
    )
    @PreAuthorize(
            "@hospitalAuthorization.isSuperAdmin(authentication) "
                    + "or @hospitalAuthorization.hasAdminPermission("
                    + "T(com.hospital.gestion.api.common.enums.AdminPermission)"
                    + ".MANAGE_ROLES, authentication)"
    )
    public ResponseEntity<AdminResponseDTO> revokePermission(
            @PathVariable("adminId") Long adminId,
            @PathVariable("permission") AdminPermission permission
    ) {
        log.info(
                "REST request to revoke permission {} from admin {}",
                permission,
                adminId
        );

        return ResponseEntity.ok(
                adminService.revokePermission(
                        adminId,
                        permission
                )
        );
    }

    // ============================================================
    // SEARCH
    // ============================================================

    @GetMapping("/search")
    public ResponseEntity<List<AdminResponseDTO>>
    searchAdmins(
            @RequestParam String text
    ) {
        return ResponseEntity.ok(
                adminService.searchAdmins(text)
        );
    }

    @GetMapping("/search/page")
    public ResponseEntity<Page<AdminResponseDTO>>
    searchAdminsPaginated(
            @RequestParam String text,

            @PageableDefault(
                    size = 20,
                    sort = "user.lastName",
                    direction = Sort.Direction.ASC
            )
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                adminService.searchAdmins(
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
            "@hospitalAuthorization.isSuperAdmin(authentication)"
    )
    public ResponseEntity<AdminResponseDTO> updateAdmin(
            @PathVariable("id") Long id,
            @Valid @RequestBody AdminUpdateDTO request
    ) {
        log.info(
                "REST request to update admin: {}",
                id
        );

        return ResponseEntity.ok(
                adminService.updateAdmin(id, request)
        );
    }


    // ============================================================
    // DELETE
    // ============================================================

    @DeleteMapping("/{id}")
    @PreAuthorize(
            "@hospitalAuthorization.isSuperAdmin(authentication)"
    )
    public ResponseEntity<Void> deleteAdmin(
            @PathVariable("id") Long id
    ) {
        log.info(
                "REST request to delete admin: {}",
                id
        );

        adminService.deleteAdmin(id);

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
                adminService.existsByUserId(userId)
        );
    }

    // ============================================================
    // COUNT
    // ============================================================

    @GetMapping("/count")
    public ResponseEntity<Long> countAllAdmins() {
        return ResponseEntity.ok(
                adminService.countAllAdmins()
        );
    }

    @GetMapping("/count/level/{adminLevel}")
    public ResponseEntity<Long> countAdminsByLevel(
            @PathVariable("adminLevel") AdminLevel adminLevel
    ) {
        return ResponseEntity.ok(
                adminService.countAdminsByLevel(
                        adminLevel
                )
        );
    }

    @GetMapping("/count/department/{departmentId}")
    public ResponseEntity<Long> countAdminsByDepartment(
            @PathVariable("departmentId") Long departmentId
    ) {
        return ResponseEntity.ok(
                adminService.countAdminsByDepartment(
                        departmentId
                )
        );
    }

    @GetMapping(
            "/count/department/{departmentId}/level/{adminLevel}"
    )
    public ResponseEntity<Long>
    countAdminsByDepartmentAndLevel(
            @PathVariable("departmentId") Long departmentId,
            @PathVariable("adminLevel") AdminLevel adminLevel
    ) {
        return ResponseEntity.ok(
                adminService.countAdminsByDepartmentAndLevel(
                        departmentId,
                        adminLevel
                )
        );
    }

    @GetMapping("/count/super")
    public ResponseEntity<Long> countSuperAdmins(
            @RequestParam(
                    defaultValue = "true"
            )
            boolean isSuperAdmin
    ) {
        return ResponseEntity.ok(
                adminService.countSuperAdmins(
                        isSuperAdmin
                )
        );
    }

    @GetMapping("/count/status")
    public ResponseEntity<Long> countAdminsByActiveStatus(
            @RequestParam Boolean isActive
    ) {
        return ResponseEntity.ok(
                adminService.countAdminsByActiveStatus(
                        isActive
                )
        );
    }




}
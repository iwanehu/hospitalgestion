package com.hospital.gestion.api.user.controller;

import com.hospital.gestion.api.common.dto.PageResponseDTO;
import com.hospital.gestion.api.common.enums.Role;
import com.hospital.gestion.api.user.dto.PasswordChangeDTO;
import com.hospital.gestion.api.user.dto.UserRequestDTO;
import com.hospital.gestion.api.user.dto.UserResponseDTO;
import com.hospital.gestion.api.user.dto.UserUpdateDTO;
import com.hospital.gestion.api.user.service.UserService;
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
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    // ========================================
    // CREATE
    // ========================================

    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(
            @Valid @RequestBody UserRequestDTO request
    ) {
        log.info(
                "REST request to create user with email: {}",
                request.email()
        );

        UserResponseDTO response =
                userService.createUser(request);

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
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        log.info("REST request to get all users");

        return ResponseEntity.ok(
                userService.getAllUsers()
        );
    }

    @GetMapping("/page")
    public ResponseEntity<PageResponseDTO<UserResponseDTO>>
    getUsersPaginated(
            @RequestParam(required = false)
            String text,

            @RequestParam(required = false)
            Role role,

            @RequestParam(required = false)
            Boolean isActive,

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE_TIME
            )
            LocalDateTime createdFrom,

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE_TIME
            )
            LocalDateTime createdTo,

            @PageableDefault(
                    size = 20,
                    sort = "lastName",
                    direction = Sort.Direction.ASC
            )
            Pageable pageable
    ) {
        Page<UserResponseDTO> result =
                userService.getUsers(
                        text,
                        role,
                        isActive,
                        createdFrom,
                        createdTo,
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
    public ResponseEntity<List<UserResponseDTO>>
    getAllUsersOrdered() {
        log.info(
                "REST request to get users ordered by name"
        );

        return ResponseEntity.ok(
                userService.getAllUsersOrdered()
        );
    }

    @GetMapping("/active/ordered")
    public ResponseEntity<List<UserResponseDTO>>
    getActiveUsersOrdered() {
        log.info(
                "REST request to get active users ordered"
        );

        return ResponseEntity.ok(
                userService.getActiveUsersOrdered()
        );
    }

    // ========================================
    // GET BY ID
    // ========================================

    @GetMapping("/{id}")
    @PreAuthorize(
            "hasRole('ADMIN') "
                    + "or authentication.principal.id() == #id"
    )
    public ResponseEntity<UserResponseDTO> getUserById(
            @PathVariable("id") Long id
    ) {
        log.info(
                "REST request to get user by id: {}",
                id
        );

        return ResponseEntity.ok(
                userService.getUserById(id)
        );
    }

    // ========================================
    // GET BY EMAIL
    // ========================================

    @GetMapping("/email")
    public ResponseEntity<UserResponseDTO> getUserByEmail(
            @RequestParam String email
    ) {
        log.info(
                "REST request to get user by email: {}",
                email
        );

        return ResponseEntity.ok(
                userService.getUserByEmail(email)
        );
    }

    // ========================================
    // GET BY DOCUMENT
    // ========================================

    @GetMapping("/document/{documentId}")
    public ResponseEntity<UserResponseDTO>
    getUserByDocumentId(
            @PathVariable("documentId") String documentId
    ) {
        log.info(
                "REST request to get user by document: {}",
                documentId
        );

        return ResponseEntity.ok(
                userService.getUserByDocumentId(documentId)
        );
    }

    // ========================================
    // GET BY ROLE
    // ========================================

    @GetMapping("/role/{role}")
    public ResponseEntity<List<UserResponseDTO>>
    getUsersByRole(
            @PathVariable("role") Role role
    ) {
        log.info(
                "REST request to get users by role: {}",
                role
        );

        return ResponseEntity.ok(
                userService.getUsersByRole(role)
        );
    }

    @GetMapping("/role/{role}/page")
    public ResponseEntity<Page<UserResponseDTO>>
    getUsersByRolePaginated(
            @PathVariable("role") Role role,
            @PageableDefault(
                    size = 20,
                    sort = "lastName"
            )
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                userService.getUsersByRole(
                        role,
                        pageable
                )
        );
    }

    // ========================================
    // GET BY ACTIVE STATUS
    // ========================================

    @GetMapping("/status")
    public ResponseEntity<List<UserResponseDTO>>
    getUsersByActiveStatus(
            @RequestParam Boolean isActive
    ) {
        log.info(
                "REST request to get users by status: {}",
                isActive
        );

        return ResponseEntity.ok(
                userService.getUsersByActiveStatus(isActive)
        );
    }

    @GetMapping("/status/page")
    public ResponseEntity<Page<UserResponseDTO>>
    getUsersByActiveStatusPaginated(
            @RequestParam Boolean isActive,
            @PageableDefault(
                    size = 20,
                    sort = "lastName"
            )
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                userService.getUsersByActiveStatus(
                        isActive,
                        pageable
                )
        );
    }

    // ========================================
    // GET BY ROLE AND STATUS
    // ========================================

    @GetMapping("/role/{role}/status")
    public ResponseEntity<List<UserResponseDTO>>
    getUsersByRoleAndActiveStatus(
            @PathVariable("role") Role role,
            @RequestParam Boolean isActive
    ) {
        log.info(
                "REST request to get users by role: {} "
                        + "and status: {}",
                role,
                isActive
        );

        return ResponseEntity.ok(
                userService.getUsersByRoleAndActiveStatus(
                        role,
                        isActive
                )
        );
    }

    @GetMapping("/role/{role}/status/page")
    public ResponseEntity<Page<UserResponseDTO>>
    getUsersByRoleAndActiveStatusPaginated(
            @PathVariable("role") Role role,
            @RequestParam Boolean isActive,
            @PageableDefault(
                    size = 20,
                    sort = "lastName"
            )
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                userService.getUsersByRoleAndActiveStatus(
                        role,
                        isActive,
                        pageable
                )
        );
    }

    // ========================================
    // SEARCH
    // ========================================

    @GetMapping("/search")
    public ResponseEntity<List<UserResponseDTO>> searchUsers(
            @RequestParam String text
    ) {
        log.info(
                "REST request to search users: {}",
                text
        );

        return ResponseEntity.ok(
                userService.searchUsers(text)
        );
    }

    @GetMapping("/search/page")
    public ResponseEntity<Page<UserResponseDTO>>
    searchUsersPaginated(
            @RequestParam String text,
            @PageableDefault(
                    size = 20,
                    sort = "lastName"
            )
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                userService.searchUsers(
                        text,
                        pageable
                )
        );
    }

    // ========================================
    // UPDATE
    // ========================================

    @PutMapping("/{id}")
    @PreAuthorize(
            "hasRole('ADMIN') "
                    + "or authentication.principal.id() == #id"
    )
    public ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable("id") Long id,
            @Valid @RequestBody UserUpdateDTO request
    ) {
        log.info(
                "REST request to update user: {}",
                id
        );

        return ResponseEntity.ok(
                userService.updateUser(id, request)
        );
    }

    // ========================================
    // CHANGE PASSWORD
    // ========================================
    @PatchMapping("/{id}/password")
    @PreAuthorize(
            "authentication.principal.id() == #id"
    )
    public ResponseEntity<Void> changePassword(
            @PathVariable("id") Long id,
            @Valid @RequestBody PasswordChangeDTO request
    ) {
        log.info(
                "REST request to change password "
                        + "for user: {}",
                id
        );

        userService.changePassword(id, request);

        return ResponseEntity.noContent().build();
    }

    // ========================================
    // ACTIVATE
    // ========================================

    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activateUser(
            @PathVariable("id") Long id
    ) {
        log.info(
                "REST request to activate user: {}",
                id
        );

        userService.activateUser(id);

        return ResponseEntity.noContent().build();
    }

    // ========================================
    // DEACTIVATE
    // ========================================

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateUser(
            @PathVariable("id") Long id
    ) {
        log.info(
                "REST request to deactivate user: {}",
                id
        );

        userService.deactivateUser(id);

        return ResponseEntity.noContent().build();
    }

    // ========================================
    // DELETE
    // ========================================

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable("id") Long id
    ) {
        log.info(
                "REST request to delete user: {}",
                id
        );

        userService.deleteUser(id);

        return ResponseEntity.noContent().build();
    }

    // ========================================
    // EXISTS BY EMAIL
    // ========================================

    @GetMapping("/exists/email")
    public ResponseEntity<Boolean> existsByEmail(
            @RequestParam String email
    ) {
        return ResponseEntity.ok(
                userService.existsByEmail(email)
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
                userService.existsByDocumentId(documentId)
        );
    }

    // ========================================
    // COUNT ALL
    // ========================================

    @GetMapping("/count")
    public ResponseEntity<Long> countAllUsers() {
        return ResponseEntity.ok(
                userService.countAllUsers()
        );
    }

    // ========================================
    // COUNT BY ROLE
    // ========================================

    @GetMapping("/count/role/{role}")
    public ResponseEntity<Long> countUsersByRole(
            @PathVariable("role") Role role
    ) {
        return ResponseEntity.ok(
                userService.countUsersByRole(role)
        );
    }

    // ========================================
    // COUNT BY STATUS
    // ========================================

    @GetMapping("/count/status")
    public ResponseEntity<Long> countUsersByActiveStatus(
            @RequestParam Boolean isActive
    ) {
        return ResponseEntity.ok(
                userService.countUsersByActiveStatus(
                        isActive
                )
        );
    }

    // ========================================
    // COUNT BY ROLE AND STATUS
    // ========================================

    @GetMapping("/count/role/{role}/status")
    public ResponseEntity<Long>
    countUsersByRoleAndActiveStatus(
            @PathVariable("role") Role role,
            @RequestParam Boolean isActive
    ) {
        return ResponseEntity.ok(
                userService.countUsersByRoleAndActiveStatus(
                        role,
                        isActive
                )
        );
    }
}
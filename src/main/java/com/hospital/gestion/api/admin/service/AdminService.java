package com.hospital.gestion.api.admin.service;

import com.hospital.gestion.api.admin.dto.AdminRequestDTO;
import com.hospital.gestion.api.admin.dto.AdminResponseDTO;
import com.hospital.gestion.api.admin.dto.AdminUpdateDTO;
import com.hospital.gestion.api.admin.entity.Admin;
import com.hospital.gestion.api.admin.mapper.AdminMapper;
import com.hospital.gestion.api.admin.repository.AdminRepository;
import com.hospital.gestion.api.admin.specification.AdminSpecification;
import com.hospital.gestion.api.common.dto.PageResponseDTO;
import com.hospital.gestion.api.common.enums.AdminLevel;
import com.hospital.gestion.api.common.enums.AdminPermission;
import com.hospital.gestion.api.common.enums.Role;
import com.hospital.gestion.api.common.exception.ConflictException;
import com.hospital.gestion.api.common.exception.ResourceNotFoundException;
import com.hospital.gestion.api.common.helper.HospitalEntityHelper;
import com.hospital.gestion.api.department.entity.Department;
import com.hospital.gestion.api.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {



    private final AdminRepository adminRepository;
    private final AdminMapper adminMapper;
    private final HospitalEntityHelper helper;


    private static final Set<String>
            ALLOWED_SORT_PROPERTIES = Set.of(
            "id",
            "adminLevel",
            "isSuperAdmin",
            "lastLogin",
            "createdAt",
            "updatedAt",
            "user.firstName",
            "user.lastName",
            "user.email",
            "user.documentId",
            "user.isActive",
            "department.departmentType"
    );

    // ============================================================
    // CREATE
    // ============================================================

    @Transactional
    public AdminResponseDTO createAdmin(
            AdminRequestDTO request
    ) {
        log.info(
                "Creating admin for user: {} with level: {}",
                request.userId(),
                request.adminLevel()
        );

        User user = helper.findUserByIdForUpdate(
                request.userId()
        );

        if (user.getRole() != Role.ADMIN) {
            throw new ConflictException(
                    "User must have ADMIN role"
            );
        }

        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new ConflictException(
                    "Inactive user cannot be registered "
                            + "as an admin"
            );
        }

        if (adminRepository.existsByUser_Id(user.getId())) {
            throw new ConflictException(
                    "An admin profile already exists for user: "
                            + user.getId()
            );
        }

        Department department =
                helper.resolveDepartment(
                        request.adminLevel(),
                        request.departmentId()
                );

        List<AdminPermission> permissions =
                helper.normalizePermissions(
                        request.permissions()
                );

        Admin admin = adminMapper.toEntity(
                request,
                user,
                department
        );

        admin.setPermissions(permissions);

        Admin savedAdmin =
                adminRepository.saveAndFlush(admin);

        log.info(
                "Admin created successfully with id: {}",
                savedAdmin.getId()
        );

        return adminMapper.toResponseDTO(savedAdmin);
    }

    // ============================================================
    // GET ALL
    // ============================================================

    @Transactional(readOnly = true)
    public List<AdminResponseDTO> getAllAdmins() {
        log.info("Fetching all admins");

        return adminMapper.toResponseDTOList(
                adminRepository.findAll()
        );
    }

    @Transactional(readOnly = true)
    public Page<AdminResponseDTO> getAllAdmins(
            Pageable pageable
    ) {
        helper.validatePageable(
                pageable,
                ALLOWED_SORT_PROPERTIES
        );

        log.info(
                "Fetching admins with pagination: {}",
                pageable
        );

        return adminRepository.findAll(pageable)
                .map(adminMapper::toResponseDTO);
    }

    // ============================================================
    // ORDERED
    // ============================================================

    @Transactional(readOnly = true)
    public List<AdminResponseDTO> getAllAdminsOrdered() {
        return adminMapper.toResponseDTOList(
                adminRepository
                        .findAllByOrderByUser_LastNameAscUser_FirstNameAsc()
        );
    }

    @Transactional(readOnly = true)
    public List<AdminResponseDTO> getActiveAdminsOrdered() {
        return adminMapper.toResponseDTOList(
                adminRepository
                        .findByUser_IsActiveTrueOrderByUser_LastNameAscUser_FirstNameAsc()
        );
    }

    // ============================================================
    // GET BY ID
    // ============================================================

    @Transactional(readOnly = true)
    public AdminResponseDTO getAdminById(Long id) {
        log.info("Fetching admin by id: {}", id);

        return adminMapper.toResponseDTO(
                helper.findAdminById(id)
        );
    }

    // ============================================================
    // GET BY USER
    // ============================================================

    @Transactional(readOnly = true)
    public AdminResponseDTO getAdminByUserId(
            Long userId
    ) {
        helper.validateId(userId, "User");

        Admin admin = adminRepository
                .findByUser_Id(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Admin not found for user: "
                                        + userId
                        )
                );

        return adminMapper.toResponseDTO(admin);
    }

    @Transactional(readOnly = true)
    public AdminResponseDTO getAdminByEmail(
            String email
    ) {
        String normalizedEmail =
                helper.normalizeRequiredText(email, "Email");

        Admin admin = adminRepository
                .findByUser_EmailIgnoreCase(
                        normalizedEmail
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Admin not found with email: "
                                        + normalizedEmail
                        )
                );

        return adminMapper.toResponseDTO(admin);
    }

    @Transactional(readOnly = true)
    public AdminResponseDTO getAdminByDocumentId(
            String documentId
    ) {
        String normalizedDocument =
                helper.normalizeRequiredText(
                        documentId,
                        "Document ID"
                );

        Admin admin = adminRepository
                .findByUser_DocumentIdIgnoreCase(
                        normalizedDocument
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Admin not found with document ID: "
                                        + normalizedDocument
                        )
                );

        return adminMapper.toResponseDTO(admin);
    }

    // ============================================================
    // BY ADMIN LEVEL
    // ============================================================

    @Transactional(readOnly = true)
    public List<AdminResponseDTO> getAdminsByLevel(
            AdminLevel adminLevel
    ) {
        helper.validateAdminLevel(adminLevel);

        return adminMapper.toResponseDTOList(
                adminRepository.findByAdminLevel(
                        adminLevel
                )
        );
    }

    @Transactional(readOnly = true)
    public Page<AdminResponseDTO> getAdminsByLevel(
            AdminLevel adminLevel,
            Pageable pageable
    ) {
        helper.validateAdminLevel(adminLevel);
        helper.validatePageable(
                pageable,
                ALLOWED_SORT_PROPERTIES
        );

        return adminRepository
                .findByAdminLevel(
                        adminLevel,
                        pageable
                )
                .map(adminMapper::toResponseDTO);
    }

    // ============================================================
    // BY DEPARTMENT
    // ============================================================

    @Transactional(readOnly = true)
    public List<AdminResponseDTO> getAdminsByDepartment(
            Long departmentId
    ) {
        helper.validateDepartmentExist(departmentId);

        return adminMapper.toResponseDTOList(
                adminRepository.findByDepartment_Id(
                        departmentId
                )
        );
    }

    @Transactional(readOnly = true)
    public Page<AdminResponseDTO> getAdminsByDepartment(
            Long departmentId,
            Pageable pageable
    ) {
        helper.validateDepartmentExist(departmentId);
        helper.validatePageable(
                pageable,
                ALLOWED_SORT_PROPERTIES
        );

        return adminRepository
                .findByDepartment_Id(
                        departmentId,
                        pageable
                )
                .map(adminMapper::toResponseDTO);
    }

    // ============================================================
    // BY DEPARTMENT AND LEVEL
    // ============================================================

    @Transactional(readOnly = true)
    public List<AdminResponseDTO>
    getAdminsByDepartmentAndLevel(
            Long departmentId,
            AdminLevel adminLevel
    ) {
        helper.validateDepartmentExist(departmentId);
        helper.validateAdminLevel(adminLevel);

        return adminMapper.toResponseDTOList(
                adminRepository
                        .findByDepartment_IdAndAdminLevel(
                                departmentId,
                                adminLevel
                        )
        );
    }

    @Transactional(readOnly = true)
    public Page<AdminResponseDTO>
    getAdminsByDepartmentAndLevel(
            Long departmentId,
            AdminLevel adminLevel,
            Pageable pageable
    ) {
        helper.validateDepartmentExist(departmentId);
        helper.validateAdminLevel(adminLevel);
        helper.validatePageable(
                pageable,
                ALLOWED_SORT_PROPERTIES
        );

        return adminRepository
                .findByDepartment_IdAndAdminLevel(
                        departmentId,
                        adminLevel,
                        pageable
                )
                .map(adminMapper::toResponseDTO);
    }

    // ============================================================
    // SUPER ADMINS
    // ============================================================

    @Transactional(readOnly = true)
    public List<AdminResponseDTO> getSuperAdmins(
            boolean isSuperAdmin
    ) {
        return adminMapper.toResponseDTOList(
                adminRepository.findByIsSuperAdmin(
                        isSuperAdmin
                )
        );
    }

    @Transactional(readOnly = true)
    public Page<AdminResponseDTO> getSuperAdmins(
            boolean isSuperAdmin,
            Pageable pageable
    ) {
        helper.validatePageable(
                pageable,
                ALLOWED_SORT_PROPERTIES
        );

        return adminRepository
                .findByIsSuperAdmin(
                        isSuperAdmin,
                        pageable
                )
                .map(adminMapper::toResponseDTO);
    }

    // ============================================================
    // ACTIVE STATUS
    // ============================================================

    @Transactional(readOnly = true)
    public List<AdminResponseDTO>
    getAdminsByActiveStatus(
            Boolean isActive
    ) {
        helper.validateBoolean(isActive, "Active status");

        return adminMapper.toResponseDTOList(
                adminRepository.findByUser_IsActive(
                        isActive
                )
        );
    }

    @Transactional(readOnly = true)
    public Page<AdminResponseDTO>
    getAdminsByActiveStatus(
            Boolean isActive,
            Pageable pageable
    ) {
        helper.validateBoolean(isActive, "Active status");
        helper.validatePageable(
                pageable,
                ALLOWED_SORT_PROPERTIES
        );

        return adminRepository
                .findByUser_IsActive(
                        isActive,
                        pageable
                )
                .map(adminMapper::toResponseDTO);
    }

    // ============================================================
    // PERMISSIONS
    // ============================================================

    @Transactional(readOnly = true)
    public List<AdminResponseDTO> getAdminsByPermission(
            AdminPermission permission
    ) {
        helper.validatePermission(permission);

        return adminMapper.toResponseDTOList(
                adminRepository.findByPermission(permission)
        );
    }

    @Transactional(readOnly = true)
    public Page<AdminResponseDTO> getAdminsByPermission(
            AdminPermission permission,
            Pageable pageable
    ) {
        helper.validatePermission(permission);
        helper.validatePageable(
                pageable,
                ALLOWED_SORT_PROPERTIES
        );

        return adminRepository
                .findByPermission(
                        permission,
                        pageable
                )
                .map(adminMapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public boolean hasPermission(
            Long adminId,
            AdminPermission permission
    ) {
        helper.validatePermission(permission);

        /*
         * También garantiza un 404 si el admin no existe.
         */
        helper.findAdminById(adminId);

        return adminRepository.hasPermission(
                adminId,
                permission
        );
    }

    @Transactional
    public AdminResponseDTO grantPermission(
            Long adminId,
            AdminPermission permission
    ) {
        helper.validatePermission(permission);

        Admin admin = helper.findAdminByIdForUpdate(adminId);

        if (admin.hasPermission(permission)) {
            throw new ConflictException(
                    "Admin already has permission: "
                            + permission
            );
        }

        admin.getPermissions().add(permission);
        admin.touch();

        Admin updatedAdmin =
                adminRepository.saveAndFlush(admin);

        log.info(
                "Permission {} granted to admin: {}",
                permission,
                adminId
        );

        return adminMapper.toResponseDTO(updatedAdmin);
    }

    @Transactional
    public AdminResponseDTO revokePermission(
            Long adminId,
            AdminPermission permission
    ) {
        helper.validatePermission(permission);

        Admin admin = helper.findAdminByIdForUpdate(adminId);

        if (!admin.removePermission(permission)) {
            throw new ConflictException(
                    "Admin does not have permission: "
                            + permission
            );
        }

        admin.touch();

        Admin updatedAdmin =
                adminRepository.saveAndFlush(admin);

        log.info(
                "Permission {} revoked from admin: {}",
                permission,
                adminId
        );

        return adminMapper.toResponseDTO(updatedAdmin);
    }

    // ============================================================
    // SEARCH
    // ============================================================

    @Transactional(readOnly = true)
    public List<AdminResponseDTO> searchAdmins(
            String text
    ) {
        String normalizedText =
                helper.normalizeRequiredText(
                        text,
                        "Search text"
                );

        return adminMapper.toResponseDTOList(
                adminRepository.searchAdmins(normalizedText)
        );
    }

    @Transactional(readOnly = true)
    public Page<AdminResponseDTO> searchAdmins(
            String text,
            Pageable pageable
    ) {
        String normalizedText =
                helper.normalizeRequiredText(
                        text,
                        "Search text"
                );

        helper.validatePageable(
                pageable,
                ALLOWED_SORT_PROPERTIES
        );

        return adminRepository
                .searchAdmins(
                        normalizedText,
                        pageable
                )
                .map(adminMapper::toResponseDTO);
    }

    // ============================================================
    // UPDATE
    // ============================================================
    @Transactional
    public AdminResponseDTO updateAdmin(
            Long id,
            AdminUpdateDTO request
    ) {
        log.info(
                "Updating admin with id: {}",
                id
        );

        Admin admin =
                helper.findAdminByIdForUpdate(id);

        Department department =
                helper.resolveDepartment(
                        request.adminLevel(),
                        request.departmentId()
                );

        adminMapper.updateEntity(
                admin,
                request,
                department
        );

        if (request.permissions() != null) {
            admin.setPermissions(
                    helper.normalizePermissions(
                            request.permissions()
                    )
            );
        }

        Admin updatedAdmin =
                adminRepository.saveAndFlush(admin);

        log.info(
                "Admin updated successfully with id: {}",
                updatedAdmin.getId()
        );

        return adminMapper.toResponseDTO(
                updatedAdmin
        );
    }



    // ============================================================
    // DELETE
    // ============================================================
    @Transactional
    public void deleteAdmin(Long id) {
        log.info(
                "Deleting admin with id: {}",
                id
        );

        Admin admin =
                helper.findAdminByIdForUpdate(id);

        adminRepository.delete(admin);

        log.info(
                "Admin deleted successfully with id: {}",
                id
        );
    }

    // ============================================================
    // EXISTS
    // ============================================================

    @Transactional(readOnly = true)
    public boolean existsByUserId(Long userId) {
        helper.validateId(userId, "User");

        return adminRepository.existsByUser_Id(userId);
    }

    // ============================================================
    // COUNT
    // ============================================================

    @Transactional(readOnly = true)
    public long countAllAdmins() {
        return adminRepository.count();
    }

    @Transactional(readOnly = true)
    public long countAdminsByLevel(
            AdminLevel adminLevel
    ) {
        helper.validateAdminLevel(adminLevel);

        return adminRepository.countByAdminLevel(
                adminLevel
        );
    }

    @Transactional(readOnly = true)
    public long countAdminsByDepartment(
            Long departmentId
    ) {
        helper.validateDepartmentExist(departmentId);

        return adminRepository.countByDepartment_Id(
                departmentId
        );
    }

    @Transactional(readOnly = true)
    public long countAdminsByDepartmentAndLevel(
            Long departmentId,
            AdminLevel adminLevel
    ) {
        helper.validateDepartmentExist(departmentId);
        helper.validateAdminLevel(adminLevel);

        return adminRepository
                .countByDepartment_IdAndAdminLevel(
                        departmentId,
                        adminLevel
                );
    }

    @Transactional(readOnly = true)
    public long countSuperAdmins(
            boolean isSuperAdmin
    ) {
        return adminRepository.countByIsSuperAdmin(
                isSuperAdmin
        );
    }

    @Transactional(readOnly = true)
    public long countAdminsByActiveStatus(
            Boolean isActive
    ) {
        helper.validateBoolean(isActive, "Active status");

        return adminRepository.countByUser_IsActive(
                isActive
        );
    }






    @Transactional(readOnly = true)
    public PageResponseDTO<AdminResponseDTO> searchAdmins(
            String text,
            AdminLevel adminLevel,
            Long departmentId,
            AdminPermission permission,
            Boolean isActive,
            Boolean isSuperAdmin,
            LocalDateTime lastLoginFrom,
            LocalDateTime lastLoginTo,
            Pageable pageable
    ) {
        helper.validatePageable(
                pageable,
                ALLOWED_SORT_PROPERTIES
        );

        if (departmentId != null) {
            helper.validateDepartmentExist(departmentId);
        }

        if (lastLoginFrom != null
                && lastLoginTo != null
                && lastLoginFrom.isAfter(lastLoginTo)) {
            throw new IllegalArgumentException(
                    "Start date cannot be after end date"
            );
        }

        Page<AdminResponseDTO> page =
                adminRepository.findAll(
                                AdminSpecification.withFilters(
                                        text,
                                        adminLevel,
                                        departmentId,
                                        permission,
                                        isActive,
                                        isSuperAdmin,
                                        lastLoginFrom,
                                        lastLoginTo
                                ),
                                pageable
                        )
                        .map(adminMapper::toResponseDTO);

        return PageResponseDTO.from(page);
    }










}
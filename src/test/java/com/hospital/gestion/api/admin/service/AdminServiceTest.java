package com.hospital.gestion.api.admin.service;

import com.hospital.gestion.api.admin.dto.AdminRequestDTO;
import com.hospital.gestion.api.admin.dto.AdminResponseDTO;
import com.hospital.gestion.api.admin.dto.AdminUpdateDTO;
import com.hospital.gestion.api.admin.entity.Admin;
import com.hospital.gestion.api.admin.mapper.AdminMapper;
import com.hospital.gestion.api.admin.repository.AdminRepository;
import com.hospital.gestion.api.common.enums.AdminLevel;
import com.hospital.gestion.api.common.enums.AdminPermission;
import com.hospital.gestion.api.common.enums.Role;
import com.hospital.gestion.api.common.exception.ConflictException;
import com.hospital.gestion.api.common.exception.ResourceNotFoundException;
import com.hospital.gestion.api.common.helper.HospitalEntityHelper;
import com.hospital.gestion.api.department.entity.Department;
import com.hospital.gestion.api.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private AdminMapper adminMapper;

    @Mock
    private HospitalEntityHelper helper;

    @InjectMocks
    private AdminService adminService;

    private User user;
    private Department department;
    private Admin admin;
    private AdminResponseDTO response;

    @BeforeEach
    void setUp() {
        user = mock(User.class);
        department = mock(Department.class);
        response = mock(AdminResponseDTO.class);

        lenient().when(user.getId())
                .thenReturn(1L);

        lenient().when(user.getRole())
                .thenReturn(Role.ADMIN);

        lenient().when(user.getIsActive())
                .thenReturn(true);

        admin = Admin.builder()
                .id(1L)
                .user(user)
                .adminLevel(AdminLevel.DEPARTMENT_ADMIN)
                .department(department)
                .permissions(
                        new ArrayList<>(
                                List.of(
                                        AdminPermission.VIEW_USERS
                                )
                        )
                )
                .isSuperAdmin(false)
                .build();
    }

    private AdminRequestDTO request() {
        return new AdminRequestDTO(
                1L,
                AdminLevel.DEPARTMENT_ADMIN,
                1L,
                List.of(AdminPermission.VIEW_USERS)
        );
    }

    @Test
    void createAdminValidatesAndSavesProfile() {
        AdminRequestDTO request = request();

        List<AdminPermission> permissions =
                new ArrayList<>(
                        List.of(
                                AdminPermission.VIEW_USERS
                        )
                );

        when(helper.findUserByIdForUpdate(1L))
                .thenReturn(user);

        when(adminRepository.existsByUser_Id(1L))
                .thenReturn(false);

        when(
                helper.resolveDepartment(
                        AdminLevel.DEPARTMENT_ADMIN,
                        1L
                )
        ).thenReturn(department);

        when(
                helper.normalizePermissions(
                        request.permissions()
                )
        ).thenReturn(permissions);

        when(
                adminMapper.toEntity(
                        request,
                        user,
                        department
                )
        ).thenReturn(admin);

        when(adminRepository.saveAndFlush(admin))
                .thenReturn(admin);

        when(adminMapper.toResponseDTO(admin))
                .thenReturn(response);

        AdminResponseDTO result =
                adminService.createAdmin(request);

        assertSame(response, result);
        assertEquals(permissions, admin.getPermissions());

        verify(helper).findUserByIdForUpdate(1L);

        verify(helper).resolveDepartment(
                AdminLevel.DEPARTMENT_ADMIN,
                1L
        );

        verify(adminRepository).saveAndFlush(admin);
    }

    @Test
    void createAdminRejectsIncorrectUserRole() {
        AdminRequestDTO request = request();

        when(user.getRole()).thenReturn(Role.DOCTOR);

        when(helper.findUserByIdForUpdate(1L))
                .thenReturn(user);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> adminService.createAdmin(request)
        );

        assertEquals(
                "User must have ADMIN role",
                exception.getMessage()
        );

        verify(adminRepository, never())
                .existsByUser_Id(anyLong());

        verify(adminRepository, never())
                .saveAndFlush(any(Admin.class));
    }

    @Test
    void createAdminRejectsInactiveUser() {
        AdminRequestDTO request = request();

        when(user.getIsActive()).thenReturn(false);

        when(helper.findUserByIdForUpdate(1L))
                .thenReturn(user);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> adminService.createAdmin(request)
        );

        assertEquals(
                "Inactive user cannot be registered as an admin",
                exception.getMessage()
        );

        verify(helper, never()).resolveDepartment(
                any(),
                any()
        );

        verify(adminRepository, never())
                .saveAndFlush(any(Admin.class));
    }

    @Test
    void createAdminRejectsExistingProfile() {
        AdminRequestDTO request = request();

        when(helper.findUserByIdForUpdate(1L))
                .thenReturn(user);

        when(adminRepository.existsByUser_Id(1L))
                .thenReturn(true);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> adminService.createAdmin(request)
        );

        assertEquals(
                "An admin profile already exists for user: 1",
                exception.getMessage()
        );

        verify(helper, never()).resolveDepartment(
                any(),
                any()
        );

        verify(adminRepository, never())
                .saveAndFlush(any(Admin.class));
    }

    @Test
    void getAdminByUserThrowsWhenMissing() {
        when(adminRepository.findByUser_Id(1L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> adminService.getAdminByUserId(1L)
        );

        assertEquals(
                "Admin not found for user: 1",
                exception.getMessage()
        );

        verify(helper).validateId(1L, "User");
    }

    @Test
    void hasPermissionValidatesAdminAndPermission() {
        when(
                adminRepository.hasPermission(
                        1L,
                        AdminPermission.VIEW_USERS
                )
        ).thenReturn(true);

        when(helper.findAdminById(1L))
                .thenReturn(admin);

        boolean result = adminService.hasPermission(
                1L,
                AdminPermission.VIEW_USERS
        );

        assertTrue(result);

        verify(helper).validatePermission(
                AdminPermission.VIEW_USERS
        );

        verify(helper).findAdminById(1L);

        verify(adminRepository).hasPermission(
                1L,
                AdminPermission.VIEW_USERS
        );
    }

    @Test
    void grantPermissionAddsAndSavesPermission() {
        when(helper.findAdminByIdForUpdate(1L))
                .thenReturn(admin);

        when(adminRepository.saveAndFlush(admin))
                .thenReturn(admin);

        when(adminMapper.toResponseDTO(admin))
                .thenReturn(response);

        AdminResponseDTO result =
                adminService.grantPermission(
                        1L,
                        AdminPermission.MANAGE_USERS
                );

        assertSame(response, result);

        assertTrue(
                admin.getPermissions().contains(
                        AdminPermission.MANAGE_USERS
                )
        );

        assertNotNull(admin.getUpdatedAt());

        verify(helper).validatePermission(
                AdminPermission.MANAGE_USERS
        );

        verify(adminRepository).saveAndFlush(admin);
    }

    @Test
    void grantPermissionRejectsExistingPermission() {
        when(helper.findAdminByIdForUpdate(1L))
                .thenReturn(admin);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> adminService.grantPermission(
                        1L,
                        AdminPermission.VIEW_USERS
                )
        );

        assertEquals(
                "Admin already has permission: VIEW_USERS",
                exception.getMessage()
        );

        verify(adminRepository, never())
                .saveAndFlush(any(Admin.class));
    }

    @Test
    void revokePermissionRemovesAndSavesPermission() {
        when(helper.findAdminByIdForUpdate(1L))
                .thenReturn(admin);

        when(adminRepository.saveAndFlush(admin))
                .thenReturn(admin);

        when(adminMapper.toResponseDTO(admin))
                .thenReturn(response);

        AdminResponseDTO result =
                adminService.revokePermission(
                        1L,
                        AdminPermission.VIEW_USERS
                );

        assertSame(response, result);

        assertFalse(
                admin.getPermissions().contains(
                        AdminPermission.VIEW_USERS
                )
        );

        assertNotNull(admin.getUpdatedAt());

        verify(adminRepository).saveAndFlush(admin);
    }

    @Test
    void revokePermissionRejectsMissingPermission() {
        when(helper.findAdminByIdForUpdate(1L))
                .thenReturn(admin);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> adminService.revokePermission(
                        1L,
                        AdminPermission.MANAGE_USERS
                )
        );

        assertEquals(
                "Admin does not have permission: MANAGE_USERS",
                exception.getMessage()
        );

        verify(adminRepository, never())
                .saveAndFlush(any(Admin.class));
    }

    @Test
    void updateAdminUsesLockAndReplacesPermissions() {
        List<AdminPermission> requestedPermissions =
                List.of(
                        AdminPermission.VIEW_USERS,
                        AdminPermission.MANAGE_USERS
                );

        List<AdminPermission> normalizedPermissions =
                new ArrayList<>(requestedPermissions);

        AdminUpdateDTO request =
                new AdminUpdateDTO(
                        AdminLevel.SUPER_ADMIN,
                        null,
                        requestedPermissions
                );

        when(helper.findAdminByIdForUpdate(1L))
                .thenReturn(admin);

        when(
                helper.resolveDepartment(
                        AdminLevel.SUPER_ADMIN,
                        null
                )
        ).thenReturn(null);

        when(
                helper.normalizePermissions(
                        requestedPermissions
                )
        ).thenReturn(normalizedPermissions);

        when(adminRepository.saveAndFlush(admin))
                .thenReturn(admin);

        when(adminMapper.toResponseDTO(admin))
                .thenReturn(response);

        AdminResponseDTO result =
                adminService.updateAdmin(
                        1L,
                        request
                );

        assertSame(response, result);

        assertEquals(
                normalizedPermissions,
                admin.getPermissions()
        );

        verify(adminMapper).updateEntity(
                admin,
                request,
                null
        );

        verify(adminRepository).saveAndFlush(admin);
    }

    @Test
    void deleteAdminUsesLockAndDeletesProfile() {
        when(helper.findAdminByIdForUpdate(1L))
                .thenReturn(admin);

        adminService.deleteAdmin(1L);

        verify(helper).findAdminByIdForUpdate(1L);
        verify(adminRepository).delete(admin);
    }

    @Test
    void existsByUserIdValidatesAndQueriesRepository() {
        when(adminRepository.existsByUser_Id(1L))
                .thenReturn(true);

        boolean result = adminService.existsByUserId(1L);

        assertTrue(result);

        verify(helper).validateId(1L, "User");
        verify(adminRepository).existsByUser_Id(1L);
    }

    @Test
    void countByDepartmentAndLevelValidatesArguments() {
        when(
                adminRepository
                        .countByDepartment_IdAndAdminLevel(
                                1L,
                                AdminLevel.DEPARTMENT_ADMIN
                        )
        ).thenReturn(2L);

        long result =
                adminService
                        .countAdminsByDepartmentAndLevel(
                                1L,
                                AdminLevel.DEPARTMENT_ADMIN
                        );

        assertEquals(2L, result);

        verify(helper).validateDepartmentExist(1L);

        verify(helper).validateAdminLevel(
                AdminLevel.DEPARTMENT_ADMIN
        );
    }

    @Test
    void searchAdminsRejectsInvalidLastLoginRange() {
        Pageable pageable = PageRequest.of(0, 20);

        LocalDateTime from =
                LocalDateTime.of(
                        2026,
                        9,
                        30,
                        23,
                        59
                );

        LocalDateTime to =
                LocalDateTime.of(
                        2026,
                        8,
                        1,
                        0,
                        0
                );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> adminService.searchAdmins(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        from,
                        to,
                        pageable
                )
        );

        assertEquals(
                "Start date cannot be after end date",
                exception.getMessage()
        );

        verify(helper).validatePageable(
                eq(pageable),
                anySet()
        );

        verify(adminRepository, never()).findAll(
                any(org.springframework.data.jpa.domain.Specification.class),
                any(Pageable.class)
        );
    }
}

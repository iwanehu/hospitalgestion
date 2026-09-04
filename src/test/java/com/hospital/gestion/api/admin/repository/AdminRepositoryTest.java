package com.hospital.gestion.api.admin.repository;

import com.hospital.gestion.api.admin.entity.Admin;
import com.hospital.gestion.api.common.enums.AdminLevel;
import com.hospital.gestion.api.common.enums.AdminPermission;
import com.hospital.gestion.api.common.enums.DepartmentType;
import com.hospital.gestion.api.common.enums.Role;
import com.hospital.gestion.api.department.entity.Department;
import com.hospital.gestion.api.department.repository.DepartmentRepository;
import com.hospital.gestion.api.user.entity.User;
import com.hospital.gestion.api.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class AdminRepositoryTest {

    @Container
    static final PostgreSQLContainer POSTGRES =
            new PostgreSQLContainer(
                    "postgres:18.4-alpine"
            )
                    .withDatabaseName("hospital_test")
                    .withUsername("hospital_test")
                    .withPassword("hospital_test");

    @DynamicPropertySource
    static void configureDatabase(
            DynamicPropertyRegistry registry
    ) {
        registry.add(
                "spring.datasource.url",
                POSTGRES::getJdbcUrl
        );
        registry.add(
                "spring.datasource.username",
                POSTGRES::getUsername
        );
        registry.add(
                "spring.datasource.password",
                POSTGRES::getPassword
        );
        registry.add(
                "spring.datasource.driver-class-name",
                POSTGRES::getDriverClassName
        );
        registry.add(
                "spring.jpa.hibernate.ddl-auto",
                () -> "validate"
        );
        registry.add(
                "spring.flyway.enabled",
                () -> true
        );
    }

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void savePersistsAdminAndSynchronizesSuperAdminStatus() {
        Admin saved =
                saveAdmin(
                        "carlos",
                        "Carlos",
                        "Administrador",
                        AdminLevel.SUPER_ADMIN,
                        null,
                        true,
                        List.of(
                                AdminPermission.VIEW_USERS,
                                AdminPermission.MANAGE_USERS
                        )
                );

        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());
        assertEquals(
                "Carlos Administrador",
                saved.getFullName()
        );
        assertEquals(
                AdminLevel.SUPER_ADMIN,
                saved.getAdminLevel()
        );
        assertTrue(saved.isSuperAdmin());
        assertEquals(2, saved.getPermissions().size());
    }

    @Test
    void userLookupsAndOwnershipWorkCorrectly() {
        Department department =
                saveDepartment(
                        DepartmentType.CARDIOLOGY
                );

        Admin admin =
                saveAdmin(
                        "carlos",
                        "Carlos",
                        "Administrador",
                        AdminLevel.DEPARTMENT_ADMIN,
                        department,
                        true,
                        List.of(AdminPermission.VIEW_USERS)
                );

        Long userId = admin.getUser().getId();

        assertTrue(
                adminRepository.existsByUser_Id(userId)
        );

        assertEquals(
                admin.getId(),
                adminRepository.findByUser_Id(userId)
                        .orElseThrow()
                        .getId()
        );

        assertEquals(
                admin.getId(),
                adminRepository
                        .findByUser_EmailIgnoreCase(
                                "CARLOS@HOSPITAL.TEST"
                        )
                        .orElseThrow()
                        .getId()
        );

        assertEquals(
                admin.getId(),
                adminRepository
                        .findByUser_DocumentIdIgnoreCase(
                                "carlos-doc"
                        )
                        .orElseThrow()
                        .getId()
        );

        assertTrue(
                adminRepository.existsByIdAndUser_Id(
                        admin.getId(),
                        userId
                )
        );

        assertFalse(
                adminRepository.existsByIdAndUser_Id(
                        admin.getId(),
                        999999L
                )
        );
    }

    @Test
    void findByAdminLevelReturnsListAndPage() {
        Department department =
                saveDepartment(
                        DepartmentType.CARDIOLOGY
                );

        saveAdmin(
                "department-one",
                "Department",
                "One",
                AdminLevel.DEPARTMENT_ADMIN,
                department,
                true,
                List.of(AdminPermission.VIEW_USERS)
        );

        saveAdmin(
                "department-two",
                "Department",
                "Two",
                AdminLevel.DEPARTMENT_ADMIN,
                department,
                true,
                List.of(AdminPermission.VIEW_DOCTORS)
        );

        saveAdmin(
                "system",
                "System",
                "Admin",
                AdminLevel.SYSTEM_ADMIN,
                null,
                true,
                List.of(AdminPermission.MANAGE_SETTINGS)
        );

        List<Admin> list =
                adminRepository.findByAdminLevel(
                        AdminLevel.DEPARTMENT_ADMIN
                );

        Page<Admin> page =
                adminRepository.findByAdminLevel(
                        AdminLevel.DEPARTMENT_ADMIN,
                        firstPage()
                );

        assertEquals(2, list.size());
        assertEquals(1, page.getNumberOfElements());
        assertEquals(2L, page.getTotalElements());
        assertEquals(2, page.getTotalPages());
    }

    @Test
    void findByDepartmentReturnsListAndPage() {
        Department cardiology =
                saveDepartment(
                        DepartmentType.CARDIOLOGY
                );

        Department emergency =
                saveDepartment(
                        DepartmentType.EMERGENCY
                );

        saveAdmin(
                "cardio-one",
                "Cardio",
                "One",
                AdminLevel.DEPARTMENT_ADMIN,
                cardiology,
                true,
                List.of(AdminPermission.VIEW_DOCTORS)
        );

        saveAdmin(
                "cardio-two",
                "Cardio",
                "Two",
                AdminLevel.DEPARTMENT_ADMIN,
                cardiology,
                true,
                List.of(AdminPermission.VIEW_NURSES)
        );

        saveAdmin(
                "emergency",
                "Emergency",
                "Admin",
                AdminLevel.DEPARTMENT_ADMIN,
                emergency,
                true,
                List.of(AdminPermission.VIEW_PATIENTS)
        );

        List<Admin> list =
                adminRepository.findByDepartment_Id(
                        cardiology.getId()
                );

        Page<Admin> page =
                adminRepository.findByDepartment_Id(
                        cardiology.getId(),
                        firstPage()
                );

        assertEquals(2, list.size());
        assertEquals(2L, page.getTotalElements());
    }

    @Test
    void findByDepartmentAndLevelReturnsListAndPage() {
        Department department =
                saveDepartment(
                        DepartmentType.CARDIOLOGY
                );

        saveAdmin(
                "department",
                "Department",
                "Admin",
                AdminLevel.DEPARTMENT_ADMIN,
                department,
                true,
                List.of(AdminPermission.VIEW_USERS)
        );

        saveAdmin(
                "system",
                "System",
                "Admin",
                AdminLevel.SYSTEM_ADMIN,
                department,
                true,
                List.of(AdminPermission.MANAGE_SETTINGS)
        );

        List<Admin> list =
                adminRepository
                        .findByDepartment_IdAndAdminLevel(
                                department.getId(),
                                AdminLevel.DEPARTMENT_ADMIN
                        );

        Page<Admin> page =
                adminRepository
                        .findByDepartment_IdAndAdminLevel(
                                department.getId(),
                                AdminLevel.DEPARTMENT_ADMIN,
                                firstPage()
                        );

        assertEquals(1, list.size());
        assertEquals(1L, page.getTotalElements());
    }

    @Test
    void findBySuperAdminReturnsListAndPage() {
        saveAdmin(
                "super",
                "Super",
                "Admin",
                AdminLevel.SUPER_ADMIN,
                null,
                true,
                List.of(AdminPermission.MANAGE_USERS)
        );

        saveAdmin(
                "system",
                "System",
                "Admin",
                AdminLevel.SYSTEM_ADMIN,
                null,
                true,
                List.of(AdminPermission.MANAGE_SETTINGS)
        );

        List<Admin> list =
                adminRepository.findByIsSuperAdmin(true);

        Page<Admin> page =
                adminRepository.findByIsSuperAdmin(
                        true,
                        firstPage()
                );

        assertEquals(1, list.size());
        assertEquals(1L, page.getTotalElements());
        assertEquals(
                AdminLevel.SUPER_ADMIN,
                list.getFirst().getAdminLevel()
        );
    }

    @Test
    void findByActiveUserReturnsListAndPage() {
        saveAdmin(
                "active",
                "Active",
                "Admin",
                AdminLevel.SYSTEM_ADMIN,
                null,
                true,
                List.of(AdminPermission.VIEW_USERS)
        );

        saveAdmin(
                "inactive",
                "Inactive",
                "Admin",
                AdminLevel.SYSTEM_ADMIN,
                null,
                false,
                List.of(AdminPermission.VIEW_USERS)
        );

        List<Admin> list =
                adminRepository.findByUser_IsActive(true);

        Page<Admin> page =
                adminRepository.findByUser_IsActive(
                        true,
                        firstPage()
                );

        assertEquals(1, list.size());
        assertEquals(1L, page.getTotalElements());
        assertTrue(list.getFirst().getUser().getIsActive());
    }

    @Test
    void findByPermissionReturnsListAndPageWithoutDuplicates() {
        saveAdmin(
                "first",
                "First",
                "Admin",
                AdminLevel.SYSTEM_ADMIN,
                null,
                true,
                List.of(
                        AdminPermission.VIEW_USERS,
                        AdminPermission.MANAGE_USERS
                )
        );

        saveAdmin(
                "second",
                "Second",
                "Admin",
                AdminLevel.SYSTEM_ADMIN,
                null,
                true,
                List.of(AdminPermission.VIEW_USERS)
        );

        saveAdmin(
                "third",
                "Third",
                "Admin",
                AdminLevel.SYSTEM_ADMIN,
                null,
                true,
                List.of(AdminPermission.VIEW_DOCTORS)
        );

        List<Admin> list =
                adminRepository.findByPermission(
                        AdminPermission.VIEW_USERS
                );

        Page<Admin> page =
                adminRepository.findByPermission(
                        AdminPermission.VIEW_USERS,
                        firstPage()
                );

        assertEquals(2, list.size());
        assertEquals(1, page.getNumberOfElements());
        assertEquals(2L, page.getTotalElements());
        assertEquals(2, page.getTotalPages());
    }

    @Test
    void hasPermissionReturnsExpectedValue() {
        Admin admin =
                saveAdmin(
                        "carlos",
                        "Carlos",
                        "Administrador",
                        AdminLevel.SYSTEM_ADMIN,
                        null,
                        true,
                        List.of(
                                AdminPermission.VIEW_USERS,
                                AdminPermission.MANAGE_USERS
                        )
                );

        assertTrue(
                adminRepository.hasPermission(
                        admin.getId(),
                        AdminPermission.VIEW_USERS
                )
        );

        assertFalse(
                adminRepository.hasPermission(
                        admin.getId(),
                        AdminPermission.VIEW_DOCTORS
                )
        );
    }

    @Test
    void searchAdminsSearchesUserFields() {
        Admin expected =
                saveAdmin(
                        "carlos",
                        "Carlos",
                        "Administrador Especial",
                        AdminLevel.SYSTEM_ADMIN,
                        null,
                        true,
                        List.of(AdminPermission.VIEW_USERS)
                );

        saveAdmin(
                "ana",
                "Ana",
                "General",
                AdminLevel.SYSTEM_ADMIN,
                null,
                true,
                List.of(AdminPermission.VIEW_DOCTORS)
        );

        List<Admin> list =
                adminRepository.searchAdmins(
                        "carlos"
                );

        Page<Admin> page =
                adminRepository.searchAdmins(
                        "CARLOS-DOC",
                        firstPage()
                );

        assertEquals(1, list.size());
        assertEquals(expected.getId(), list.getFirst().getId());
        assertEquals(1L, page.getTotalElements());
        assertEquals(
                expected.getId(),
                page.getContent().getFirst().getId()
        );
    }

    @Test
    void findAllReturnsAdminsOrderedByName() {
        saveAdmin(
                "carlos",
                "Carlos",
                "Zuluaga",
                AdminLevel.SYSTEM_ADMIN,
                null,
                true,
                List.of()
        );

        saveAdmin(
                "pedro",
                "Pedro",
                "Alonso",
                AdminLevel.SYSTEM_ADMIN,
                null,
                true,
                List.of()
        );

        saveAdmin(
                "ana",
                "Ana",
                "Alonso",
                AdminLevel.SYSTEM_ADMIN,
                null,
                true,
                List.of()
        );

        List<Admin> result =
                adminRepository
                        .findAllByOrderByUser_LastNameAscUser_FirstNameAsc();

        assertEquals(3, result.size());
        assertEquals(
                "Ana",
                result.get(0).getUser().getFirstName()
        );
        assertEquals(
                "Pedro",
                result.get(1).getUser().getFirstName()
        );
        assertEquals(
                "Carlos",
                result.get(2).getUser().getFirstName()
        );
    }

    @Test
    void findActiveAdminsReturnsOnlyActiveAndOrdered() {
        saveAdmin(
                "carlos",
                "Carlos",
                "Zuluaga",
                AdminLevel.SYSTEM_ADMIN,
                null,
                true,
                List.of()
        );

        saveAdmin(
                "ana",
                "Ana",
                "Alonso",
                AdminLevel.SYSTEM_ADMIN,
                null,
                true,
                List.of()
        );

        saveAdmin(
                "inactive",
                "Inactive",
                "Aardvark",
                AdminLevel.SYSTEM_ADMIN,
                null,
                false,
                List.of()
        );

        List<Admin> result =
                adminRepository
                        .findByUser_IsActiveTrueOrderByUser_LastNameAscUser_FirstNameAsc();

        assertEquals(2, result.size());
        assertEquals(
                "Ana",
                result.get(0).getUser().getFirstName()
        );
        assertEquals(
                "Carlos",
                result.get(1).getUser().getFirstName()
        );

        assertTrue(
                result.stream().allMatch(admin ->
                        admin.getUser().getIsActive()
                )
        );
    }

    @Test
    void countMethodsReturnCorrectValues() {
        Department cardiology =
                saveDepartment(
                        DepartmentType.CARDIOLOGY
                );

        Department emergency =
                saveDepartment(
                        DepartmentType.EMERGENCY
                );

        saveAdmin(
                "cardio-active",
                "Cardio",
                "Active",
                AdminLevel.DEPARTMENT_ADMIN,
                cardiology,
                true,
                List.of(AdminPermission.VIEW_DOCTORS)
        );

        saveAdmin(
                "cardio-inactive",
                "Cardio",
                "Inactive",
                AdminLevel.DEPARTMENT_ADMIN,
                cardiology,
                false,
                List.of(AdminPermission.VIEW_NURSES)
        );

        saveAdmin(
                "emergency",
                "Emergency",
                "Admin",
                AdminLevel.DEPARTMENT_ADMIN,
                emergency,
                true,
                List.of(AdminPermission.VIEW_PATIENTS)
        );

        saveAdmin(
                "super",
                "Super",
                "Admin",
                AdminLevel.SUPER_ADMIN,
                null,
                true,
                List.of(AdminPermission.MANAGE_USERS)
        );

        assertEquals(
                3L,
                adminRepository.countByAdminLevel(
                        AdminLevel.DEPARTMENT_ADMIN
                )
        );

        assertEquals(
                2L,
                adminRepository.countByDepartment_Id(
                        cardiology.getId()
                )
        );

        assertEquals(
                2L,
                adminRepository
                        .countByDepartment_IdAndAdminLevel(
                                cardiology.getId(),
                                AdminLevel.DEPARTMENT_ADMIN
                        )
        );

        assertEquals(
                1L,
                adminRepository.countByIsSuperAdmin(true)
        );

        assertEquals(
                3L,
                adminRepository.countByUser_IsActive(true)
        );
    }

    @Test
    void findByIdForUpdateLocksAdminAndLoadsEntityGraph() {
        Department department =
                saveDepartment(
                        DepartmentType.CARDIOLOGY
                );

        Admin saved =
                saveAdmin(
                        "carlos",
                        "Carlos",
                        "Administrador",
                        AdminLevel.DEPARTMENT_ADMIN,
                        department,
                        true,
                        List.of(AdminPermission.VIEW_USERS)
                );

        Long adminId = saved.getId();

        entityManager.clear();

        Admin result =
                adminRepository.findByIdForUpdate(adminId)
                        .orElseThrow();

        assertEquals(adminId, result.getId());

        assertEquals(
                LockModeType.PESSIMISTIC_WRITE,
                entityManager.getLockMode(result)
        );

        assertTrue(Hibernate.isInitialized(result.getUser()));
        assertTrue(
                Hibernate.isInitialized(
                        result.getDepartment()
                )
        );
    }

    private PageRequest firstPage() {
        return PageRequest.of(
                0,
                1,
                Sort.by(
                        "user.lastName",
                        "user.firstName"
                ).ascending()
        );
    }

    private Department saveDepartment(
            DepartmentType type
    ) {
        return departmentRepository.saveAndFlush(
                Department.builder()
                        .departmentType(type)
                        .location(type + " location")
                        .phoneExtension("100")
                        .description(type + " department")
                        .isActive(true)
                        .build()
        );
    }

    private Admin saveAdmin(
            String suffix,
            String firstName,
            String lastName,
            AdminLevel level,
            Department department,
            boolean active,
            List<AdminPermission> permissions
    ) {
        User user =
                userRepository.saveAndFlush(
                        User.builder()
                                .role(Role.ADMIN)
                                .email(
                                        suffix
                                                + "@hospital.test"
                                )
                                .password(
                                        "$2a$10$encodedPassword"
                                )
                                .isActive(active)
                                .documentId(
                                        suffix.toUpperCase()
                                                + "-DOC"
                                )
                                .firstName(firstName)
                                .lastName(lastName)
                                .phone("600000000")
                                .build()
                );

        return adminRepository.saveAndFlush(
                Admin.builder()
                        .user(user)
                        .adminLevel(level)
                        .department(department)
                        .permissions(
                                new ArrayList<>(permissions)
                        )
                        .build()
        );
    }
}

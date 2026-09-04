package com.hospital.gestion.api.nurse.repository;

import com.hospital.gestion.api.common.enums.DepartmentType;
import com.hospital.gestion.api.common.enums.NurseSpecialty;
import com.hospital.gestion.api.common.enums.Role;
import com.hospital.gestion.api.common.enums.ShiftType;
import com.hospital.gestion.api.department.entity.Department;
import com.hospital.gestion.api.department.repository.DepartmentRepository;
import com.hospital.gestion.api.nurse.entity.Nurse;
import com.hospital.gestion.api.user.entity.User;
import com.hospital.gestion.api.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class NurseRepositoryTest {

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
    private NurseRepository nurseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void savePersistsNurseAndGeneratesMetadata() {
        Department department =
                saveDepartment(
                        DepartmentType.EMERGENCY
                );

        Nurse saved =
                saveNurse(
                        "maria",
                        "Maria",
                        "Enfermera",
                        "NUR-001",
                        NurseSpecialty.EMERGENCY,
                        ShiftType.ROTATING,
                        department,
                        true,
                        true
                );

        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());
        assertEquals("Maria Enfermera", saved.getFullName());
        assertEquals(
                NurseSpecialty.EMERGENCY,
                saved.getSpecialty()
        );
        assertEquals(ShiftType.ROTATING, saved.getShiftType());
        assertTrue(saved.getIsChargeNurse());
        assertEquals(8, saved.getYearsOfExperience());
    }

    @Test
    void userLookupsAndOwnershipWorkCorrectly() {
        Department department =
                saveDepartment(
                        DepartmentType.EMERGENCY
                );

        Nurse nurse =
                saveNurse(
                        "maria",
                        "Maria",
                        "Enfermera",
                        "NUR-001",
                        NurseSpecialty.EMERGENCY,
                        ShiftType.ROTATING,
                        department,
                        true,
                        true
                );

        Long userId = nurse.getUser().getId();

        assertEquals(
                nurse.getId(),
                nurseRepository.findByUser_Id(userId)
                        .orElseThrow()
                        .getId()
        );

        assertTrue(
                nurseRepository.existsByUser_Id(userId)
        );

        assertEquals(
                nurse.getId(),
                nurseRepository
                        .findByUser_EmailIgnoreCase(
                                "MARIA@HOSPITAL.TEST"
                        )
                        .orElseThrow()
                        .getId()
        );

        assertEquals(
                nurse.getId(),
                nurseRepository
                        .findByUser_DocumentIdIgnoreCase(
                                "maria-doc"
                        )
                        .orElseThrow()
                        .getId()
        );

        assertTrue(
                nurseRepository.existsByIdAndUser_Id(
                        nurse.getId(),
                        userId
                )
        );

        assertFalse(
                nurseRepository.existsByIdAndUser_Id(
                        nurse.getId(),
                        999999L
                )
        );
    }

    @Test
    void findAndExistsByLicenseIgnoreCaseWorkCorrectly() {
        Department department =
                saveDepartment(
                        DepartmentType.EMERGENCY
                );

        Nurse nurse =
                saveNurse(
                        "maria",
                        "Maria",
                        "Enfermera",
                        "NUR-001",
                        NurseSpecialty.EMERGENCY,
                        ShiftType.ROTATING,
                        department,
                        true,
                        true
                );

        Optional<Nurse> result =
                nurseRepository
                        .findByLicenseNumberIgnoreCase(
                                "nur-001"
                        );

        assertTrue(result.isPresent());
        assertEquals(nurse.getId(), result.get().getId());

        assertTrue(
                nurseRepository
                        .existsByLicenseNumberIgnoreCase(
                                "nur-001"
                        )
        );

        assertFalse(
                nurseRepository
                        .existsByLicenseNumberIgnoreCase(
                                "NUR-999"
                        )
        );
    }

    @Test
    void findByDepartmentReturnsListAndPage() {
        Department emergency =
                saveDepartment(
                        DepartmentType.EMERGENCY
                );

        Department cardiology =
                saveDepartment(
                        DepartmentType.CARDIOLOGY
                );

        saveNurse(
                "maria",
                "Maria",
                "Enfermera",
                "NUR-001",
                NurseSpecialty.EMERGENCY,
                ShiftType.MORNING,
                emergency,
                true,
                true
        );

        saveNurse(
                "ana",
                "Ana",
                "Enfermera",
                "NUR-002",
                NurseSpecialty.GENERAL,
                ShiftType.NIGHT,
                emergency,
                true,
                false
        );

        saveNurse(
                "laura",
                "Laura",
                "Enfermera",
                "NUR-003",
                NurseSpecialty.CARDIOLOGY,
                ShiftType.AFTERNOON,
                cardiology,
                true,
                false
        );

        List<Nurse> list =
                nurseRepository.findByDepartment_Id(
                        emergency.getId()
                );

        Page<Nurse> page =
                nurseRepository.findByDepartment_Id(
                        emergency.getId(),
                        firstPage()
                );

        assertEquals(2, list.size());
        assertEquals(2L, page.getTotalElements());
        assertEquals(2, page.getTotalPages());
    }

    @Test
    void findBySpecialtyReturnsListAndPage() {
        Department department =
                saveDepartment(
                        DepartmentType.EMERGENCY
                );

        saveNurse(
                "maria",
                "Maria",
                "Enfermera",
                "NUR-001",
                NurseSpecialty.EMERGENCY,
                ShiftType.MORNING,
                department,
                true,
                true
        );

        saveNurse(
                "ana",
                "Ana",
                "Enfermera",
                "NUR-002",
                NurseSpecialty.EMERGENCY,
                ShiftType.NIGHT,
                department,
                true,
                false
        );

        saveNurse(
                "laura",
                "Laura",
                "Enfermera",
                "NUR-003",
                NurseSpecialty.GENERAL,
                ShiftType.AFTERNOON,
                department,
                true,
                false
        );

        List<Nurse> list =
                nurseRepository.findBySpecialty(
                        NurseSpecialty.EMERGENCY
                );

        Page<Nurse> page =
                nurseRepository.findBySpecialty(
                        NurseSpecialty.EMERGENCY,
                        firstPage()
                );

        assertEquals(2, list.size());
        assertEquals(2L, page.getTotalElements());
    }

    @Test
    void findByShiftReturnsListAndPage() {
        Department department =
                saveDepartment(
                        DepartmentType.EMERGENCY
                );

        saveNurse(
                "maria",
                "Maria",
                "Enfermera",
                "NUR-001",
                NurseSpecialty.EMERGENCY,
                ShiftType.NIGHT,
                department,
                true,
                true
        );

        saveNurse(
                "ana",
                "Ana",
                "Enfermera",
                "NUR-002",
                NurseSpecialty.GENERAL,
                ShiftType.NIGHT,
                department,
                true,
                false
        );

        saveNurse(
                "laura",
                "Laura",
                "Enfermera",
                "NUR-003",
                NurseSpecialty.GENERAL,
                ShiftType.MORNING,
                department,
                true,
                false
        );

        List<Nurse> list =
                nurseRepository.findByShiftType(
                        ShiftType.NIGHT
                );

        Page<Nurse> page =
                nurseRepository.findByShiftType(
                        ShiftType.NIGHT,
                        firstPage()
                );

        assertEquals(2, list.size());
        assertEquals(2L, page.getTotalElements());
    }

    @Test
    void findByChargeNurseReturnsListAndPage() {
        Department department =
                saveDepartment(
                        DepartmentType.EMERGENCY
                );

        saveNurse(
                "maria",
                "Maria",
                "Enfermera",
                "NUR-001",
                NurseSpecialty.EMERGENCY,
                ShiftType.ROTATING,
                department,
                true,
                true
        );

        saveNurse(
                "ana",
                "Ana",
                "Enfermera",
                "NUR-002",
                NurseSpecialty.GENERAL,
                ShiftType.MORNING,
                department,
                true,
                false
        );

        List<Nurse> list =
                nurseRepository.findByIsChargeNurse(true);

        Page<Nurse> page =
                nurseRepository.findByIsChargeNurse(
                        true,
                        firstPage()
                );

        assertEquals(1, list.size());
        assertEquals(1L, page.getTotalElements());
        assertTrue(list.getFirst().getIsChargeNurse());
    }

    @Test
    void findByActiveStatusReturnsListAndPage() {
        Department department =
                saveDepartment(
                        DepartmentType.EMERGENCY
                );

        saveNurse(
                "active",
                "Active",
                "Nurse",
                "NUR-001",
                NurseSpecialty.EMERGENCY,
                ShiftType.MORNING,
                department,
                true,
                false
        );

        saveNurse(
                "inactive",
                "Inactive",
                "Nurse",
                "NUR-002",
                NurseSpecialty.EMERGENCY,
                ShiftType.NIGHT,
                department,
                false,
                false
        );

        List<Nurse> list =
                nurseRepository.findByUser_IsActive(true);

        Page<Nurse> page =
                nurseRepository.findByUser_IsActive(
                        true,
                        firstPage()
                );

        assertEquals(1, list.size());
        assertEquals(1L, page.getTotalElements());
        assertTrue(list.getFirst().getUser().getIsActive());
    }

    @Test
    void findByDepartmentAndSpecialtyReturnsListAndPage() {
        Department department =
                saveDepartment(
                        DepartmentType.EMERGENCY
                );

        saveNurse(
                "matching",
                "Matching",
                "Nurse",
                "NUR-001",
                NurseSpecialty.EMERGENCY,
                ShiftType.MORNING,
                department,
                true,
                false
        );

        saveNurse(
                "general",
                "General",
                "Nurse",
                "NUR-002",
                NurseSpecialty.GENERAL,
                ShiftType.MORNING,
                department,
                true,
                false
        );

        List<Nurse> list =
                nurseRepository
                        .findByDepartment_IdAndSpecialty(
                                department.getId(),
                                NurseSpecialty.EMERGENCY
                        );

        Page<Nurse> page =
                nurseRepository
                        .findByDepartment_IdAndSpecialty(
                                department.getId(),
                                NurseSpecialty.EMERGENCY,
                                firstPage()
                        );

        assertEquals(1, list.size());
        assertEquals(1L, page.getTotalElements());
    }

    @Test
    void findByDepartmentAndShiftReturnsListAndPage() {
        Department department =
                saveDepartment(
                        DepartmentType.EMERGENCY
                );

        saveNurse(
                "morning",
                "Morning",
                "Nurse",
                "NUR-001",
                NurseSpecialty.EMERGENCY,
                ShiftType.MORNING,
                department,
                true,
                false
        );

        saveNurse(
                "night",
                "Night",
                "Nurse",
                "NUR-002",
                NurseSpecialty.EMERGENCY,
                ShiftType.NIGHT,
                department,
                true,
                false
        );

        List<Nurse> list =
                nurseRepository
                        .findByDepartment_IdAndShiftType(
                                department.getId(),
                                ShiftType.MORNING
                        );

        Page<Nurse> page =
                nurseRepository
                        .findByDepartment_IdAndShiftType(
                                department.getId(),
                                ShiftType.MORNING,
                                firstPage()
                        );

        assertEquals(1, list.size());
        assertEquals(1L, page.getTotalElements());
    }

    @Test
    void findBySpecialtyAndShiftReturnsListAndPage() {
        Department department =
                saveDepartment(
                        DepartmentType.EMERGENCY
                );

        saveNurse(
                "matching",
                "Matching",
                "Nurse",
                "NUR-001",
                NurseSpecialty.EMERGENCY,
                ShiftType.NIGHT,
                department,
                true,
                false
        );

        saveNurse(
                "morning",
                "Morning",
                "Nurse",
                "NUR-002",
                NurseSpecialty.EMERGENCY,
                ShiftType.MORNING,
                department,
                true,
                false
        );

        List<Nurse> list =
                nurseRepository
                        .findBySpecialtyAndShiftType(
                                NurseSpecialty.EMERGENCY,
                                ShiftType.NIGHT
                        );

        Page<Nurse> page =
                nurseRepository
                        .findBySpecialtyAndShiftType(
                                NurseSpecialty.EMERGENCY,
                                ShiftType.NIGHT,
                                firstPage()
                        );

        assertEquals(1, list.size());
        assertEquals(1L, page.getTotalElements());
    }

    @Test
    void findBySpecialtyAndActiveReturnsListAndPage() {
        Department department =
                saveDepartment(
                        DepartmentType.EMERGENCY
                );

        saveNurse(
                "active",
                "Active",
                "Nurse",
                "NUR-001",
                NurseSpecialty.EMERGENCY,
                ShiftType.MORNING,
                department,
                true,
                false
        );

        saveNurse(
                "inactive",
                "Inactive",
                "Nurse",
                "NUR-002",
                NurseSpecialty.EMERGENCY,
                ShiftType.NIGHT,
                department,
                false,
                false
        );

        List<Nurse> list =
                nurseRepository
                        .findBySpecialtyAndUser_IsActive(
                                NurseSpecialty.EMERGENCY,
                                true
                        );

        Page<Nurse> page =
                nurseRepository
                        .findBySpecialtyAndUser_IsActive(
                                NurseSpecialty.EMERGENCY,
                                true,
                                firstPage()
                        );

        assertEquals(1, list.size());
        assertEquals(1L, page.getTotalElements());
    }

    @Test
    void departmentAndShiftActiveFiltersWorkCorrectly() {
        Department department =
                saveDepartment(
                        DepartmentType.EMERGENCY
                );

        saveNurse(
                "matching",
                "Matching",
                "Nurse",
                "NUR-001",
                NurseSpecialty.EMERGENCY,
                ShiftType.NIGHT,
                department,
                true,
                false
        );

        saveNurse(
                "inactive",
                "Inactive",
                "Nurse",
                "NUR-002",
                NurseSpecialty.EMERGENCY,
                ShiftType.NIGHT,
                department,
                false,
                false
        );

        List<Nurse> departmentList =
                nurseRepository
                        .findByDepartment_IdAndUser_IsActive(
                                department.getId(),
                                true
                        );

        Page<Nurse> departmentPage =
                nurseRepository
                        .findByDepartment_IdAndUser_IsActive(
                                department.getId(),
                                true,
                                firstPage()
                        );

        List<Nurse> shiftList =
                nurseRepository
                        .findByShiftTypeAndUser_IsActive(
                                ShiftType.NIGHT,
                                true
                        );

        Page<Nurse> shiftPage =
                nurseRepository
                        .findByShiftTypeAndUser_IsActive(
                                ShiftType.NIGHT,
                                true,
                                firstPage()
                        );

        assertEquals(1, departmentList.size());
        assertEquals(1L, departmentPage.getTotalElements());
        assertEquals(1, shiftList.size());
        assertEquals(1L, shiftPage.getTotalElements());
    }

    @Test
    void searchNursesSearchesUserAndLicenseFields() {
        Department department =
                saveDepartment(
                        DepartmentType.EMERGENCY
                );

        Nurse expected =
                saveNurse(
                        "maria",
                        "Maria",
                        "Enfermera Especial",
                        "NUR-12345",
                        NurseSpecialty.EMERGENCY,
                        ShiftType.ROTATING,
                        department,
                        true,
                        true
                );

        saveNurse(
                "ana",
                "Ana",
                "General",
                "NUR-99999",
                NurseSpecialty.GENERAL,
                ShiftType.MORNING,
                department,
                true,
                false
        );

        List<Nurse> list =
                nurseRepository.searchNurses(
                        "maria enfermera"
                );

        Page<Nurse> page =
                nurseRepository.searchNurses(
                        "NUR-12345",
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
    void orderedQueriesSortAndFilterNurses() {
        Department department =
                saveDepartment(
                        DepartmentType.EMERGENCY
                );

        saveNurse(
                "carlos",
                "Carlos",
                "Zuluaga",
                "NUR-001",
                NurseSpecialty.EMERGENCY,
                ShiftType.MORNING,
                department,
                true,
                false
        );

        saveNurse(
                "ana",
                "Ana",
                "Alonso",
                "NUR-002",
                NurseSpecialty.EMERGENCY,
                ShiftType.NIGHT,
                department,
                true,
                false
        );

        saveNurse(
                "inactive",
                "Inactive",
                "Aardvark",
                "NUR-003",
                NurseSpecialty.EMERGENCY,
                ShiftType.NIGHT,
                department,
                false,
                false
        );

        List<Nurse> all =
                nurseRepository
                        .findAllByOrderByUser_LastNameAscUser_FirstNameAsc();

        List<Nurse> active =
                nurseRepository
                        .findByUser_IsActiveTrueOrderByUser_LastNameAscUser_FirstNameAsc();

        assertEquals(3, all.size());
        assertEquals(
                "Inactive",
                all.get(0).getUser().getFirstName()
        );
        assertEquals(
                "Ana",
                all.get(1).getUser().getFirstName()
        );
        assertEquals(
                "Carlos",
                all.get(2).getUser().getFirstName()
        );

        assertEquals(2, active.size());
        assertEquals(
                "Ana",
                active.get(0).getUser().getFirstName()
        );
        assertEquals(
                "Carlos",
                active.get(1).getUser().getFirstName()
        );
    }

    @Test
    void countMethodsReturnCorrectValues() {
        Department emergency =
                saveDepartment(
                        DepartmentType.EMERGENCY
                );

        Department cardiology =
                saveDepartment(
                        DepartmentType.CARDIOLOGY
                );

        saveNurse(
                "active-emergency",
                "Active",
                "Emergency",
                "NUR-001",
                NurseSpecialty.EMERGENCY,
                ShiftType.NIGHT,
                emergency,
                true,
                true
        );

        saveNurse(
                "inactive-emergency",
                "Inactive",
                "Emergency",
                "NUR-002",
                NurseSpecialty.EMERGENCY,
                ShiftType.NIGHT,
                emergency,
                false,
                false
        );

        saveNurse(
                "cardiology",
                "Cardiology",
                "Nurse",
                "NUR-003",
                NurseSpecialty.CARDIOLOGY,
                ShiftType.MORNING,
                cardiology,
                true,
                false
        );

        assertEquals(
                2L,
                nurseRepository.countByDepartment_Id(
                        emergency.getId()
                )
        );

        assertEquals(
                2L,
                nurseRepository.countBySpecialty(
                        NurseSpecialty.EMERGENCY
                )
        );

        assertEquals(
                2L,
                nurseRepository.countByShiftType(
                        ShiftType.NIGHT
                )
        );

        assertEquals(
                2L,
                nurseRepository.countByUser_IsActive(true)
        );

        assertEquals(
                1L,
                nurseRepository.countByIsChargeNurse(true)
        );

        assertEquals(
                2L,
                nurseRepository
                        .countByDepartment_IdAndShiftType(
                                emergency.getId(),
                                ShiftType.NIGHT
                        )
        );

        assertEquals(
                1L,
                nurseRepository
                        .countBySpecialtyAndUser_IsActive(
                                NurseSpecialty.EMERGENCY,
                                true
                        )
        );
    }

    @Test
    void findByIdForUpdateReturnsNurseWithPessimisticLock() {
        Department department =
                saveDepartment(
                        DepartmentType.EMERGENCY
                );

        Nurse saved =
                saveNurse(
                        "maria",
                        "Maria",
                        "Enfermera",
                        "NUR-001",
                        NurseSpecialty.EMERGENCY,
                        ShiftType.ROTATING,
                        department,
                        true,
                        true
                );

        Long nurseId = saved.getId();

        entityManager.clear();

        Nurse result =
                nurseRepository.findByIdForUpdate(
                                nurseId
                        )
                        .orElseThrow();

        assertEquals(nurseId, result.getId());

        assertEquals(
                LockModeType.PESSIMISTIC_WRITE,
                entityManager.getLockMode(result)
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

    private Nurse saveNurse(
            String suffix,
            String firstName,
            String lastName,
            String license,
            NurseSpecialty specialty,
            ShiftType shift,
            Department department,
            boolean active,
            boolean chargeNurse
    ) {
        User user =
                userRepository.saveAndFlush(
                        User.builder()
                                .role(Role.NURSE)
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

        return nurseRepository.saveAndFlush(
                Nurse.builder()
                        .user(user)
                        .department(department)
                        .licenseNumber(license)
                        .specialty(specialty)
                        .shiftType(shift)
                        .yearsOfExperience(8)
                        .hireDate(
                                LocalDate.of(
                                        2020, 6, 15
                                )
                        )
                        .biography("Repository test")
                        .maxPatientsPerShift(7)
                        .isChargeNurse(chargeNurse)
                        .vacationDaysAvailable(20)
                        .build()
        );
    }
}

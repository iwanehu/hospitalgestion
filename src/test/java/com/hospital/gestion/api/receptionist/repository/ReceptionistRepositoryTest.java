package com.hospital.gestion.api.receptionist.repository;

import com.hospital.gestion.api.common.enums.DepartmentType;
import com.hospital.gestion.api.common.enums.Role;
import com.hospital.gestion.api.common.enums.ShiftType;
import com.hospital.gestion.api.department.entity.Department;
import com.hospital.gestion.api.department.repository.DepartmentRepository;
import com.hospital.gestion.api.receptionist.entity.Receptionist;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class ReceptionistRepositoryTest {

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
    private ReceptionistRepository receptionistRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void savePersistsReceptionistAndGeneratesMetadata() {
        Department department =
                saveDepartment(
                        DepartmentType.CARDIOLOGY
                );

        Receptionist saved =
                saveReceptionist(
                        "ana",
                        "Ana",
                        "Recepcionista",
                        "DESK-01",
                        ShiftType.MORNING,
                        department,
                        true
                );

        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());
        assertEquals(
                "Ana Recepcionista",
                saved.getFullName()
        );
        assertEquals("DESK-01", saved.getDeskNumber());
        assertEquals(ShiftType.MORNING, saved.getShiftType());
        assertEquals(department.getId(),
                saved.getDepartment().getId());
    }

    @Test
    void userLookupsAndOwnershipWorkCorrectly() {
        Department department =
                saveDepartment(
                        DepartmentType.CARDIOLOGY
                );

        Receptionist receptionist =
                saveReceptionist(
                        "ana",
                        "Ana",
                        "Recepcionista",
                        "DESK-01",
                        ShiftType.MORNING,
                        department,
                        true
                );

        Long userId = receptionist.getUser().getId();

        assertTrue(
                receptionistRepository.existsByUser_Id(
                        userId
                )
        );

        assertEquals(
                receptionist.getId(),
                receptionistRepository
                        .findByUser_Id(userId)
                        .orElseThrow()
                        .getId()
        );

        assertEquals(
                receptionist.getId(),
                receptionistRepository
                        .findByUser_EmailIgnoreCase(
                                "ANA@HOSPITAL.TEST"
                        )
                        .orElseThrow()
                        .getId()
        );

        assertEquals(
                receptionist.getId(),
                receptionistRepository
                        .findByUser_DocumentIdIgnoreCase(
                                "ana-doc"
                        )
                        .orElseThrow()
                        .getId()
        );

        assertTrue(
                receptionistRepository.existsByIdAndUser_Id(
                        receptionist.getId(),
                        userId
                )
        );

        assertFalse(
                receptionistRepository.existsByIdAndUser_Id(
                        receptionist.getId(),
                        999999L
                )
        );
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

        saveReceptionist(
                "ana",
                "Ana",
                "Recepcionista",
                "DESK-01",
                ShiftType.MORNING,
                cardiology,
                true
        );

        saveReceptionist(
                "maria",
                "Maria",
                "Recepcionista",
                "DESK-02",
                ShiftType.NIGHT,
                cardiology,
                true
        );

        saveReceptionist(
                "laura",
                "Laura",
                "Recepcionista",
                "DESK-03",
                ShiftType.ROTATING,
                emergency,
                true
        );

        List<Receptionist> list =
                receptionistRepository.findByDepartment_Id(
                        cardiology.getId()
                );

        Page<Receptionist> page =
                receptionistRepository.findByDepartment_Id(
                        cardiology.getId(),
                        firstPage()
                );

        assertEquals(2, list.size());
        assertEquals(1, page.getNumberOfElements());
        assertEquals(2L, page.getTotalElements());
        assertEquals(2, page.getTotalPages());
    }

    @Test
    void findByShiftReturnsListAndPage() {
        Department department =
                saveDepartment(
                        DepartmentType.CARDIOLOGY
                );

        saveReceptionist(
                "ana",
                "Ana",
                "Recepcionista",
                "DESK-01",
                ShiftType.NIGHT,
                department,
                true
        );

        saveReceptionist(
                "maria",
                "Maria",
                "Recepcionista",
                "DESK-02",
                ShiftType.NIGHT,
                department,
                true
        );

        saveReceptionist(
                "laura",
                "Laura",
                "Recepcionista",
                "DESK-03",
                ShiftType.MORNING,
                department,
                true
        );

        List<Receptionist> list =
                receptionistRepository.findByShiftType(
                        ShiftType.NIGHT
                );

        Page<Receptionist> page =
                receptionistRepository.findByShiftType(
                        ShiftType.NIGHT,
                        firstPage()
                );

        assertEquals(2, list.size());
        assertEquals(2L, page.getTotalElements());
    }

    @Test
    void findByDepartmentAndShiftReturnsListAndPage() {
        Department department =
                saveDepartment(
                        DepartmentType.CARDIOLOGY
                );

        saveReceptionist(
                "matching",
                "Matching",
                "Receptionist",
                "DESK-01",
                ShiftType.NIGHT,
                department,
                true
        );

        saveReceptionist(
                "morning",
                "Morning",
                "Receptionist",
                "DESK-02",
                ShiftType.MORNING,
                department,
                true
        );

        List<Receptionist> list =
                receptionistRepository
                        .findByDepartment_IdAndShiftType(
                                department.getId(),
                                ShiftType.NIGHT
                        );

        Page<Receptionist> page =
                receptionistRepository
                        .findByDepartment_IdAndShiftType(
                                department.getId(),
                                ShiftType.NIGHT,
                                firstPage()
                        );

        assertEquals(1, list.size());
        assertEquals(1L, page.getTotalElements());
    }

    @Test
    void findByActiveStatusReturnsListAndPage() {
        Department department =
                saveDepartment(
                        DepartmentType.CARDIOLOGY
                );

        saveReceptionist(
                "active",
                "Active",
                "Receptionist",
                "DESK-01",
                ShiftType.MORNING,
                department,
                true
        );

        saveReceptionist(
                "inactive",
                "Inactive",
                "Receptionist",
                "DESK-02",
                ShiftType.NIGHT,
                department,
                false
        );

        List<Receptionist> list =
                receptionistRepository
                        .findByUser_IsActive(true);

        Page<Receptionist> page =
                receptionistRepository
                        .findByUser_IsActive(
                                true,
                                firstPage()
                        );

        assertEquals(1, list.size());
        assertEquals(1L, page.getTotalElements());
        assertTrue(list.getFirst().getUser().getIsActive());
    }

    @Test
    void findByDeskNumberIgnoresCase() {
        Department department =
                saveDepartment(
                        DepartmentType.CARDIOLOGY
                );

        Receptionist expected =
                saveReceptionist(
                        "ana",
                        "Ana",
                        "Recepcionista",
                        "DESK-01",
                        ShiftType.MORNING,
                        department,
                        true
                );

        saveReceptionist(
                "maria",
                "Maria",
                "Recepcionista",
                "DESK-02",
                ShiftType.NIGHT,
                department,
                true
        );

        List<Receptionist> result =
                receptionistRepository
                        .findByDeskNumberIgnoreCase(
                                "desk-01"
                        );

        assertEquals(1, result.size());
        assertEquals(expected.getId(), result.getFirst().getId());
    }

    @Test
    void deskNumberSearchCanBeRestrictedToDepartment() {
        Department cardiology =
                saveDepartment(
                        DepartmentType.CARDIOLOGY
                );

        Department emergency =
                saveDepartment(
                        DepartmentType.EMERGENCY
                );

        Receptionist expected =
                saveReceptionist(
                        "ana",
                        "Ana",
                        "Recepcionista",
                        "DESK-01",
                        ShiftType.MORNING,
                        cardiology,
                        true
                );

        saveReceptionist(
                "maria",
                "Maria",
                "Recepcionista",
                "DESK-01",
                ShiftType.NIGHT,
                emergency,
                true
        );

        List<Receptionist> result =
                receptionistRepository
                        .findByDepartment_IdAndDeskNumberIgnoreCase(
                                cardiology.getId(),
                                "desk-01"
                        );

        assertEquals(1, result.size());
        assertEquals(expected.getId(), result.getFirst().getId());
    }

    @Test
    void searchReceptionistsSearchesUserAndDeskFields() {
        Department department =
                saveDepartment(
                        DepartmentType.CARDIOLOGY
                );

        Receptionist expected =
                saveReceptionist(
                        "ana",
                        "Ana",
                        "Recepcionista Especial",
                        "FRONT-01",
                        ShiftType.MORNING,
                        department,
                        true
                );

        saveReceptionist(
                "maria",
                "Maria",
                "General",
                "DESK-99",
                ShiftType.NIGHT,
                department,
                true
        );

        List<Receptionist> list =
                receptionistRepository.searchReceptionists(
                        "ana"
                );

        Page<Receptionist> page =
                receptionistRepository.searchReceptionists(
                        "FRONT-01",
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
    void findAllReturnsReceptionistsOrderedByName() {
        Department department =
                saveDepartment(
                        DepartmentType.CARDIOLOGY
                );

        saveReceptionist(
                "carlos",
                "Carlos",
                "Zuluaga",
                "DESK-01",
                ShiftType.MORNING,
                department,
                true
        );

        saveReceptionist(
                "pedro",
                "Pedro",
                "Alonso",
                "DESK-02",
                ShiftType.NIGHT,
                department,
                true
        );

        saveReceptionist(
                "ana",
                "Ana",
                "Alonso",
                "DESK-03",
                ShiftType.ROTATING,
                department,
                true
        );

        List<Receptionist> result =
                receptionistRepository
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
    void findActiveReceptionistsReturnsOnlyActiveAndOrdered() {
        Department department =
                saveDepartment(
                        DepartmentType.CARDIOLOGY
                );

        saveReceptionist(
                "carlos",
                "Carlos",
                "Zuluaga",
                "DESK-01",
                ShiftType.MORNING,
                department,
                true
        );

        saveReceptionist(
                "ana",
                "Ana",
                "Alonso",
                "DESK-02",
                ShiftType.NIGHT,
                department,
                true
        );

        saveReceptionist(
                "inactive",
                "Inactive",
                "Aardvark",
                "DESK-03",
                ShiftType.ROTATING,
                department,
                false
        );

        List<Receptionist> result =
                receptionistRepository
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
                result.stream().allMatch(receptionist ->
                        receptionist.getUser().getIsActive()
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

        saveReceptionist(
                "cardio-night",
                "Cardio",
                "Night",
                "DESK-01",
                ShiftType.NIGHT,
                cardiology,
                true
        );

        saveReceptionist(
                "cardio-morning",
                "Cardio",
                "Morning",
                "DESK-02",
                ShiftType.MORNING,
                cardiology,
                false
        );

        saveReceptionist(
                "emergency-night",
                "Emergency",
                "Night",
                "DESK-03",
                ShiftType.NIGHT,
                emergency,
                true
        );

        assertEquals(
                2L,
                receptionistRepository.countByDepartment_Id(
                        cardiology.getId()
                )
        );

        assertEquals(
                2L,
                receptionistRepository.countByShiftType(
                        ShiftType.NIGHT
                )
        );

        assertEquals(
                1L,
                receptionistRepository
                        .countByDepartment_IdAndShiftType(
                                cardiology.getId(),
                                ShiftType.NIGHT
                        )
        );

        assertEquals(
                2L,
                receptionistRepository
                        .countByUser_IsActive(true)
        );
    }

    @Test
    void findByIdForUpdateReturnsReceptionistWithLock() {
        Department department =
                saveDepartment(
                        DepartmentType.CARDIOLOGY
                );

        Receptionist saved =
                saveReceptionist(
                        "ana",
                        "Ana",
                        "Recepcionista",
                        "DESK-01",
                        ShiftType.MORNING,
                        department,
                        true
                );

        Long receptionistId = saved.getId();

        entityManager.clear();

        Receptionist result =
                receptionistRepository.findByIdForUpdate(
                                receptionistId
                        )
                        .orElseThrow();

        assertEquals(receptionistId, result.getId());

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

    private Receptionist saveReceptionist(
            String suffix,
            String firstName,
            String lastName,
            String deskNumber,
            ShiftType shiftType,
            Department department,
            boolean active
    ) {
        User user =
                userRepository.saveAndFlush(
                        User.builder()
                                .role(Role.RECEPTIONIST)
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

        return receptionistRepository.saveAndFlush(
                Receptionist.builder()
                        .user(user)
                        .department(department)
                        .deskNumber(deskNumber)
                        .shiftType(shiftType)
                        .build()
        );
    }
}

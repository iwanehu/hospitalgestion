package com.hospital.gestion.api.user.repository;

import com.hospital.gestion.api.common.enums.Role;
import com.hospital.gestion.api.user.entity.User;
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
class UserRepositoryTest {

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
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void savePersistsUserAndGeneratesMetadata() {
        User saved =
                saveUser(
                        "admin",
                        "Carlos",
                        "Administrador",
                        Role.ADMIN,
                        true
                );

        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());
        assertEquals(Role.ADMIN, saved.getRole());
        assertEquals(
                "admin@hospital.test",
                saved.getEmail()
        );
        assertTrue(saved.getIsActive());
    }

    @Test
    void findAndExistsByEmailIgnoreCaseWorkCorrectly() {
        User expected =
                saveUser(
                        "doctor",
                        "Laura",
                        "Medica",
                        Role.DOCTOR,
                        true
                );

        Optional<User> result =
                userRepository.findByEmailIgnoreCase(
                        "DOCTOR@HOSPITAL.TEST"
                );

        assertTrue(result.isPresent());
        assertEquals(expected.getId(), result.get().getId());

        assertTrue(
                userRepository.existsByEmailIgnoreCase(
                        "DOCTOR@HOSPITAL.TEST"
                )
        );

        assertFalse(
                userRepository.existsByEmailIgnoreCase(
                        "unknown@hospital.test"
                )
        );
    }

    @Test
    void findAndExistsByDocumentIgnoreCaseWorkCorrectly() {
        User expected =
                saveUser(
                        "doctor",
                        "Laura",
                        "Medica",
                        Role.DOCTOR,
                        true
                );

        Optional<User> result =
                userRepository.findByDocumentIdIgnoreCase(
                        "doctor-doc"
                );

        assertTrue(result.isPresent());
        assertEquals(expected.getId(), result.get().getId());

        assertTrue(
                userRepository.existsByDocumentIdIgnoreCase(
                        "doctor-doc"
                )
        );

        assertFalse(
                userRepository.existsByDocumentIdIgnoreCase(
                        "UNKNOWN-DOC"
                )
        );
    }

    @Test
    void findByRoleReturnsListAndPage() {
        saveUser(
                "doctor-one",
                "Laura",
                "Medica",
                Role.DOCTOR,
                true
        );

        saveUser(
                "doctor-two",
                "Carlos",
                "Medico",
                Role.DOCTOR,
                true
        );

        saveUser(
                "nurse",
                "Maria",
                "Enfermera",
                Role.NURSE,
                true
        );

        List<User> list =
                userRepository.findByRole(Role.DOCTOR);

        Page<User> page =
                userRepository.findByRole(
                        Role.DOCTOR,
                        firstPage()
                );

        assertEquals(2, list.size());
        assertEquals(1, page.getNumberOfElements());
        assertEquals(2L, page.getTotalElements());
        assertEquals(2, page.getTotalPages());
    }

    @Test
    void findByActiveStatusReturnsListAndPage() {
        saveUser(
                "active-one",
                "Active",
                "One",
                Role.DOCTOR,
                true
        );

        saveUser(
                "active-two",
                "Active",
                "Two",
                Role.NURSE,
                true
        );

        saveUser(
                "inactive",
                "Inactive",
                "User",
                Role.PATIENT,
                false
        );

        List<User> list =
                userRepository.findByIsActive(true);

        Page<User> page =
                userRepository.findByIsActive(
                        true,
                        firstPage()
                );

        assertEquals(2, list.size());
        assertEquals(2L, page.getTotalElements());

        assertTrue(
                list.stream().allMatch(
                        User::getIsActive
                )
        );
    }

    @Test
    void findByRoleAndActiveStatusReturnsListAndPage() {
        saveUser(
                "active-doctor",
                "Active",
                "Doctor",
                Role.DOCTOR,
                true
        );

        saveUser(
                "inactive-doctor",
                "Inactive",
                "Doctor",
                Role.DOCTOR,
                false
        );

        saveUser(
                "active-nurse",
                "Active",
                "Nurse",
                Role.NURSE,
                true
        );

        List<User> list =
                userRepository.findByRoleAndIsActive(
                        Role.DOCTOR,
                        true
                );

        Page<User> page =
                userRepository.findByRoleAndIsActive(
                        Role.DOCTOR,
                        true,
                        firstPage()
                );

        assertEquals(1, list.size());
        assertEquals(1L, page.getTotalElements());
        assertEquals(
                "active-doctor@hospital.test",
                list.getFirst().getEmail()
        );
    }

    @Test
    void searchUsersSearchesAllSupportedFields() {
        User expected =
                saveUser(
                        "pedro",
                        "Pedro",
                        "Paciente Especial",
                        Role.PATIENT,
                        true
                );

        saveUser(
                "ana",
                "Ana",
                "Recepcionista",
                Role.RECEPTIONIST,
                true
        );

        List<User> byFullName =
                userRepository.searchUsers(
                        "pedro paciente"
                );

        List<User> byEmail =
                userRepository.searchUsers(
                        "PEDRO@HOSPITAL"
                );

        List<User> byDocument =
                userRepository.searchUsers(
                        "PEDRO-DOC"
                );

        Page<User> page =
                userRepository.searchUsers(
                        "pedro",
                        firstPage()
                );

        assertEquals(1, byFullName.size());
        assertEquals(expected.getId(),
                byFullName.getFirst().getId());

        assertEquals(1, byEmail.size());
        assertEquals(1, byDocument.size());

        assertEquals(1L, page.getTotalElements());
        assertEquals(expected.getId(),
                page.getContent().getFirst().getId());
    }

    @Test
    void findAllReturnsUsersOrderedByLastAndFirstName() {
        saveUser(
                "carlos",
                "Carlos",
                "Zuluaga",
                Role.ADMIN,
                true
        );

        saveUser(
                "pedro",
                "Pedro",
                "Alonso",
                Role.PATIENT,
                true
        );

        saveUser(
                "ana",
                "Ana",
                "Alonso",
                Role.RECEPTIONIST,
                true
        );

        List<User> result =
                userRepository
                        .findAllByOrderByLastNameAscFirstNameAsc();

        assertEquals(3, result.size());
        assertEquals("Ana", result.get(0).getFirstName());
        assertEquals("Pedro", result.get(1).getFirstName());
        assertEquals("Carlos", result.get(2).getFirstName());
    }

    @Test
    void findActiveUsersReturnsOnlyActiveAndOrdered() {
        saveUser(
                "carlos",
                "Carlos",
                "Zuluaga",
                Role.ADMIN,
                true
        );

        saveUser(
                "ana",
                "Ana",
                "Alonso",
                Role.RECEPTIONIST,
                true
        );

        saveUser(
                "inactive",
                "Inactive",
                "Aardvark",
                Role.PATIENT,
                false
        );

        List<User> result =
                userRepository
                        .findByIsActiveTrueOrderByLastNameAscFirstNameAsc();

        assertEquals(2, result.size());
        assertEquals("Ana", result.get(0).getFirstName());
        assertEquals("Carlos", result.get(1).getFirstName());

        assertTrue(
                result.stream().allMatch(
                        User::getIsActive
                )
        );
    }

    @Test
    void countMethodsReturnCorrectValues() {
        saveUser(
                "active-doctor",
                "Active",
                "Doctor",
                Role.DOCTOR,
                true
        );

        saveUser(
                "inactive-doctor",
                "Inactive",
                "Doctor",
                Role.DOCTOR,
                false
        );

        saveUser(
                "active-nurse",
                "Active",
                "Nurse",
                Role.NURSE,
                true
        );

        assertEquals(
                2L,
                userRepository.countByRole(Role.DOCTOR)
        );

        assertEquals(
                2L,
                userRepository.countByIsActive(true)
        );

        assertEquals(
                1L,
                userRepository.countByRoleAndIsActive(
                        Role.DOCTOR,
                        true
                )
        );
    }

    @Test
    void findByIdForUpdateReturnsUserWithPessimisticLock() {
        User saved =
                saveUser(
                        "admin",
                        "Carlos",
                        "Administrador",
                        Role.ADMIN,
                        true
                );

        Long userId = saved.getId();

        entityManager.clear();

        User result =
                userRepository.findByIdForUpdate(userId)
                        .orElseThrow();

        assertEquals(userId, result.getId());

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
                        "lastName",
                        "firstName"
                ).ascending()
        );
    }

    private User saveUser(
            String suffix,
            String firstName,
            String lastName,
            Role role,
            boolean active
    ) {
        return userRepository.saveAndFlush(
                User.builder()
                        .role(role)
                        .email(
                                suffix + "@hospital.test"
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
    }
}

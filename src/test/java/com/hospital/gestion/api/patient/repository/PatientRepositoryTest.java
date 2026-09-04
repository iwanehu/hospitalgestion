package com.hospital.gestion.api.patient.repository;

import com.hospital.gestion.api.common.enums.BloodType;
import com.hospital.gestion.api.common.enums.Role;
import com.hospital.gestion.api.patient.entity.EmergencyContact;
import com.hospital.gestion.api.patient.entity.Patient;
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
class PatientRepositoryTest {

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
    private PatientRepository patientRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void savePersistsPatientAndGeneratesMetadata() {
        Patient saved =
                savePatient(
                        "pedro",
                        "Pedro",
                        "Paciente",
                        BloodType.O_POSITIVE,
                        true,
                        "Sanitas",
                        true,
                        LocalDate.of(1990, 5, 15)
                );

        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());
        assertEquals("Pedro Paciente", saved.getFullName());
        assertEquals(BloodType.O_POSITIVE, saved.getBloodType());
        assertTrue(saved.getHasHealthInsurance());

        assertEquals(
                "Maria Familiar",
                saved.getEmergencyContact().getName()
        );
    }

    @Test
    void findExistsAndOwnershipByUserWorkCorrectly() {
        Patient patient =
                savePatient(
                        "pedro",
                        "Pedro",
                        "Paciente",
                        BloodType.O_POSITIVE,
                        false,
                        null,
                        true,
                        LocalDate.of(1990, 5, 15)
                );

        Long userId = patient.getUser().getId();

        Optional<Patient> result =
                patientRepository.findByUser_Id(userId);

        assertTrue(result.isPresent());
        assertEquals(patient.getId(), result.get().getId());
        assertTrue(patientRepository.existsByUser_Id(userId));

        assertTrue(
                patientRepository.existsByIdAndUser_Id(
                        patient.getId(),
                        userId
                )
        );

        assertFalse(
                patientRepository.existsByIdAndUser_Id(
                        patient.getId(),
                        999999L
                )
        );
    }

    @Test
    void findAndExistsByEmailIgnoreCaseWorkCorrectly() {
        Patient patient =
                savePatient(
                        "pedro",
                        "Pedro",
                        "Paciente",
                        BloodType.O_POSITIVE,
                        false,
                        null,
                        true,
                        LocalDate.of(1990, 5, 15)
                );

        Optional<Patient> result =
                patientRepository
                        .findByUser_EmailIgnoreCase(
                                "PEDRO@HOSPITAL.TEST"
                        );

        assertTrue(result.isPresent());
        assertEquals(patient.getId(), result.get().getId());

        assertTrue(
                patientRepository
                        .existsByUser_EmailIgnoreCase(
                                "PEDRO@HOSPITAL.TEST"
                        )
        );

        assertFalse(
                patientRepository
                        .existsByUser_EmailIgnoreCase(
                                "unknown@hospital.test"
                        )
        );
    }

    @Test
    void findAndExistsByDocumentIgnoreCaseWorkCorrectly() {
        Patient patient =
                savePatient(
                        "pedro",
                        "Pedro",
                        "Paciente",
                        BloodType.O_POSITIVE,
                        false,
                        null,
                        true,
                        LocalDate.of(1990, 5, 15)
                );

        Optional<Patient> result =
                patientRepository
                        .findByUser_DocumentIdIgnoreCase(
                                "pedro-doc"
                        );

        assertTrue(result.isPresent());
        assertEquals(patient.getId(), result.get().getId());

        assertTrue(
                patientRepository
                        .existsByUser_DocumentIdIgnoreCase(
                                "pedro-doc"
                        )
        );

        assertFalse(
                patientRepository
                        .existsByUser_DocumentIdIgnoreCase(
                                "UNKNOWN-DOC"
                        )
        );
    }

    @Test
    void findByBloodTypeReturnsListAndPage() {
        savePatient(
                "pedro",
                "Pedro",
                "Paciente",
                BloodType.O_POSITIVE,
                false,
                null,
                true,
                LocalDate.of(1990, 5, 15)
        );

        savePatient(
                "ana",
                "Ana",
                "Paciente",
                BloodType.O_POSITIVE,
                true,
                "Sanitas",
                true,
                LocalDate.of(1985, 2, 10)
        );

        savePatient(
                "juan",
                "Juan",
                "Paciente",
                BloodType.A_POSITIVE,
                false,
                null,
                true,
                LocalDate.of(1995, 7, 20)
        );

        List<Patient> list =
                patientRepository.findByBloodType(
                        BloodType.O_POSITIVE
                );

        Page<Patient> page =
                patientRepository.findByBloodType(
                        BloodType.O_POSITIVE,
                        firstPage()
                );

        assertEquals(2, list.size());
        assertEquals(1, page.getNumberOfElements());
        assertEquals(2L, page.getTotalElements());
        assertEquals(2, page.getTotalPages());
    }

    @Test
    void findByHealthInsuranceReturnsListAndPage() {
        savePatient(
                "pedro",
                "Pedro",
                "Paciente",
                BloodType.O_POSITIVE,
                true,
                "Sanitas",
                true,
                LocalDate.of(1990, 5, 15)
        );

        savePatient(
                "ana",
                "Ana",
                "Paciente",
                BloodType.A_POSITIVE,
                false,
                null,
                true,
                LocalDate.of(1985, 2, 10)
        );

        List<Patient> list =
                patientRepository
                        .findByHasHealthInsurance(true);

        Page<Patient> page =
                patientRepository
                        .findByHasHealthInsurance(
                                true,
                                firstPage()
                        );

        assertEquals(1, list.size());
        assertEquals(1L, page.getTotalElements());
        assertEquals("Pedro", list.getFirst()
                .getUser().getFirstName());
    }

    @Test
    void findByInsuranceProviderIgnoresCaseAndPaginates() {
        savePatient(
                "pedro",
                "Pedro",
                "Paciente",
                BloodType.O_POSITIVE,
                true,
                "Sanitas Madrid",
                true,
                LocalDate.of(1990, 5, 15)
        );

        savePatient(
                "ana",
                "Ana",
                "Paciente",
                BloodType.A_POSITIVE,
                true,
                "Sanitas Premium",
                true,
                LocalDate.of(1985, 2, 10)
        );

        savePatient(
                "juan",
                "Juan",
                "Paciente",
                BloodType.B_POSITIVE,
                true,
                "Adeslas",
                true,
                LocalDate.of(1995, 7, 20)
        );

        List<Patient> list =
                patientRepository
                        .findByHealthInsuranceProviderContainingIgnoreCase(
                                "SANITAS"
                        );

        Page<Patient> page =
                patientRepository
                        .findByHealthInsuranceProviderContainingIgnoreCase(
                                "sanitas",
                                firstPage()
                        );

        assertEquals(2, list.size());
        assertEquals(2L, page.getTotalElements());
        assertEquals(2, page.getTotalPages());
    }

    @Test
    void findByActiveUserReturnsListAndPage() {
        savePatient(
                "active",
                "Active",
                "Patient",
                BloodType.O_POSITIVE,
                false,
                null,
                true,
                LocalDate.of(1990, 5, 15)
        );

        savePatient(
                "inactive",
                "Inactive",
                "Patient",
                BloodType.A_POSITIVE,
                false,
                null,
                false,
                LocalDate.of(1985, 2, 10)
        );

        List<Patient> list =
                patientRepository.findByUser_IsActive(true);

        Page<Patient> page =
                patientRepository.findByUser_IsActive(
                        true,
                        firstPage()
                );

        assertEquals(1, list.size());
        assertEquals(1L, page.getTotalElements());
        assertTrue(list.getFirst().getUser().getIsActive());
    }

    @Test
    void findByBloodTypeAndActiveUserReturnsListAndPage() {
        savePatient(
                "active-o",
                "Active",
                "One",
                BloodType.O_POSITIVE,
                false,
                null,
                true,
                LocalDate.of(1990, 5, 15)
        );

        savePatient(
                "inactive-o",
                "Inactive",
                "One",
                BloodType.O_POSITIVE,
                false,
                null,
                false,
                LocalDate.of(1985, 2, 10)
        );

        savePatient(
                "active-a",
                "Active",
                "Two",
                BloodType.A_POSITIVE,
                false,
                null,
                true,
                LocalDate.of(1995, 7, 20)
        );

        List<Patient> list =
                patientRepository
                        .findByBloodTypeAndUser_IsActive(
                                BloodType.O_POSITIVE,
                                true
                        );

        Page<Patient> page =
                patientRepository
                        .findByBloodTypeAndUser_IsActive(
                                BloodType.O_POSITIVE,
                                true,
                                firstPage()
                        );

        assertEquals(1, list.size());
        assertEquals(1L, page.getTotalElements());
        assertEquals(
                "active-o@hospital.test",
                list.getFirst().getUser().getEmail()
        );
    }

    @Test
    void findByBirthDateBetweenReturnsListAndPage() {
        Patient expected =
                savePatient(
                        "pedro",
                        "Pedro",
                        "Paciente",
                        BloodType.O_POSITIVE,
                        false,
                        null,
                        true,
                        LocalDate.of(1990, 5, 15)
                );

        savePatient(
                "ana",
                "Ana",
                "Paciente",
                BloodType.A_POSITIVE,
                false,
                null,
                true,
                LocalDate.of(1970, 2, 10)
        );

        LocalDate start =
                LocalDate.of(1980, 1, 1);

        LocalDate end =
                LocalDate.of(2000, 12, 31);

        List<Patient> list =
                patientRepository.findByBirthDateBetween(
                        start,
                        end
                );

        Page<Patient> page =
                patientRepository.findByBirthDateBetween(
                        start,
                        end,
                        firstPage()
                );

        assertEquals(1, list.size());
        assertEquals(expected.getId(), list.getFirst().getId());
        assertEquals(1L, page.getTotalElements());
    }

    @Test
    void searchPatientsSearchesUserFieldsAndPaginates() {
        Patient expected =
                savePatient(
                        "pedro",
                        "Pedro",
                        "Paciente Especial",
                        BloodType.O_POSITIVE,
                        false,
                        null,
                        true,
                        LocalDate.of(1990, 5, 15)
                );

        savePatient(
                "ana",
                "Ana",
                "Gomez",
                BloodType.A_POSITIVE,
                false,
                null,
                true,
                LocalDate.of(1985, 2, 10)
        );

        List<Patient> list =
                patientRepository.searchPatients(
                        "pedro paciente"
                );

        Page<Patient> page =
                patientRepository.searchPatients(
                        "PEDRO",
                        firstPage()
                );

        assertEquals(1, list.size());
        assertEquals(expected.getId(), list.getFirst().getId());
        assertEquals(1L, page.getTotalElements());
    }

    @Test
    void findAllReturnsPatientsOrderedByLastAndFirstName() {
        savePatient(
                "carlos",
                "Carlos",
                "Zuluaga",
                BloodType.O_POSITIVE,
                false,
                null,
                true,
                LocalDate.of(1990, 5, 15)
        );

        savePatient(
                "pedro",
                "Pedro",
                "Alonso",
                BloodType.A_POSITIVE,
                false,
                null,
                true,
                LocalDate.of(1985, 2, 10)
        );

        savePatient(
                "ana",
                "Ana",
                "Alonso",
                BloodType.B_POSITIVE,
                false,
                null,
                true,
                LocalDate.of(1995, 7, 20)
        );

        List<Patient> result =
                patientRepository
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
    void findActivePatientsReturnsOnlyActiveAndOrdered() {
        savePatient(
                "carlos",
                "Carlos",
                "Zuluaga",
                BloodType.O_POSITIVE,
                false,
                null,
                true,
                LocalDate.of(1990, 5, 15)
        );

        savePatient(
                "ana",
                "Ana",
                "Alonso",
                BloodType.A_POSITIVE,
                false,
                null,
                true,
                LocalDate.of(1985, 2, 10)
        );

        savePatient(
                "inactive",
                "Inactive",
                "Aardvark",
                BloodType.B_POSITIVE,
                false,
                null,
                false,
                LocalDate.of(1995, 7, 20)
        );

        List<Patient> result =
                patientRepository
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
                result.stream().allMatch(patient ->
                        patient.getUser().getIsActive()
                )
        );
    }

    @Test
    void countMethodsReturnCorrectValues() {
        savePatient(
                "pedro",
                "Pedro",
                "Paciente",
                BloodType.O_POSITIVE,
                true,
                "Sanitas",
                true,
                LocalDate.of(1990, 5, 15)
        );

        savePatient(
                "ana",
                "Ana",
                "Paciente",
                BloodType.O_POSITIVE,
                false,
                null,
                false,
                LocalDate.of(1985, 2, 10)
        );

        savePatient(
                "juan",
                "Juan",
                "Paciente",
                BloodType.A_POSITIVE,
                true,
                "Adeslas",
                true,
                LocalDate.of(1995, 7, 20)
        );

        assertEquals(
                2L,
                patientRepository.countByBloodType(
                        BloodType.O_POSITIVE
                )
        );

        assertEquals(
                2L,
                patientRepository
                        .countByHasHealthInsurance(true)
        );

        assertEquals(
                2L,
                patientRepository.countByUser_IsActive(true)
        );

        assertEquals(
                1L,
                patientRepository
                        .countByBloodTypeAndUser_IsActive(
                                BloodType.O_POSITIVE,
                                true
                        )
        );
    }

    @Test
    void findByIdForUpdateReturnsPatientWithPessimisticLock() {
        Patient saved =
                savePatient(
                        "pedro",
                        "Pedro",
                        "Paciente",
                        BloodType.O_POSITIVE,
                        false,
                        null,
                        true,
                        LocalDate.of(1990, 5, 15)
                );

        Long patientId = saved.getId();

        entityManager.clear();

        Patient result =
                patientRepository.findByIdForUpdate(
                                patientId
                        )
                        .orElseThrow();

        assertEquals(patientId, result.getId());

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

    private Patient savePatient(
            String suffix,
            String firstName,
            String lastName,
            BloodType bloodType,
            boolean insured,
            String provider,
            boolean active,
            LocalDate birthDate
    ) {
        User user =
                userRepository.saveAndFlush(
                        User.builder()
                                .role(Role.PATIENT)
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

        return patientRepository.saveAndFlush(
                Patient.builder()
                        .user(user)
                        .bloodType(bloodType)
                        .birthDate(birthDate)
                        .emergencyContact(
                                EmergencyContact.builder()
                                        .name("Maria Familiar")
                                        .phone("611111111")
                                        .relationship("SISTER")
                                        .build()
                        )
                        .allergies("None")
                        .hasHealthInsurance(insured)
                        .healthInsuranceProvider(provider)
                        .healthInsuranceNumber(
                                insured
                                        ? "INS-" + suffix
                                        : null
                        )
                        .medicalHistory(
                                "Repository test"
                        )
                        .build()
        );
    }
}


package com.hospital.gestion.api.doctor.repository;

import com.hospital.gestion.api.common.enums.DepartmentType;
import com.hospital.gestion.api.common.enums.Role;
import com.hospital.gestion.api.common.enums.Specialty;
import com.hospital.gestion.api.department.entity.Department;
import com.hospital.gestion.api.department.repository.DepartmentRepository;
import com.hospital.gestion.api.doctor.entity.Doctor;
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
class DoctorRepositoryTest {

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
    private DoctorRepository doctorRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void savePersistsDoctorAndGeneratesMetadata() {
        Department department =
                saveDepartment(
                        DepartmentType.CARDIOLOGY
                );

        Doctor saved =
                saveDoctor(
                        "laura",
                        "Laura",
                        "Medica",
                        "MED-001",
                        Specialty.CARDIOLOGY,
                        department,
                        true
                );

        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());
        assertEquals("Laura Medica", saved.getFullName());
        assertEquals(Specialty.CARDIOLOGY, saved.getSpecialty());
        assertEquals(department.getId(), saved.getDepartment().getId());
        assertEquals(10, saved.getYearsOfExperience());
    }

    @Test
    void userLookupsAndOwnershipWorkCorrectly() {
        Department department =
                saveDepartment(
                        DepartmentType.CARDIOLOGY
                );

        Doctor doctor =
                saveDoctor(
                        "laura",
                        "Laura",
                        "Medica",
                        "MED-001",
                        Specialty.CARDIOLOGY,
                        department,
                        true
                );

        Long userId = doctor.getUser().getId();

        assertEquals(
                doctor.getId(),
                doctorRepository.findByUser_Id(userId)
                        .orElseThrow()
                        .getId()
        );

        assertTrue(
                doctorRepository.existsByUser_Id(userId)
        );

        assertEquals(
                doctor.getId(),
                doctorRepository
                        .findByUser_EmailIgnoreCase(
                                "LAURA@HOSPITAL.TEST"
                        )
                        .orElseThrow()
                        .getId()
        );

        assertEquals(
                doctor.getId(),
                doctorRepository
                        .findByUser_DocumentIdIgnoreCase(
                                "laura-doc"
                        )
                        .orElseThrow()
                        .getId()
        );

        assertTrue(
                doctorRepository.existsByIdAndUser_Id(
                        doctor.getId(),
                        userId
                )
        );

        assertFalse(
                doctorRepository.existsByIdAndUser_Id(
                        doctor.getId(),
                        999999L
                )
        );
    }

    @Test
    void findAndExistsByMedicalLicenseIgnoreCase() {
        Department department =
                saveDepartment(
                        DepartmentType.CARDIOLOGY
                );

        Doctor doctor =
                saveDoctor(
                        "laura",
                        "Laura",
                        "Medica",
                        "MED-001",
                        Specialty.CARDIOLOGY,
                        department,
                        true
                );

        Optional<Doctor> result =
                doctorRepository
                        .findByMedicalLicenseNumberIgnoreCase(
                                "med-001"
                        );

        assertTrue(result.isPresent());
        assertEquals(doctor.getId(), result.get().getId());

        assertTrue(
                doctorRepository
                        .existsByMedicalLicenseNumberIgnoreCase(
                                "med-001"
                        )
        );

        assertFalse(
                doctorRepository
                        .existsByMedicalLicenseNumberIgnoreCase(
                                "MED-999"
                        )
        );
    }

    @Test
    void findBySpecialtyReturnsListAndPage() {
        Department department =
                saveDepartment(
                        DepartmentType.CARDIOLOGY
                );

        saveDoctor(
                "laura",
                "Laura",
                "Medica",
                "MED-001",
                Specialty.CARDIOLOGY,
                department,
                true
        );

        saveDoctor(
                "carlos",
                "Carlos",
                "Cardiologo",
                "MED-002",
                Specialty.CARDIOLOGY,
                department,
                true
        );

        saveDoctor(
                "ana",
                "Ana",
                "Cirujana",
                "MED-003",
                Specialty.CARDIAC_SURGERY,
                department,
                true
        );

        List<Doctor> list =
                doctorRepository.findBySpecialty(
                        Specialty.CARDIOLOGY
                );

        Page<Doctor> page =
                doctorRepository.findBySpecialty(
                        Specialty.CARDIOLOGY,
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

        saveDoctor(
                "laura",
                "Laura",
                "Medica",
                "MED-001",
                Specialty.CARDIOLOGY,
                cardiology,
                true
        );

        saveDoctor(
                "carlos",
                "Carlos",
                "Medico",
                "MED-002",
                Specialty.CARDIAC_SURGERY,
                cardiology,
                true
        );

        saveDoctor(
                "ana",
                "Ana",
                "Urgencias",
                "MED-003",
                Specialty.EMERGENCY_MEDICINE,
                emergency,
                true
        );

        List<Doctor> list =
                doctorRepository.findByDepartment_Id(
                        cardiology.getId()
                );

        Page<Doctor> page =
                doctorRepository.findByDepartment_Id(
                        cardiology.getId(),
                        firstPage()
                );

        assertEquals(2, list.size());
        assertEquals(2L, page.getTotalElements());
    }

    @Test
    void findByDepartmentAndSpecialtyReturnsListAndPage() {
        Department department =
                saveDepartment(
                        DepartmentType.CARDIOLOGY
                );

        saveDoctor(
                "laura",
                "Laura",
                "Medica",
                "MED-001",
                Specialty.CARDIOLOGY,
                department,
                true
        );

        saveDoctor(
                "carlos",
                "Carlos",
                "Medico",
                "MED-002",
                Specialty.CARDIOLOGY,
                department,
                true
        );

        saveDoctor(
                "ana",
                "Ana",
                "Cirujana",
                "MED-003",
                Specialty.CARDIAC_SURGERY,
                department,
                true
        );

        List<Doctor> list =
                doctorRepository
                        .findByDepartment_IdAndSpecialty(
                                department.getId(),
                                Specialty.CARDIOLOGY
                        );

        Page<Doctor> page =
                doctorRepository
                        .findByDepartment_IdAndSpecialty(
                                department.getId(),
                                Specialty.CARDIOLOGY,
                                firstPage()
                        );

        assertEquals(2, list.size());
        assertEquals(2L, page.getTotalElements());
    }

    @Test
    void findByActiveStatusReturnsListAndPage() {
        Department department =
                saveDepartment(
                        DepartmentType.CARDIOLOGY
                );

        saveDoctor(
                "active",
                "Active",
                "Doctor",
                "MED-001",
                Specialty.CARDIOLOGY,
                department,
                true
        );

        saveDoctor(
                "inactive",
                "Inactive",
                "Doctor",
                "MED-002",
                Specialty.CARDIOLOGY,
                department,
                false
        );

        List<Doctor> list =
                doctorRepository.findByUser_IsActive(true);

        Page<Doctor> page =
                doctorRepository.findByUser_IsActive(
                        true,
                        firstPage()
                );

        assertEquals(1, list.size());
        assertEquals(1L, page.getTotalElements());
        assertTrue(list.getFirst().getUser().getIsActive());
    }

    @Test
    void findBySpecialtyAndActiveReturnsListAndPage() {
        Department department =
                saveDepartment(
                        DepartmentType.CARDIOLOGY
                );

        saveDoctor(
                "active-cardio",
                "Active",
                "Cardio",
                "MED-001",
                Specialty.CARDIOLOGY,
                department,
                true
        );

        saveDoctor(
                "inactive-cardio",
                "Inactive",
                "Cardio",
                "MED-002",
                Specialty.CARDIOLOGY,
                department,
                false
        );

        saveDoctor(
                "active-surgery",
                "Active",
                "Surgery",
                "MED-003",
                Specialty.CARDIAC_SURGERY,
                department,
                true
        );

        List<Doctor> list =
                doctorRepository
                        .findBySpecialtyAndUser_IsActive(
                                Specialty.CARDIOLOGY,
                                true
                        );

        Page<Doctor> page =
                doctorRepository
                        .findBySpecialtyAndUser_IsActive(
                                Specialty.CARDIOLOGY,
                                true,
                                firstPage()
                        );

        assertEquals(1, list.size());
        assertEquals(1L, page.getTotalElements());
    }

    @Test
    void findByDepartmentAndActiveReturnsListAndPage() {
        Department cardiology =
                saveDepartment(
                        DepartmentType.CARDIOLOGY
                );

        Department emergency =
                saveDepartment(
                        DepartmentType.EMERGENCY
                );

        saveDoctor(
                "active-cardio",
                "Active",
                "Cardio",
                "MED-001",
                Specialty.CARDIOLOGY,
                cardiology,
                true
        );

        saveDoctor(
                "inactive-cardio",
                "Inactive",
                "Cardio",
                "MED-002",
                Specialty.CARDIOLOGY,
                cardiology,
                false
        );

        saveDoctor(
                "active-emergency",
                "Active",
                "Emergency",
                "MED-003",
                Specialty.EMERGENCY_MEDICINE,
                emergency,
                true
        );

        List<Doctor> list =
                doctorRepository
                        .findByDepartment_IdAndUser_IsActive(
                                cardiology.getId(),
                                true
                        );

        Page<Doctor> page =
                doctorRepository
                        .findByDepartment_IdAndUser_IsActive(
                                cardiology.getId(),
                                true,
                                firstPage()
                        );

        assertEquals(1, list.size());
        assertEquals(1L, page.getTotalElements());
    }

    @Test
    void combinedDepartmentSpecialtyAndActiveFilterWorks() {
        Department department =
                saveDepartment(
                        DepartmentType.CARDIOLOGY
                );

        saveDoctor(
                "matching",
                "Matching",
                "Doctor",
                "MED-001",
                Specialty.CARDIOLOGY,
                department,
                true
        );

        saveDoctor(
                "inactive",
                "Inactive",
                "Doctor",
                "MED-002",
                Specialty.CARDIOLOGY,
                department,
                false
        );

        saveDoctor(
                "surgery",
                "Surgery",
                "Doctor",
                "MED-003",
                Specialty.CARDIAC_SURGERY,
                department,
                true
        );

        List<Doctor> list =
                doctorRepository
                        .findByDepartment_IdAndSpecialtyAndUser_IsActive(
                                department.getId(),
                                Specialty.CARDIOLOGY,
                                true
                        );

        Page<Doctor> page =
                doctorRepository
                        .findByDepartment_IdAndSpecialtyAndUser_IsActive(
                                department.getId(),
                                Specialty.CARDIOLOGY,
                                true,
                                firstPage()
                        );

        assertEquals(1, list.size());
        assertEquals(1L, page.getTotalElements());
        assertEquals(
                "matching@hospital.test",
                list.getFirst().getUser().getEmail()
        );
    }

    @Test
    void searchDoctorsSearchesUserAndLicenseFields() {
        Department department =
                saveDepartment(
                        DepartmentType.CARDIOLOGY
                );

        Doctor expected =
                saveDoctor(
                        "laura",
                        "Laura",
                        "Medica Especial",
                        "CARD-12345",
                        Specialty.CARDIOLOGY,
                        department,
                        true
                );

        saveDoctor(
                "ana",
                "Ana",
                "Cirujana",
                "SUR-99999",
                Specialty.CARDIAC_SURGERY,
                department,
                true
        );

        List<Doctor> list =
                doctorRepository.searchDoctors(
                        "laura medica"
                );

        Page<Doctor> page =
                doctorRepository.searchDoctors(
                        "CARD-12345",
                        firstPage()
                );

        assertEquals(1, list.size());
        assertEquals(expected.getId(), list.getFirst().getId());
        assertEquals(1L, page.getTotalElements());
        assertEquals(expected.getId(),
                page.getContent().getFirst().getId());
    }

    @Test
    void findAllReturnsDoctorsOrderedByName() {
        Department department =
                saveDepartment(
                        DepartmentType.CARDIOLOGY
                );

        saveDoctor(
                "carlos",
                "Carlos",
                "Zuluaga",
                "MED-001",
                Specialty.CARDIOLOGY,
                department,
                true
        );

        saveDoctor(
                "pedro",
                "Pedro",
                "Alonso",
                "MED-002",
                Specialty.CARDIOLOGY,
                department,
                true
        );

        saveDoctor(
                "ana",
                "Ana",
                "Alonso",
                "MED-003",
                Specialty.CARDIOLOGY,
                department,
                true
        );

        List<Doctor> result =
                doctorRepository
                        .findAllByOrderByUser_LastNameAscUser_FirstNameAsc();

        assertEquals(3, result.size());
        assertEquals("Ana",
                result.get(0).getUser().getFirstName());
        assertEquals("Pedro",
                result.get(1).getUser().getFirstName());
        assertEquals("Carlos",
                result.get(2).getUser().getFirstName());
    }

    @Test
    void findActiveDoctorsReturnsOnlyActiveAndOrdered() {
        Department department =
                saveDepartment(
                        DepartmentType.CARDIOLOGY
                );

        saveDoctor(
                "carlos",
                "Carlos",
                "Zuluaga",
                "MED-001",
                Specialty.CARDIOLOGY,
                department,
                true
        );

        saveDoctor(
                "ana",
                "Ana",
                "Alonso",
                "MED-002",
                Specialty.CARDIOLOGY,
                department,
                true
        );

        saveDoctor(
                "inactive",
                "Inactive",
                "Aardvark",
                "MED-003",
                Specialty.CARDIOLOGY,
                department,
                false
        );

        List<Doctor> result =
                doctorRepository
                        .findByUser_IsActiveTrueOrderByUser_LastNameAscUser_FirstNameAsc();

        assertEquals(2, result.size());
        assertEquals("Ana",
                result.get(0).getUser().getFirstName());
        assertEquals("Carlos",
                result.get(1).getUser().getFirstName());

        assertTrue(
                result.stream().allMatch(doctor ->
                        doctor.getUser().getIsActive()
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

        saveDoctor(
                "active-cardio",
                "Active",
                "Cardio",
                "MED-001",
                Specialty.CARDIOLOGY,
                cardiology,
                true
        );

        saveDoctor(
                "inactive-cardio",
                "Inactive",
                "Cardio",
                "MED-002",
                Specialty.CARDIOLOGY,
                cardiology,
                false
        );

        saveDoctor(
                "emergency",
                "Emergency",
                "Doctor",
                "MED-003",
                Specialty.EMERGENCY_MEDICINE,
                emergency,
                true
        );

        assertEquals(
                2L,
                doctorRepository.countBySpecialty(
                        Specialty.CARDIOLOGY
                )
        );

        assertEquals(
                2L,
                doctorRepository.countByDepartment_Id(
                        cardiology.getId()
                )
        );

        assertEquals(
                2L,
                doctorRepository.countByUser_IsActive(true)
        );

        assertEquals(
                1L,
                doctorRepository
                        .countBySpecialtyAndUser_IsActive(
                                Specialty.CARDIOLOGY,
                                true
                        )
        );

        assertEquals(
                1L,
                doctorRepository
                        .countByDepartment_IdAndUser_IsActive(
                                cardiology.getId(),
                                true
                        )
        );

        assertEquals(
                2L,
                doctorRepository
                        .countByDepartment_IdAndSpecialty(
                                cardiology.getId(),
                                Specialty.CARDIOLOGY
                        )
        );
    }

    @Test
    void findByIdForUpdateReturnsDoctorWithPessimisticLock() {
        Department department =
                saveDepartment(
                        DepartmentType.CARDIOLOGY
                );

        Doctor saved =
                saveDoctor(
                        "laura",
                        "Laura",
                        "Medica",
                        "MED-001",
                        Specialty.CARDIOLOGY,
                        department,
                        true
                );

        Long doctorId = saved.getId();

        entityManager.clear();

        Doctor result =
                doctorRepository.findByIdForUpdate(
                                doctorId
                        )
                        .orElseThrow();

        assertEquals(doctorId, result.getId());

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

    private Doctor saveDoctor(
            String suffix,
            String firstName,
            String lastName,
            String license,
            Specialty specialty,
            Department department,
            boolean active
    ) {
        User user =
                userRepository.saveAndFlush(
                        User.builder()
                                .role(Role.DOCTOR)
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

        return doctorRepository.saveAndFlush(
                Doctor.builder()
                        .user(user)
                        .department(department)
                        .specialty(specialty)
                        .medicalLicenseNumber(license)
                        .yearsOfExperience(10)
                        .biography("Repository test")
                        .build()
        );
    }
}

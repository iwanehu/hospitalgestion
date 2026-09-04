package com.hospital.gestion.api.admission.repository;

import com.hospital.gestion.api.admission.entity.Admission;
import com.hospital.gestion.api.bed.entity.Bed;
import com.hospital.gestion.api.bed.repository.BedRepository;
import com.hospital.gestion.api.common.enums.*;
import com.hospital.gestion.api.department.entity.Department;
import com.hospital.gestion.api.department.repository.DepartmentRepository;
import com.hospital.gestion.api.doctor.entity.Doctor;
import com.hospital.gestion.api.doctor.repository.DoctorRepository;
import com.hospital.gestion.api.patient.entity.Patient;
import com.hospital.gestion.api.patient.repository.PatientRepository;
import com.hospital.gestion.api.room.entity.Room;
import com.hospital.gestion.api.room.repository.RoomRepository;
import com.hospital.gestion.api.user.entity.User;
import com.hospital.gestion.api.user.repository.UserRepository;
import com.hospital.gestion.api.ward.entity.Ward;
import com.hospital.gestion.api.ward.repository.WardRepository;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class AdmissionRepositoryTest {

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
    private AdmissionRepository admissionRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BedRepository bedRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private WardRepository wardRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void savePersistsAdmissionAndGeneratesMetadata() {
        Fixture fixture = fixture();

        Admission saved =
                saveAdmission(
                        fixture,
                        AdmissionStatus.ACTIVE,
                        LocalDateTime.of(
                                2026, 9, 1, 10, 0
                        )
                );

        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());
        assertEquals(
                AdmissionStatus.ACTIVE,
                saved.getStatus()
        );
        assertEquals(
                fixture.patient().getId(),
                saved.getPatient().getId()
        );
        assertEquals(
                fixture.bed().getId(),
                saved.getBed().getId()
        );
        assertEquals(
                fixture.doctor().getId(),
                saved.getAttendingDoctor().getId()
        );
    }

    @Test
    void findByPatientReturnsListAndPage() {
        Fixture fixture = fixture();

        saveAdmission(
                fixture,
                AdmissionStatus.DISCHARGED,
                LocalDateTime.of(2026, 8, 1, 10, 0)
        );

        saveAdmission(
                fixture,
                AdmissionStatus.CANCELLED,
                LocalDateTime.of(2026, 8, 2, 10, 0)
        );

        List<Admission> list =
                admissionRepository.findByPatient_Id(
                        fixture.patient().getId()
                );

        Page<Admission> page =
                admissionRepository.findByPatient_Id(
                        fixture.patient().getId(),
                        firstPage()
                );

        assertEquals(2, list.size());
        assertEquals(1, page.getNumberOfElements());
        assertEquals(2L, page.getTotalElements());
        assertEquals(2, page.getTotalPages());
    }

    @Test
    void findByPatientAndStatusReturnsListPageAndFirst() {
        Fixture fixture = fixture();

        Admission expected =
                saveAdmission(
                        fixture,
                        AdmissionStatus.ACTIVE,
                        LocalDateTime.of(
                                2026, 8, 1, 10, 0
                        )
                );

        saveAdmission(
                fixture,
                AdmissionStatus.DISCHARGED,
                LocalDateTime.of(2026, 7, 1, 10, 0)
        );

        List<Admission> list =
                admissionRepository
                        .findByPatient_IdAndStatus(
                                fixture.patient().getId(),
                                AdmissionStatus.ACTIVE
                        );

        Page<Admission> page =
                admissionRepository
                        .findByPatient_IdAndStatus(
                                fixture.patient().getId(),
                                AdmissionStatus.ACTIVE,
                                firstPage()
                        );

        Optional<Admission> first =
                admissionRepository
                        .findFirstByPatient_IdAndStatus(
                                fixture.patient().getId(),
                                AdmissionStatus.ACTIVE
                        );

        assertEquals(1, list.size());
        assertEquals(1L, page.getTotalElements());
        assertTrue(first.isPresent());
        assertEquals(expected.getId(), first.get().getId());
    }

    @Test
    void findByBedReturnsListAndPage() {
        Fixture fixture = fixture();

        saveAdmission(
                fixture,
                AdmissionStatus.DISCHARGED,
                LocalDateTime.of(2026, 8, 1, 10, 0)
        );

        saveAdmission(
                fixture,
                AdmissionStatus.CANCELLED,
                LocalDateTime.of(2026, 8, 2, 10, 0)
        );

        List<Admission> list =
                admissionRepository.findByBed_Id(
                        fixture.bed().getId()
                );

        Page<Admission> page =
                admissionRepository.findByBed_Id(
                        fixture.bed().getId(),
                        firstPage()
                );

        assertEquals(2, list.size());
        assertEquals(2L, page.getTotalElements());
    }

    @Test
    void findByBedAndStatusReturnsListAndFirst() {
        Fixture fixture = fixture();

        Admission expected =
                saveAdmission(
                        fixture,
                        AdmissionStatus.ACTIVE,
                        LocalDateTime.of(
                                2026, 8, 1, 10, 0
                        )
                );

        saveAdmission(
                fixture,
                AdmissionStatus.DISCHARGED,
                LocalDateTime.of(2026, 7, 1, 10, 0)
        );

        List<Admission> list =
                admissionRepository
                        .findByBed_IdAndStatus(
                                fixture.bed().getId(),
                                AdmissionStatus.ACTIVE
                        );

        Optional<Admission> first =
                admissionRepository
                        .findFirstByBed_IdAndStatus(
                                fixture.bed().getId(),
                                AdmissionStatus.ACTIVE
                        );

        assertEquals(1, list.size());
        assertTrue(first.isPresent());
        assertEquals(expected.getId(), first.get().getId());
    }

    @Test
    void findByDoctorReturnsListAndPage() {
        Fixture fixture = fixture();

        saveAdmission(
                fixture,
                AdmissionStatus.DISCHARGED,
                LocalDateTime.of(2026, 8, 1, 10, 0)
        );

        saveAdmission(
                fixture,
                AdmissionStatus.CANCELLED,
                LocalDateTime.of(2026, 8, 2, 10, 0)
        );

        List<Admission> list =
                admissionRepository
                        .findByAttendingDoctor_Id(
                                fixture.doctor().getId()
                        );

        Page<Admission> page =
                admissionRepository
                        .findByAttendingDoctor_Id(
                                fixture.doctor().getId(),
                                firstPage()
                        );

        assertEquals(2, list.size());
        assertEquals(2L, page.getTotalElements());
    }

    @Test
    void findByDoctorAndStatusReturnsListAndPage() {
        Fixture fixture = fixture();

        saveAdmission(
                fixture,
                AdmissionStatus.DISCHARGED,
                LocalDateTime.of(2026, 8, 1, 10, 0)
        );

        saveAdmission(
                fixture,
                AdmissionStatus.DISCHARGED,
                LocalDateTime.of(2026, 8, 2, 10, 0)
        );

        saveAdmission(
                fixture,
                AdmissionStatus.CANCELLED,
                LocalDateTime.of(2026, 8, 3, 10, 0)
        );

        List<Admission> list =
                admissionRepository
                        .findByAttendingDoctor_IdAndStatus(
                                fixture.doctor().getId(),
                                AdmissionStatus.DISCHARGED
                        );

        Page<Admission> page =
                admissionRepository
                        .findByAttendingDoctor_IdAndStatus(
                                fixture.doctor().getId(),
                                AdmissionStatus.DISCHARGED,
                                firstPage()
                        );

        assertEquals(2, list.size());
        assertEquals(2L, page.getTotalElements());
        assertEquals(2, page.getTotalPages());
    }

    @Test
    void findByStatusReturnsListAndPage() {
        Fixture fixture = fixture();

        saveAdmission(
                fixture,
                AdmissionStatus.DISCHARGED,
                LocalDateTime.of(2026, 8, 1, 10, 0)
        );

        saveAdmission(
                fixture,
                AdmissionStatus.DISCHARGED,
                LocalDateTime.of(2026, 8, 2, 10, 0)
        );

        saveAdmission(
                fixture,
                AdmissionStatus.CANCELLED,
                LocalDateTime.of(2026, 8, 3, 10, 0)
        );

        List<Admission> list =
                admissionRepository.findByStatus(
                        AdmissionStatus.DISCHARGED
                );

        Page<Admission> page =
                admissionRepository.findByStatus(
                        AdmissionStatus.DISCHARGED,
                        firstPage()
                );

        assertEquals(2, list.size());
        assertEquals(2L, page.getTotalElements());
    }

    @Test
    void findByRoomReturnsListPageAndStatusFilter() {
        Fixture fixture = fixture();

        saveAdmission(
                fixture,
                AdmissionStatus.DISCHARGED,
                LocalDateTime.of(2026, 8, 1, 10, 0)
        );

        saveAdmission(
                fixture,
                AdmissionStatus.CANCELLED,
                LocalDateTime.of(2026, 8, 2, 10, 0)
        );

        List<Admission> list =
                admissionRepository.findByBed_Room_Id(
                        fixture.room().getId()
                );

        Page<Admission> page =
                admissionRepository.findByBed_Room_Id(
                        fixture.room().getId(),
                        firstPage()
                );

        List<Admission> filtered =
                admissionRepository
                        .findByBed_Room_IdAndStatus(
                                fixture.room().getId(),
                                AdmissionStatus.DISCHARGED
                        );

        assertEquals(2, list.size());
        assertEquals(2L, page.getTotalElements());
        assertEquals(1, filtered.size());
    }

    @Test
    void findByWardReturnsListPageAndStatusFilter() {
        Fixture fixture = fixture();

        saveAdmission(
                fixture,
                AdmissionStatus.DISCHARGED,
                LocalDateTime.of(2026, 8, 1, 10, 0)
        );

        saveAdmission(
                fixture,
                AdmissionStatus.CANCELLED,
                LocalDateTime.of(2026, 8, 2, 10, 0)
        );

        List<Admission> list =
                admissionRepository
                        .findByBed_Room_Ward_Id(
                                fixture.ward().getId()
                        );

        Page<Admission> page =
                admissionRepository
                        .findByBed_Room_Ward_Id(
                                fixture.ward().getId(),
                                firstPage()
                        );

        List<Admission> filtered =
                admissionRepository
                        .findByBed_Room_Ward_IdAndStatus(
                                fixture.ward().getId(),
                                AdmissionStatus.DISCHARGED
                        );

        assertEquals(2, list.size());
        assertEquals(2L, page.getTotalElements());
        assertEquals(1, filtered.size());
    }

    @Test
    void findByDepartmentReturnsListPageAndStatusFilter() {
        Fixture fixture = fixture();

        saveAdmission(
                fixture,
                AdmissionStatus.DISCHARGED,
                LocalDateTime.of(2026, 8, 1, 10, 0)
        );

        saveAdmission(
                fixture,
                AdmissionStatus.CANCELLED,
                LocalDateTime.of(2026, 8, 2, 10, 0)
        );

        List<Admission> list =
                admissionRepository
                        .findByBed_Room_Ward_Department_Id(
                                fixture.department().getId()
                        );

        Page<Admission> page =
                admissionRepository
                        .findByBed_Room_Ward_Department_Id(
                                fixture.department().getId(),
                                firstPage()
                        );

        List<Admission> filtered =
                admissionRepository
                        .findByBed_Room_Ward_Department_IdAndStatus(
                                fixture.department().getId(),
                                AdmissionStatus.DISCHARGED
                        );

        assertEquals(2, list.size());
        assertEquals(2L, page.getTotalElements());
        assertEquals(1, filtered.size());
    }

    @Test
    void findByAdmittedAtBetweenReturnsListAndPage() {
        Fixture fixture = fixture();

        saveAdmission(
                fixture,
                AdmissionStatus.DISCHARGED,
                LocalDateTime.of(2026, 7, 15, 10, 0)
        );

        Admission expected =
                saveAdmission(
                        fixture,
                        AdmissionStatus.CANCELLED,
                        LocalDateTime.of(
                                2026, 8, 15, 10, 0
                        )
                );

        saveAdmission(
                fixture,
                AdmissionStatus.TRANSFERRED,
                LocalDateTime.of(2026, 9, 15, 10, 0)
        );

        LocalDateTime start =
                LocalDateTime.of(2026, 8, 1, 0, 0);

        LocalDateTime end =
                LocalDateTime.of(2026, 8, 31, 23, 59);

        List<Admission> list =
                admissionRepository
                        .findByAdmittedAtBetween(
                                start,
                                end
                        );

        Page<Admission> page =
                admissionRepository
                        .findByAdmittedAtBetween(
                                start,
                                end,
                                firstPage()
                        );

        assertEquals(1, list.size());
        assertEquals(expected.getId(), list.getFirst().getId());
        assertEquals(1L, page.getTotalElements());
    }

    @Test
    void existsMethodsReturnExpectedValues() {
        Fixture fixture = fixture();

        saveAdmission(
                fixture,
                AdmissionStatus.ACTIVE,
                LocalDateTime.of(2026, 8, 1, 10, 0)
        );

        assertTrue(
                admissionRepository
                        .existsByPatient_IdAndStatus(
                                fixture.patient().getId(),
                                AdmissionStatus.ACTIVE
                        )
        );

        assertTrue(
                admissionRepository
                        .existsByBed_IdAndStatus(
                                fixture.bed().getId(),
                                AdmissionStatus.ACTIVE
                        )
        );

        assertFalse(
                admissionRepository
                        .existsByPatient_IdAndStatus(
                                fixture.patient().getId(),
                                AdmissionStatus.CANCELLED
                        )
        );
    }

    @Test
    void countByStatusAndPatientReturnsCorrectValues() {
        Fixture fixture = fixture();

        saveAdmission(
                fixture,
                AdmissionStatus.DISCHARGED,
                LocalDateTime.of(2026, 8, 1, 10, 0)
        );

        saveAdmission(
                fixture,
                AdmissionStatus.CANCELLED,
                LocalDateTime.of(2026, 8, 2, 10, 0)
        );

        assertEquals(
                1L,
                admissionRepository.countByStatus(
                        AdmissionStatus.DISCHARGED
                )
        );

        assertEquals(
                2L,
                admissionRepository.countByPatient_Id(
                        fixture.patient().getId()
                )
        );
    }

    @Test
    void countByDoctorAndStatusReturnsCorrectValues() {
        Fixture fixture = fixture();

        saveAdmission(
                fixture,
                AdmissionStatus.DISCHARGED,
                LocalDateTime.of(2026, 8, 1, 10, 0)
        );

        saveAdmission(
                fixture,
                AdmissionStatus.DISCHARGED,
                LocalDateTime.of(2026, 8, 2, 10, 0)
        );

        saveAdmission(
                fixture,
                AdmissionStatus.CANCELLED,
                LocalDateTime.of(2026, 8, 3, 10, 0)
        );

        assertEquals(
                3L,
                admissionRepository
                        .countByAttendingDoctor_Id(
                                fixture.doctor().getId()
                        )
        );

        assertEquals(
                2L,
                admissionRepository
                        .countByAttendingDoctor_IdAndStatus(
                                fixture.doctor().getId(),
                                AdmissionStatus.DISCHARGED
                        )
        );
    }

    @Test
    void hierarchicalCountMethodsReturnCorrectValues() {
        Fixture fixture = fixture();

        saveAdmission(
                fixture,
                AdmissionStatus.DISCHARGED,
                LocalDateTime.of(2026, 8, 1, 10, 0)
        );

        saveAdmission(
                fixture,
                AdmissionStatus.DISCHARGED,
                LocalDateTime.of(2026, 8, 2, 10, 0)
        );

        saveAdmission(
                fixture,
                AdmissionStatus.CANCELLED,
                LocalDateTime.of(2026, 8, 3, 10, 0)
        );

        assertEquals(
                2L,
                admissionRepository
                        .countByBed_Room_IdAndStatus(
                                fixture.room().getId(),
                                AdmissionStatus.DISCHARGED
                        )
        );

        assertEquals(
                2L,
                admissionRepository
                        .countByBed_Room_Ward_IdAndStatus(
                                fixture.ward().getId(),
                                AdmissionStatus.DISCHARGED
                        )
        );

        assertEquals(
                2L,
                admissionRepository
                        .countByBed_Room_Ward_Department_IdAndStatus(
                                fixture.department().getId(),
                                AdmissionStatus.DISCHARGED
                        )
        );
    }

    @Test
    void findByIdForUpdateReturnsPessimisticallyLockedAdmission() {
        Fixture fixture = fixture();

        Admission saved =
                saveAdmission(
                        fixture,
                        AdmissionStatus.ACTIVE,
                        LocalDateTime.of(
                                2026, 8, 1, 10, 0
                        )
                );

        entityManager.clear();

        Admission result =
                admissionRepository.findByIdForUpdate(
                                saved.getId()
                        )
                        .orElseThrow();

        assertEquals(saved.getId(), result.getId());

        assertEquals(
                LockModeType.PESSIMISTIC_WRITE,
                entityManager.getLockMode(result)
        );
    }

    @Test
    void existsByIdAndPatientUserIdChecksOwnership() {
        Fixture fixture = fixture();

        Admission saved =
                saveAdmission(
                        fixture,
                        AdmissionStatus.ACTIVE,
                        LocalDateTime.of(
                                2026, 8, 1, 10, 0
                        )
                );

        assertTrue(
                admissionRepository
                        .existsByIdAndPatient_User_Id(
                                saved.getId(),
                                fixture.patient()
                                        .getUser()
                                        .getId()
                        )
        );

        assertFalse(
                admissionRepository
                        .existsByIdAndPatient_User_Id(
                                saved.getId(),
                                999999L
                        )
        );
    }

    private PageRequest firstPage() {
        return PageRequest.of(
                0,
                1,
                Sort.by("admittedAt").ascending()
        );
    }

    private Fixture fixture() {
        Department department =
                departmentRepository.saveAndFlush(
                        Department.builder()
                                .departmentType(
                                        DepartmentType.CARDIOLOGY
                                )
                                .location("Cardiology Floor")
                                .phoneExtension("200")
                                .description(
                                        "Cardiology department"
                                )
                                .isActive(true)
                                .build()
                );

        Ward ward =
                wardRepository.saveAndFlush(
                        Ward.builder()
                                .name("Cardiology Ward")
                                .description(
                                        "Cardiology ward"
                                )
                                .isActive(true)
                                .department(department)
                                .build()
                );

        Room room =
                roomRepository.saveAndFlush(
                        Room.builder()
                                .number("CARD-201")
                                .floor(2)
                                .roomType(
                                        RoomType.CARDIOLOGY_ICU
                                )
                                .status(
                                        RoomStatus.AVAILABLE
                                )
                                .capacity(4)
                                .ward(ward)
                                .notes("Cardiology room")
                                .build()
                );

        Bed bed =
                bedRepository.saveAndFlush(
                        Bed.builder()
                                .bedNumber("BED-001")
                                .room(room)
                                .status(BedStatus.AVAILABLE)
                                .notes("Cardiology bed")
                                .build()
                );

        User patientUser =
                saveUser(
                        Role.PATIENT,
                        "patient",
                        "PAT-001"
                );

        Patient patient =
                patientRepository.saveAndFlush(
                        Patient.builder()
                                .user(patientUser)
                                .bloodType(
                                        BloodType.O_POSITIVE
                                )
                                .birthDate(
                                        LocalDate.of(
                                                1990, 5, 15
                                        )
                                )
                                .hasHealthInsurance(false)
                                .medicalHistory(
                                        "No previous admissions"
                                )
                                .build()
                );

        User doctorUser =
                saveUser(
                        Role.DOCTOR,
                        "doctor",
                        "DOC-001"
                );

        Doctor doctor =
                doctorRepository.saveAndFlush(
                        Doctor.builder()
                                .user(doctorUser)
                                .department(department)
                                .specialty(
                                        Specialty.CARDIOLOGY
                                )
                                .medicalLicenseNumber(
                                        "MED-001"
                                )
                                .yearsOfExperience(10)
                                .biography("Cardiologist")
                                .build()
                );

        return new Fixture(
                department,
                ward,
                room,
                bed,
                patient,
                doctor
        );
    }

    private User saveUser(
            Role role,
            String prefix,
            String documentId
    ) {
        return userRepository.saveAndFlush(
                User.builder()
                        .role(role)
                        .email(
                                prefix + "@hospital.test"
                        )
                        .password(
                                "$2a$10$encodedPassword"
                        )
                        .isActive(true)
                        .documentId(documentId)
                        .firstName(prefix)
                        .lastName("Test")
                        .phone("600000000")
                        .build()
        );
    }

    private Admission saveAdmission(
            Fixture fixture,
            AdmissionStatus status,
            LocalDateTime admittedAt
    ) {
        return admissionRepository.saveAndFlush(
                Admission.builder()
                        .patient(fixture.patient())
                        .bed(fixture.bed())
                        .attendingDoctor(
                                fixture.doctor()
                        )
                        .status(status)
                        .admissionReason(
                                "Medical observation"
                        )
                        .admittedAt(admittedAt)
                        .notes("Repository test")
                        .build()
        );
    }

    private record Fixture(
            Department department,
            Ward ward,
            Room room,
            Bed bed,
            Patient patient,
            Doctor doctor
    ) {
    }
}

package com.hospital.gestion.api.appointment.repository;

import com.hospital.gestion.api.appointment.entity.Appointment;
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

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class AppointmentRepositoryTest {

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
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private WardRepository wardRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void savePersistsAppointmentAndGeneratesMetadata() {
        Fixture fixture = fixture();

        Appointment saved =
                saveAppointment(
                        fixture,
                        AppointmentStatus.SCHEDULED,
                        date(10, 0)
                );

        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());
        assertEquals(
                AppointmentStatus.SCHEDULED,
                saved.getStatus()
        );
        assertEquals(
                fixture.doctor().getId(),
                saved.getDoctor().getId()
        );
        assertEquals(
                fixture.patient().getId(),
                saved.getPatient().getId()
        );
        assertEquals(
                fixture.room().getId(),
                saved.getRoom().getId()
        );
    }

    @Test
    void findByDoctorReturnsListAndPage() {
        Fixture fixture = fixture();

        saveStandardAppointments(fixture);

        List<Appointment> list =
                appointmentRepository.findByDoctor_Id(
                        fixture.doctor().getId()
                );

        Page<Appointment> page =
                appointmentRepository.findByDoctor_Id(
                        fixture.doctor().getId(),
                        firstPage()
                );

        assertEquals(3, list.size());
        assertEquals(1, page.getNumberOfElements());
        assertEquals(3L, page.getTotalElements());
        assertEquals(3, page.getTotalPages());
    }

    @Test
    void findByDoctorAndStatusReturnsListAndPage() {
        Fixture fixture = fixture();

        saveStandardAppointments(fixture);

        List<Appointment> list =
                appointmentRepository
                        .findByDoctor_IdAndStatus(
                                fixture.doctor().getId(),
                                AppointmentStatus.SCHEDULED
                        );

        Page<Appointment> page =
                appointmentRepository
                        .findByDoctor_IdAndStatus(
                                fixture.doctor().getId(),
                                AppointmentStatus.SCHEDULED,
                                firstPage()
                        );

        assertEquals(2, list.size());
        assertEquals(2L, page.getTotalElements());
        assertEquals(2, page.getTotalPages());
    }

    @Test
    void findByPatientReturnsListAndPage() {
        Fixture fixture = fixture();

        saveStandardAppointments(fixture);

        List<Appointment> list =
                appointmentRepository.findByPatient_Id(
                        fixture.patient().getId()
                );

        Page<Appointment> page =
                appointmentRepository.findByPatient_Id(
                        fixture.patient().getId(),
                        firstPage()
                );

        assertEquals(3, list.size());
        assertEquals(3L, page.getTotalElements());
    }

    @Test
    void findByPatientAndStatusReturnsListAndPage() {
        Fixture fixture = fixture();

        saveStandardAppointments(fixture);

        List<Appointment> list =
                appointmentRepository
                        .findByPatient_IdAndStatus(
                                fixture.patient().getId(),
                                AppointmentStatus.SCHEDULED
                        );

        Page<Appointment> page =
                appointmentRepository
                        .findByPatient_IdAndStatus(
                                fixture.patient().getId(),
                                AppointmentStatus.SCHEDULED,
                                firstPage()
                        );

        assertEquals(2, list.size());
        assertEquals(2L, page.getTotalElements());
    }

    @Test
    void findByRoomReturnsListPageAndStatusFilter() {
        Fixture fixture = fixture();

        saveStandardAppointments(fixture);

        List<Appointment> list =
                appointmentRepository.findByRoom_Id(
                        fixture.room().getId()
                );

        Page<Appointment> page =
                appointmentRepository.findByRoom_Id(
                        fixture.room().getId(),
                        firstPage()
                );

        List<Appointment> filtered =
                appointmentRepository
                        .findByRoom_IdAndStatus(
                                fixture.room().getId(),
                                AppointmentStatus.CANCELLED
                        );

        assertEquals(3, list.size());
        assertEquals(3L, page.getTotalElements());
        assertEquals(1, filtered.size());
    }

    @Test
    void findByStatusReturnsListAndPage() {
        Fixture fixture = fixture();

        saveStandardAppointments(fixture);

        List<Appointment> list =
                appointmentRepository.findByStatus(
                        AppointmentStatus.SCHEDULED
                );

        Page<Appointment> page =
                appointmentRepository.findByStatus(
                        AppointmentStatus.SCHEDULED,
                        firstPage()
                );

        assertEquals(2, list.size());
        assertEquals(2L, page.getTotalElements());
    }

    @Test
    void findByDateTimeBetweenReturnsListAndPage() {
        Fixture fixture = fixture();

        saveAppointment(
                fixture,
                AppointmentStatus.COMPLETED,
                LocalDateTime.of(
                        2026, 7, 15, 10, 0
                )
        );

        Appointment expected =
                saveAppointment(
                        fixture,
                        AppointmentStatus.SCHEDULED,
                        LocalDateTime.of(
                                2026, 8, 15, 10, 0
                        )
                );

        saveAppointment(
                fixture,
                AppointmentStatus.SCHEDULED,
                LocalDateTime.of(
                        2026, 9, 15, 10, 0
                )
        );

        LocalDateTime start =
                LocalDateTime.of(
                        2026, 8, 1, 0, 0
                );

        LocalDateTime end =
                LocalDateTime.of(
                        2026, 8, 31, 23, 59
                );

        List<Appointment> list =
                appointmentRepository
                        .findByDateTimeBetween(
                                start,
                                end
                        );

        Page<Appointment> page =
                appointmentRepository
                        .findByDateTimeBetween(
                                start,
                                end,
                                firstPage()
                        );

        assertEquals(1, list.size());
        assertEquals(expected.getId(), list.getFirst().getId());
        assertEquals(1L, page.getTotalElements());
    }

    @Test
    void findFutureAppointmentsFiltersStatusesAndOrdersByDate() {
        Fixture fixture = fixture();

        saveAppointment(
                fixture,
                AppointmentStatus.SCHEDULED,
                date(11, 0)
        );

        saveAppointment(
                fixture,
                AppointmentStatus.CONFIRMED,
                date(10, 0)
        );

        saveAppointment(
                fixture,
                AppointmentStatus.CANCELLED,
                date(9, 30)
        );

        List<AppointmentStatus> statuses =
                List.of(
                        AppointmentStatus.SCHEDULED,
                        AppointmentStatus.CONFIRMED
                );

        List<Appointment> list =
                appointmentRepository
                        .findByDateTimeAfterAndStatusInOrderByDateTimeAsc(
                                date(9, 0),
                                statuses
                        );

        Page<Appointment> page =
                appointmentRepository
                        .findByDateTimeAfterAndStatusIn(
                                date(9, 0),
                                statuses,
                                firstPage()
                        );

        assertEquals(2, list.size());
        assertEquals(
                AppointmentStatus.CONFIRMED,
                list.get(0).getStatus()
        );
        assertEquals(
                AppointmentStatus.SCHEDULED,
                list.get(1).getStatus()
        );
        assertEquals(2L, page.getTotalElements());
    }

    @Test
    void dateRangeQueriesWorkForDoctorAndPatient() {
        Fixture fixture = fixture();

        Appointment expected =
                saveAppointment(
                        fixture,
                        AppointmentStatus.SCHEDULED,
                        LocalDateTime.of(
                                2026, 8, 15, 10, 0
                        )
                );

        saveAppointment(
                fixture,
                AppointmentStatus.COMPLETED,
                LocalDateTime.of(
                        2026, 7, 15, 10, 0
                )
        );

        LocalDateTime start =
                LocalDateTime.of(
                        2026, 8, 1, 0, 0
                );

        LocalDateTime end =
                LocalDateTime.of(
                        2026, 8, 31, 23, 59
                );

        List<Appointment> doctorList =
                appointmentRepository
                        .findByDoctor_IdAndDateTimeBetween(
                                fixture.doctor().getId(),
                                start,
                                end
                        );

        Page<Appointment> doctorPage =
                appointmentRepository
                        .findByDoctor_IdAndDateTimeBetween(
                                fixture.doctor().getId(),
                                start,
                                end,
                                firstPage()
                        );

        List<Appointment> patientList =
                appointmentRepository
                        .findByPatient_IdAndDateTimeBetween(
                                fixture.patient().getId(),
                                start,
                                end
                        );

        assertEquals(1, doctorList.size());
        assertEquals(1L, doctorPage.getTotalElements());
        assertEquals(1, patientList.size());
        assertEquals(
                expected.getId(),
                patientList.getFirst().getId()
        );
    }

    @Test
    void findByDepartmentReturnsListPageAndStatusFilter() {
        Fixture fixture = fixture();

        saveStandardAppointments(fixture);

        List<Appointment> list =
                appointmentRepository
                        .findByDoctor_Department_Id(
                                fixture.department().getId()
                        );

        Page<Appointment> page =
                appointmentRepository
                        .findByDoctor_Department_Id(
                                fixture.department().getId(),
                                firstPage()
                        );

        List<Appointment> filtered =
                appointmentRepository
                        .findByDoctor_Department_IdAndStatus(
                                fixture.department().getId(),
                                AppointmentStatus.CANCELLED
                        );

        assertEquals(3, list.size());
        assertEquals(3L, page.getTotalElements());
        assertEquals(1, filtered.size());
    }

    @Test
    void doctorConflictQueriesDetectAndExcludeAppointment() {
        Fixture fixture = fixture();

        Appointment saved =
                saveAppointment(
                        fixture,
                        AppointmentStatus.SCHEDULED,
                        date(10, 0)
                );

        List<AppointmentStatus> statuses =
                activeStatuses();

        assertTrue(
                appointmentRepository.existsDoctorConflict(
                        fixture.doctor().getId(),
                        date(9, 30),
                        date(10, 30),
                        statuses
                )
        );

        assertFalse(
                appointmentRepository
                        .existsDoctorConflictExcludingAppointment(
                                fixture.doctor().getId(),
                                saved.getId(),
                                date(9, 30),
                                date(10, 30),
                                statuses
                        )
        );
    }

    @Test
    void patientConflictQueriesDetectAndExcludeAppointment() {
        Fixture fixture = fixture();

        Appointment saved =
                saveAppointment(
                        fixture,
                        AppointmentStatus.CONFIRMED,
                        date(10, 0)
                );

        List<AppointmentStatus> statuses =
                activeStatuses();

        assertTrue(
                appointmentRepository.existsPatientConflict(
                        fixture.patient().getId(),
                        date(9, 30),
                        date(10, 30),
                        statuses
                )
        );

        assertFalse(
                appointmentRepository
                        .existsPatientConflictExcludingAppointment(
                                fixture.patient().getId(),
                                saved.getId(),
                                date(9, 30),
                                date(10, 30),
                                statuses
                        )
        );
    }

    @Test
    void roomConflictQueriesDetectAndExcludeAppointment() {
        Fixture fixture = fixture();

        Appointment saved =
                saveAppointment(
                        fixture,
                        AppointmentStatus.SCHEDULED,
                        date(10, 0)
                );

        List<AppointmentStatus> statuses =
                activeStatuses();

        assertTrue(
                appointmentRepository.existsRoomConflict(
                        fixture.room().getId(),
                        date(9, 30),
                        date(10, 30),
                        statuses
                )
        );

        assertFalse(
                appointmentRepository
                        .existsRoomConflictExcludingAppointment(
                                fixture.room().getId(),
                                saved.getId(),
                                date(9, 30),
                                date(10, 30),
                                statuses
                        )
        );
    }

    @Test
    void countMethodsReturnCorrectValues() {
        Fixture fixture = fixture();

        saveStandardAppointments(fixture);

        assertEquals(
                2L,
                appointmentRepository.countByStatus(
                        AppointmentStatus.SCHEDULED
                )
        );

        assertEquals(
                3L,
                appointmentRepository.countByDoctor_Id(
                        fixture.doctor().getId()
                )
        );

        assertEquals(
                2L,
                appointmentRepository
                        .countByDoctor_IdAndStatus(
                                fixture.doctor().getId(),
                                AppointmentStatus.SCHEDULED
                        )
        );

        assertEquals(
                3L,
                appointmentRepository.countByPatient_Id(
                        fixture.patient().getId()
                )
        );

        assertEquals(
                2L,
                appointmentRepository
                        .countByPatient_IdAndStatus(
                                fixture.patient().getId(),
                                AppointmentStatus.SCHEDULED
                        )
        );

        assertEquals(
                2L,
                appointmentRepository
                        .countByRoom_IdAndStatus(
                                fixture.room().getId(),
                                AppointmentStatus.SCHEDULED
                        )
        );

        assertEquals(
                2L,
                appointmentRepository
                        .countByDoctor_Department_IdAndStatus(
                                fixture.department().getId(),
                                AppointmentStatus.SCHEDULED
                        )
        );
    }

    @Test
    void lockAndOwnershipQueriesWorkCorrectly() {
        Fixture fixture = fixture();

        Appointment saved =
                saveAppointment(
                        fixture,
                        AppointmentStatus.SCHEDULED,
                        date(10, 0)
                );

        Long appointmentId = saved.getId();
        Long patientUserId =
                fixture.patient().getUser().getId();

        entityManager.clear();

        Appointment locked =
                appointmentRepository.findByIdForUpdate(
                                appointmentId
                        )
                        .orElseThrow();

        assertEquals(appointmentId, locked.getId());

        assertEquals(
                LockModeType.PESSIMISTIC_WRITE,
                entityManager.getLockMode(locked)
        );

        assertTrue(
                appointmentRepository
                        .existsByIdAndPatient_User_Id(
                                appointmentId,
                                patientUserId
                        )
        );

        assertFalse(
                appointmentRepository
                        .existsByIdAndPatient_User_Id(
                                appointmentId,
                                999999L
                        )
        );
    }

    private void saveStandardAppointments(
            Fixture fixture
    ) {
        saveAppointment(
                fixture,
                AppointmentStatus.SCHEDULED,
                date(10, 0)
        );

        saveAppointment(
                fixture,
                AppointmentStatus.SCHEDULED,
                date(11, 0)
        );

        saveAppointment(
                fixture,
                AppointmentStatus.CANCELLED,
                date(12, 0)
        );
    }

    private List<AppointmentStatus> activeStatuses() {
        return List.of(
                AppointmentStatus.SCHEDULED,
                AppointmentStatus.CONFIRMED
        );
    }

    private LocalDateTime date(
            int hour,
            int minute
    ) {
        return LocalDateTime.of(
                2026, 9, 10, hour, minute
        );
    }

    private PageRequest firstPage() {
        return PageRequest.of(
                0,
                1,
                Sort.by("dateTime").ascending()
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
                                .description("Cardiology ward")
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
                                        RoomType.CARDIOLOGY_ROOM
                                )
                                .status(RoomStatus.AVAILABLE)
                                .capacity(4)
                                .ward(ward)
                                .notes("Consultation room")
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
                                        "No previous history"
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
                room,
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
                        .email(prefix + "@hospital.test")
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

    private Appointment saveAppointment(
            Fixture fixture,
            AppointmentStatus status,
            LocalDateTime dateTime
    ) {
        return appointmentRepository.saveAndFlush(
                Appointment.builder()
                        .doctor(fixture.doctor())
                        .patient(fixture.patient())
                        .room(fixture.room())
                        .dateTime(dateTime)
                        .reason("Medical consultation")
                        .notes("Repository test")
                        .status(status)
                        .build()
        );
    }

    private record Fixture(
            Department department,
            Room room,
            Patient patient,
            Doctor doctor
    ) {
    }
}

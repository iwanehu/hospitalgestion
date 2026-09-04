package com.hospital.gestion.api.room.repository;

import com.hospital.gestion.api.bed.entity.Bed;
import com.hospital.gestion.api.bed.repository.BedRepository;
import com.hospital.gestion.api.common.enums.BedStatus;
import com.hospital.gestion.api.common.enums.DepartmentType;
import com.hospital.gestion.api.common.enums.RoomStatus;
import com.hospital.gestion.api.common.enums.RoomType;
import com.hospital.gestion.api.department.entity.Department;
import com.hospital.gestion.api.department.repository.DepartmentRepository;
import com.hospital.gestion.api.room.entity.Room;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class RoomRepositoryTest {

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
    private RoomRepository roomRepository;

    @Autowired
    private WardRepository wardRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private BedRepository bedRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void savePersistsRoomAndGeneratesMetadata() {
        Ward ward = saveCardiologyWard();

        Room saved =
                roomRepository.saveAndFlush(
                        room(
                                "CARD-201",
                                2,
                                RoomType.CARDIOLOGY_ICU,
                                RoomStatus.AVAILABLE,
                                ward
                        )
                );

        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());
        assertEquals("CARD-201", saved.getNumber());
        assertEquals(4, saved.getCapacity());
        assertEquals(ward.getId(), saved.getWard().getId());
    }

    @Test
    void findAndExistsByNumberIgnoreCaseWorkCorrectly() {
        Ward ward = saveCardiologyWard();

        roomRepository.saveAndFlush(
                room(
                        "CARD-201",
                        2,
                        RoomType.CARDIOLOGY_ICU,
                        RoomStatus.AVAILABLE,
                        ward
                )
        );

        Optional<Room> result =
                roomRepository.findByNumberIgnoreCase(
                        "card-201"
                );

        assertTrue(result.isPresent());
        assertEquals("CARD-201", result.get().getNumber());

        assertTrue(
                roomRepository.existsByNumberIgnoreCase(
                        "card-201"
                )
        );

        assertFalse(
                roomRepository.existsByNumberIgnoreCase(
                        "unknown"
                )
        );
    }

    @Test
    void findByWardReturnsListAndPage() {
        Department department =
                saveDepartment(
                        DepartmentType.CARDIOLOGY,
                        "Floor 2"
                );

        Ward targetWard =
                saveWard(
                        "Cardiology Ward",
                        department
                );

        Ward otherWard =
                saveWard(
                        "Observation Ward",
                        department
                );

        roomRepository.save(
                room(
                        "CARD-202",
                        2,
                        RoomType.CARDIOLOGY_ROOM,
                        RoomStatus.AVAILABLE,
                        targetWard
                )
        );

        roomRepository.save(
                room(
                        "CARD-201",
                        2,
                        RoomType.CARDIOLOGY_ICU,
                        RoomStatus.AVAILABLE,
                        targetWard
                )
        );

        roomRepository.save(
                room(
                        "OBS-101",
                        1,
                        RoomType.INDIVIDUAL,
                        RoomStatus.AVAILABLE,
                        otherWard
                )
        );

        roomRepository.flush();

        List<Room> list =
                roomRepository.findByWard_Id(
                        targetWard.getId()
                );

        Page<Room> page =
                roomRepository.findByWard_Id(
                        targetWard.getId(),
                        firstPage()
                );

        assertEquals(2, list.size());
        assertEquals(1, page.getNumberOfElements());
        assertEquals(2L, page.getTotalElements());
        assertEquals(2, page.getTotalPages());
        assertEquals(
                "CARD-201",
                page.getContent().getFirst().getNumber()
        );
    }

    @Test
    void findByStatusReturnsListAndPage() {
        Ward ward = saveCardiologyWard();

        saveRoomsWithDifferentValues(ward);

        List<Room> list =
                roomRepository.findByStatus(
                        RoomStatus.AVAILABLE
                );

        Page<Room> page =
                roomRepository.findByStatus(
                        RoomStatus.AVAILABLE,
                        firstPage()
                );

        assertEquals(2, list.size());
        assertEquals(1, page.getNumberOfElements());
        assertEquals(2L, page.getTotalElements());
        assertTrue(
                page.getContent().stream()
                        .allMatch(room ->
                                room.getStatus()
                                        == RoomStatus.AVAILABLE
                        )
        );
    }

    @Test
    void findByRoomTypeReturnsListAndPage() {
        Ward ward = saveCardiologyWard();

        roomRepository.save(
                room(
                        "CARD-201",
                        2,
                        RoomType.CARDIOLOGY_ICU,
                        RoomStatus.AVAILABLE,
                        ward
                )
        );

        roomRepository.save(
                room(
                        "CARD-202",
                        2,
                        RoomType.CARDIOLOGY_ICU,
                        RoomStatus.OCCUPIED,
                        ward
                )
        );

        roomRepository.save(
                room(
                        "CARD-203",
                        3,
                        RoomType.CARDIOLOGY_ROOM,
                        RoomStatus.AVAILABLE,
                        ward
                )
        );

        roomRepository.flush();

        List<Room> list =
                roomRepository.findByRoomType(
                        RoomType.CARDIOLOGY_ICU
                );

        Page<Room> page =
                roomRepository.findByRoomType(
                        RoomType.CARDIOLOGY_ICU,
                        firstPage()
                );

        assertEquals(2, list.size());
        assertEquals(2L, page.getTotalElements());
        assertEquals(
                RoomType.CARDIOLOGY_ICU,
                page.getContent().getFirst()
                        .getRoomType()
        );
    }

    @Test
    void findByFloorReturnsListAndPage() {
        Ward ward = saveCardiologyWard();

        saveRoomsWithDifferentValues(ward);

        List<Room> list =
                roomRepository.findByFloor(2);

        Page<Room> page =
                roomRepository.findByFloor(
                        2,
                        firstPage()
                );

        assertEquals(2, list.size());
        assertEquals(2L, page.getTotalElements());
        assertEquals(
                2,
                page.getContent().getFirst().getFloor()
        );
    }

    @Test
    void findByWardAndStatusReturnsListAndPage() {
        Ward ward = saveCardiologyWard();

        saveRoomsWithDifferentValues(ward);

        List<Room> list =
                roomRepository.findByWard_IdAndStatus(
                        ward.getId(),
                        RoomStatus.AVAILABLE
                );

        Page<Room> page =
                roomRepository.findByWard_IdAndStatus(
                        ward.getId(),
                        RoomStatus.AVAILABLE,
                        firstPage()
                );

        assertEquals(2, list.size());
        assertEquals(2L, page.getTotalElements());

        assertTrue(
                list.stream().allMatch(room ->
                        room.getWard().getId()
                                .equals(ward.getId())
                                && room.getStatus()
                                == RoomStatus.AVAILABLE
                )
        );
    }

    @Test
    void findByDepartmentReturnsListAndPage() {
        Department cardiology =
                saveDepartment(
                        DepartmentType.CARDIOLOGY,
                        "Floor 2"
                );

        Department emergency =
                saveDepartment(
                        DepartmentType.EMERGENCY,
                        "Floor 1"
                );

        Ward cardiologyWard =
                saveWard(
                        "Cardiology Ward",
                        cardiology
                );

        Ward emergencyWard =
                saveWard(
                        "Emergency Ward",
                        emergency
                );

        roomRepository.save(
                room(
                        "CARD-201",
                        2,
                        RoomType.CARDIOLOGY_ICU,
                        RoomStatus.AVAILABLE,
                        cardiologyWard
                )
        );

        roomRepository.save(
                room(
                        "CARD-202",
                        2,
                        RoomType.CARDIOLOGY_ROOM,
                        RoomStatus.OCCUPIED,
                        cardiologyWard
                )
        );

        roomRepository.save(
                room(
                        "ER-101",
                        1,
                        RoomType.EMERGENCY_OBSERVATION,
                        RoomStatus.AVAILABLE,
                        emergencyWard
                )
        );

        roomRepository.flush();

        List<Room> list =
                roomRepository.findByWard_Department_Id(
                        cardiology.getId()
                );

        Page<Room> page =
                roomRepository.findByWard_Department_Id(
                        cardiology.getId(),
                        firstPage()
                );

        assertEquals(2, list.size());
        assertEquals(2L, page.getTotalElements());

        assertTrue(
                list.stream().allMatch(room ->
                        room.getWard()
                                .getDepartment()
                                .getId()
                                .equals(cardiology.getId())
                )
        );
    }

    @Test
    void findByDepartmentAndStatusReturnsListAndPage() {
        Department cardiology =
                saveDepartment(
                        DepartmentType.CARDIOLOGY,
                        "Floor 2"
                );

        Ward ward =
                saveWard(
                        "Cardiology Ward",
                        cardiology
                );

        saveRoomsWithDifferentValues(ward);

        List<Room> list =
                roomRepository
                        .findByWard_Department_IdAndStatus(
                                cardiology.getId(),
                                RoomStatus.AVAILABLE
                        );

        Page<Room> page =
                roomRepository
                        .findByWard_Department_IdAndStatus(
                                cardiology.getId(),
                                RoomStatus.AVAILABLE,
                                firstPage()
                        );

        assertEquals(2, list.size());
        assertEquals(2L, page.getTotalElements());
        assertEquals(2, page.getTotalPages());
    }

    @Test
    void countByStatusTypeAndFloorReturnsCorrectValues() {
        Ward ward = saveCardiologyWard();

        saveRoomsWithDifferentValues(ward);

        assertEquals(
                2L,
                roomRepository.countByStatus(
                        RoomStatus.AVAILABLE
                )
        );

        assertEquals(
                2L,
                roomRepository.countByRoomType(
                        RoomType.CARDIOLOGY_ICU
                )
        );

        assertEquals(
                2L,
                roomRepository.countByFloor(2)
        );
    }

    @Test
    void countByWardAndStatusReturnsCorrectValues() {
        Ward ward = saveCardiologyWard();

        saveRoomsWithDifferentValues(ward);

        assertEquals(
                3L,
                roomRepository.countByWard_Id(
                        ward.getId()
                )
        );

        assertEquals(
                2L,
                roomRepository.countByWard_IdAndStatus(
                        ward.getId(),
                        RoomStatus.AVAILABLE
                )
        );
    }

    @Test
    void countByDepartmentAndStatusReturnsCorrectValues() {
        Department department =
                saveDepartment(
                        DepartmentType.CARDIOLOGY,
                        "Floor 2"
                );

        Ward ward =
                saveWard(
                        "Cardiology Ward",
                        department
                );

        saveRoomsWithDifferentValues(ward);

        assertEquals(
                3L,
                roomRepository.countByWard_Department_Id(
                        department.getId()
                )
        );

        assertEquals(
                2L,
                roomRepository
                        .countByWard_Department_IdAndStatus(
                                department.getId(),
                                RoomStatus.AVAILABLE
                        )
        );
    }

    @Test
    void countBedsByStatusReturnsCorrectCount() {
        Ward ward = saveCardiologyWard();

        Room room =
                roomRepository.saveAndFlush(
                        room(
                                "CARD-201",
                                2,
                                RoomType.CARDIOLOGY_ICU,
                                RoomStatus.AVAILABLE,
                                ward
                        )
                );

        bedRepository.save(
                bed(
                        "BED-001",
                        room,
                        BedStatus.AVAILABLE
                )
        );

        bedRepository.save(
                bed(
                        "BED-002",
                        room,
                        BedStatus.AVAILABLE
                )
        );

        bedRepository.save(
                bed(
                        "BED-003",
                        room,
                        BedStatus.OCCUPIED
                )
        );

        bedRepository.flush();

        assertEquals(
                2L,
                roomRepository.countBedsByStatus(
                        BedStatus.AVAILABLE
                )
        );

        assertEquals(
                1L,
                roomRepository.countBedsByStatus(
                        BedStatus.OCCUPIED
                )
        );
    }

    @Test
    void findByIdForUpdateReturnsRoomWithPessimisticLock() {
        Ward ward = saveCardiologyWard();

        Room saved =
                roomRepository.saveAndFlush(
                        room(
                                "CARD-201",
                                2,
                                RoomType.CARDIOLOGY_ICU,
                                RoomStatus.AVAILABLE,
                                ward
                        )
                );

        entityManager.clear();

        Room result =
                roomRepository.findByIdForUpdate(
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
    void findByIdForUpdateReturnsEmptyForMissingRoom() {
        Optional<Room> result =
                roomRepository.findByIdForUpdate(
                        999999L
                );

        assertTrue(result.isEmpty());
    }

    private void saveRoomsWithDifferentValues(
            Ward ward
    ) {
        roomRepository.save(
                room(
                        "CARD-201",
                        2,
                        RoomType.CARDIOLOGY_ICU,
                        RoomStatus.AVAILABLE,
                        ward
                )
        );

        roomRepository.save(
                room(
                        "CARD-202",
                        2,
                        RoomType.CARDIOLOGY_ICU,
                        RoomStatus.OCCUPIED,
                        ward
                )
        );

        roomRepository.save(
                room(
                        "CARD-301",
                        3,
                        RoomType.CARDIOLOGY_ROOM,
                        RoomStatus.AVAILABLE,
                        ward
                )
        );

        roomRepository.flush();
    }

    private PageRequest firstPage() {
        return PageRequest.of(
                0,
                1,
                Sort.by("number").ascending()
        );
    }

    private Department saveDepartment(
            DepartmentType type,
            String location
    ) {
        return departmentRepository.saveAndFlush(
                Department.builder()
                        .departmentType(type)
                        .location(location)
                        .phoneExtension("100")
                        .description(
                                type + " department"
                        )
                        .isActive(true)
                        .build()
        );
    }

    private Ward saveWard(
            String name,
            Department department
    ) {
        return wardRepository.saveAndFlush(
                Ward.builder()
                        .name(name)
                        .description(name + " description")
                        .isActive(true)
                        .department(department)
                        .build()
        );
    }

    private Ward saveCardiologyWard() {
        Department department =
                saveDepartment(
                        DepartmentType.CARDIOLOGY,
                        "Floor 2"
                );

        return saveWard(
                "Cardiology Ward",
                department
        );
    }

    private Room room(
            String number,
            Integer floor,
            RoomType roomType,
            RoomStatus status,
            Ward ward
    ) {
        return Room.builder()
                .number(number)
                .floor(floor)
                .roomType(roomType)
                .status(status)
                .capacity(4)
                .ward(ward)
                .notes(number + " notes")
                .build();
    }

    private Bed bed(
            String bedNumber,
            Room room,
            BedStatus status
    ) {
        return Bed.builder()
                .bedNumber(bedNumber)
                .room(room)
                .status(status)
                .notes(bedNumber + " notes")
                .build();
    }
}


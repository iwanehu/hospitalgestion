package com.hospital.gestion.api.bed.repository;

import com.hospital.gestion.api.bed.entity.Bed;
import com.hospital.gestion.api.common.enums.BedStatus;
import com.hospital.gestion.api.common.enums.DepartmentType;
import com.hospital.gestion.api.common.enums.RoomStatus;
import com.hospital.gestion.api.common.enums.RoomType;
import com.hospital.gestion.api.department.entity.Department;
import com.hospital.gestion.api.department.repository.DepartmentRepository;
import com.hospital.gestion.api.room.entity.Room;
import com.hospital.gestion.api.room.repository.RoomRepository;
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
class BedRepositoryTest {

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
    void savePersistsBedAndGeneratesMetadata() {
        Hierarchy hierarchy =
                hierarchy(
                        DepartmentType.CARDIOLOGY,
                        "Cardiology Ward",
                        "CARD-201"
                );

        Bed saved =
                bedRepository.saveAndFlush(
                        bed(
                                "BED-001",
                                hierarchy.room(),
                                BedStatus.AVAILABLE
                        )
                );

        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());
        assertEquals("BED-001", saved.getBedNumber());
        assertEquals(BedStatus.AVAILABLE, saved.getStatus());
        assertEquals(
                hierarchy.room().getId(),
                saved.getRoom().getId()
        );
    }

    @Test
    void findByRoomReturnsListAndPage() {
        Hierarchy hierarchy =
                hierarchy(
                        DepartmentType.CARDIOLOGY,
                        "Cardiology Ward",
                        "CARD-201"
                );

        Room otherRoom =
                saveRoom(
                        "CARD-202",
                        hierarchy.ward()
                );

        saveBed(
                "BED-002",
                hierarchy.room(),
                BedStatus.AVAILABLE
        );
        saveBed(
                "BED-001",
                hierarchy.room(),
                BedStatus.OCCUPIED
        );
        saveBed(
                "BED-003",
                otherRoom,
                BedStatus.AVAILABLE
        );

        List<Bed> list =
                bedRepository.findByRoom_Id(
                        hierarchy.room().getId()
                );

        Page<Bed> page =
                bedRepository.findByRoom_Id(
                        hierarchy.room().getId(),
                        firstPage()
                );

        assertEquals(2, list.size());
        assertEquals(1, page.getNumberOfElements());
        assertEquals(2L, page.getTotalElements());
        assertEquals(2, page.getTotalPages());
        assertEquals(
                "BED-001",
                page.getContent().getFirst()
                        .getBedNumber()
        );
    }

    @Test
    void findByStatusReturnsListAndPage() {
        Hierarchy hierarchy =
                hierarchy(
                        DepartmentType.CARDIOLOGY,
                        "Cardiology Ward",
                        "CARD-201"
                );

        saveBedsWithDifferentStatuses(
                hierarchy.room()
        );

        List<Bed> list =
                bedRepository.findByStatus(
                        BedStatus.AVAILABLE
                );

        Page<Bed> page =
                bedRepository.findByStatus(
                        BedStatus.AVAILABLE,
                        firstPage()
                );

        assertEquals(2, list.size());
        assertEquals(2L, page.getTotalElements());
        assertEquals(2, page.getTotalPages());

        assertTrue(
                list.stream().allMatch(bed ->
                        bed.getStatus()
                                == BedStatus.AVAILABLE
                )
        );
    }

    @Test
    void findByRoomAndStatusReturnsListAndPage() {
        Hierarchy hierarchy =
                hierarchy(
                        DepartmentType.CARDIOLOGY,
                        "Cardiology Ward",
                        "CARD-201"
                );

        saveBedsWithDifferentStatuses(
                hierarchy.room()
        );

        List<Bed> list =
                bedRepository.findByRoom_IdAndStatus(
                        hierarchy.room().getId(),
                        BedStatus.AVAILABLE
                );

        Page<Bed> page =
                bedRepository.findByRoom_IdAndStatus(
                        hierarchy.room().getId(),
                        BedStatus.AVAILABLE,
                        firstPage()
                );

        assertEquals(2, list.size());
        assertEquals(2L, page.getTotalElements());

        assertTrue(
                list.stream().allMatch(bed ->
                        bed.getRoom().getId()
                                .equals(hierarchy.room().getId())
                                && bed.getStatus()
                                == BedStatus.AVAILABLE
                )
        );
    }

    @Test
    void findByWardReturnsListAndPage() {
        Department department =
                saveDepartment(
                        DepartmentType.CARDIOLOGY
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

        Room firstRoom =
                saveRoom("CARD-201", targetWard);

        Room secondRoom =
                saveRoom("CARD-202", targetWard);

        Room otherRoom =
                saveRoom("OBS-101", otherWard);

        saveBed(
                "BED-001",
                firstRoom,
                BedStatus.AVAILABLE
        );
        saveBed(
                "BED-002",
                secondRoom,
                BedStatus.OCCUPIED
        );
        saveBed(
                "BED-003",
                otherRoom,
                BedStatus.AVAILABLE
        );

        List<Bed> list =
                bedRepository.findByRoom_Ward_Id(
                        targetWard.getId()
                );

        Page<Bed> page =
                bedRepository.findByRoom_Ward_Id(
                        targetWard.getId(),
                        firstPage()
                );

        assertEquals(2, list.size());
        assertEquals(2L, page.getTotalElements());
        assertEquals(2, page.getTotalPages());
    }

    @Test
    void findByWardAndStatusReturnsListAndPage() {
        Hierarchy hierarchy =
                hierarchy(
                        DepartmentType.CARDIOLOGY,
                        "Cardiology Ward",
                        "CARD-201"
                );

        Room secondRoom =
                saveRoom(
                        "CARD-202",
                        hierarchy.ward()
                );

        saveBed(
                "BED-001",
                hierarchy.room(),
                BedStatus.AVAILABLE
        );
        saveBed(
                "BED-002",
                secondRoom,
                BedStatus.AVAILABLE
        );
        saveBed(
                "BED-003",
                secondRoom,
                BedStatus.OCCUPIED
        );

        List<Bed> list =
                bedRepository
                        .findByRoom_Ward_IdAndStatus(
                                hierarchy.ward().getId(),
                                BedStatus.AVAILABLE
                        );

        Page<Bed> page =
                bedRepository
                        .findByRoom_Ward_IdAndStatus(
                                hierarchy.ward().getId(),
                                BedStatus.AVAILABLE,
                                firstPage()
                        );

        assertEquals(2, list.size());
        assertEquals(2L, page.getTotalElements());
    }

    @Test
    void findByDepartmentReturnsListAndPage() {
        Hierarchy cardiology =
                hierarchy(
                        DepartmentType.CARDIOLOGY,
                        "Cardiology Ward",
                        "CARD-201"
                );

        Hierarchy emergency =
                hierarchy(
                        DepartmentType.EMERGENCY,
                        "Emergency Ward",
                        "ER-101"
                );

        saveBed(
                "BED-001",
                cardiology.room(),
                BedStatus.AVAILABLE
        );
        saveBed(
                "BED-002",
                cardiology.room(),
                BedStatus.OCCUPIED
        );
        saveBed(
                "BED-003",
                emergency.room(),
                BedStatus.AVAILABLE
        );

        List<Bed> list =
                bedRepository
                        .findByRoom_Ward_Department_Id(
                                cardiology.department().getId()
                        );

        Page<Bed> page =
                bedRepository
                        .findByRoom_Ward_Department_Id(
                                cardiology.department().getId(),
                                firstPage()
                        );

        assertEquals(2, list.size());
        assertEquals(2L, page.getTotalElements());

        assertTrue(
                list.stream().allMatch(bed ->
                        bed.getRoom()
                                .getWard()
                                .getDepartment()
                                .getId()
                                .equals(
                                        cardiology.department()
                                                .getId()
                                )
                )
        );
    }

    @Test
    void findByDepartmentAndStatusReturnsListAndPage() {
        Hierarchy hierarchy =
                hierarchy(
                        DepartmentType.CARDIOLOGY,
                        "Cardiology Ward",
                        "CARD-201"
                );

        saveBedsWithDifferentStatuses(
                hierarchy.room()
        );

        List<Bed> list =
                bedRepository
                        .findByRoom_Ward_Department_IdAndStatus(
                                hierarchy.department().getId(),
                                BedStatus.AVAILABLE
                        );

        Page<Bed> page =
                bedRepository
                        .findByRoom_Ward_Department_IdAndStatus(
                                hierarchy.department().getId(),
                                BedStatus.AVAILABLE,
                                firstPage()
                        );

        assertEquals(2, list.size());
        assertEquals(2L, page.getTotalElements());
        assertEquals(2, page.getTotalPages());
    }

    @Test
    void findAndExistsByBedNumberAndRoomIgnoreCase() {
        Hierarchy hierarchy =
                hierarchy(
                        DepartmentType.CARDIOLOGY,
                        "Cardiology Ward",
                        "CARD-201"
                );

        saveBed(
                "BED-001",
                hierarchy.room(),
                BedStatus.AVAILABLE
        );

        Optional<Bed> result =
                bedRepository
                        .findByBedNumberIgnoreCaseAndRoom_Id(
                                "bed-001",
                                hierarchy.room().getId()
                        );

        assertTrue(result.isPresent());
        assertEquals(
                "BED-001",
                result.get().getBedNumber()
        );

        assertTrue(
                bedRepository
                        .existsByBedNumberIgnoreCaseAndRoom_Id(
                                "bed-001",
                                hierarchy.room().getId()
                        )
        );

        assertFalse(
                bedRepository
                        .existsByBedNumberIgnoreCaseAndRoom_Id(
                                "BED-999",
                                hierarchy.room().getId()
                        )
        );
    }

    @Test
    void bedNumberSearchIsRestrictedToRoom() {
        Hierarchy hierarchy =
                hierarchy(
                        DepartmentType.CARDIOLOGY,
                        "Cardiology Ward",
                        "CARD-201"
                );

        Room otherRoom =
                saveRoom(
                        "CARD-202",
                        hierarchy.ward()
                );

        saveBed(
                "BED-001",
                hierarchy.room(),
                BedStatus.AVAILABLE
        );

        saveBed(
                "BED-001",
                otherRoom,
                BedStatus.OCCUPIED
        );

        Bed result =
                bedRepository
                        .findByBedNumberIgnoreCaseAndRoom_Id(
                                "bed-001",
                                otherRoom.getId()
                        )
                        .orElseThrow();

        assertEquals(
                otherRoom.getId(),
                result.getRoom().getId()
        );
        assertEquals(
                BedStatus.OCCUPIED,
                result.getStatus()
        );
    }

    @Test
    void findByBedNumberContainingIgnoreCaseReturnsListAndPage() {
        Hierarchy hierarchy =
                hierarchy(
                        DepartmentType.CARDIOLOGY,
                        "Cardiology Ward",
                        "CARD-201"
                );

        saveBed(
                "BED-102",
                hierarchy.room(),
                BedStatus.AVAILABLE
        );

        saveBed(
                "BED-101",
                hierarchy.room(),
                BedStatus.AVAILABLE
        );

        saveBed(
                "BED-900",
                hierarchy.room(),
                BedStatus.OCCUPIED
        );

        List<Bed> list =
                bedRepository
                        .findByBedNumberContainingIgnoreCase(
                                "bed-10"
                        );

        Page<Bed> page =
                bedRepository
                        .findByBedNumberContainingIgnoreCase(
                                "BED-10",
                                firstPage()
                        );

        assertEquals(2, list.size());
        assertEquals(1, page.getNumberOfElements());
        assertEquals(2L, page.getTotalElements());
        assertEquals(2, page.getTotalPages());

        assertEquals(
                "BED-101",
                page.getContent()
                        .getFirst()
                        .getBedNumber()
        );
    }


    @Test
    void countByStatusReturnsCorrectValue() {
        Hierarchy hierarchy =
                hierarchy(
                        DepartmentType.CARDIOLOGY,
                        "Cardiology Ward",
                        "CARD-201"
                );

        saveBedsWithDifferentStatuses(
                hierarchy.room()
        );

        assertEquals(
                2L,
                bedRepository.countByStatus(
                        BedStatus.AVAILABLE
                )
        );

        assertEquals(
                1L,
                bedRepository.countByStatus(
                        BedStatus.OCCUPIED
                )
        );
    }

    @Test
    void countByRoomAndStatusReturnsCorrectValues() {
        Hierarchy hierarchy =
                hierarchy(
                        DepartmentType.CARDIOLOGY,
                        "Cardiology Ward",
                        "CARD-201"
                );

        Room secondRoom =
                saveRoom(
                        "CARD-202",
                        hierarchy.ward()
                );

        saveBed(
                "BED-001",
                hierarchy.room(),
                BedStatus.AVAILABLE
        );
        saveBed(
                "BED-002",
                hierarchy.room(),
                BedStatus.OCCUPIED
        );
        saveBed(
                "BED-003",
                secondRoom,
                BedStatus.AVAILABLE
        );

        assertEquals(
                2L,
                bedRepository.countByRoom_Id(
                        hierarchy.room().getId()
                )
        );

        assertEquals(
                1L,
                bedRepository.countByRoom_IdAndStatus(
                        hierarchy.room().getId(),
                        BedStatus.AVAILABLE
                )
        );
    }

    @Test
    void countByWardAndDepartmentReturnsCorrectValues() {
        Department cardiology =
                saveDepartment(
                        DepartmentType.CARDIOLOGY
                );

        Ward firstWard =
                saveWard(
                        "Cardiology Ward",
                        cardiology
                );

        Ward secondWard =
                saveWard(
                        "Observation Ward",
                        cardiology
                );

        Room firstRoom =
                saveRoom("CARD-201", firstWard);

        Room secondRoom =
                saveRoom("OBS-201", secondWard);

        saveBed(
                "BED-001",
                firstRoom,
                BedStatus.AVAILABLE
        );
        saveBed(
                "BED-002",
                firstRoom,
                BedStatus.OCCUPIED
        );
        saveBed(
                "BED-003",
                secondRoom,
                BedStatus.AVAILABLE
        );

        assertEquals(
                2L,
                bedRepository.countByRoom_Ward_Id(
                        firstWard.getId()
                )
        );

        assertEquals(
                1L,
                bedRepository
                        .countByRoom_Ward_IdAndStatus(
                                firstWard.getId(),
                                BedStatus.AVAILABLE
                        )
        );

        assertEquals(
                3L,
                bedRepository
                        .countByRoom_Ward_Department_Id(
                                cardiology.getId()
                        )
        );

        assertEquals(
                2L,
                bedRepository
                        .countByRoom_Ward_Department_IdAndStatus(
                                cardiology.getId(),
                                BedStatus.AVAILABLE
                        )
        );
    }

    @Test
    void findByIdForUpdateReturnsBedWithPessimisticLock() {
        Hierarchy hierarchy =
                hierarchy(
                        DepartmentType.CARDIOLOGY,
                        "Cardiology Ward",
                        "CARD-201"
                );

        Bed saved =
                saveBed(
                        "BED-001",
                        hierarchy.room(),
                        BedStatus.AVAILABLE
                );

        entityManager.clear();

        Bed result =
                bedRepository.findByIdForUpdate(
                                saved.getId()
                        )
                        .orElseThrow();

        assertEquals(saved.getId(), result.getId());

        assertEquals(
                LockModeType.PESSIMISTIC_WRITE,
                entityManager.getLockMode(result)
        );
    }

    private void saveBedsWithDifferentStatuses(
            Room room
    ) {
        saveBed(
                "BED-001",
                room,
                BedStatus.AVAILABLE
        );
        saveBed(
                "BED-002",
                room,
                BedStatus.OCCUPIED
        );
        saveBed(
                "BED-003",
                room,
                BedStatus.AVAILABLE
        );
    }

    private PageRequest firstPage() {
        return PageRequest.of(
                0,
                1,
                Sort.by("bedNumber").ascending()
        );
    }

    private Hierarchy hierarchy(
            DepartmentType departmentType,
            String wardName,
            String roomNumber
    ) {
        Department department =
                saveDepartment(departmentType);

        Ward ward =
                saveWard(
                        wardName,
                        department
                );

        Room room =
                saveRoom(
                        roomNumber,
                        ward
                );

        return new Hierarchy(
                department,
                ward,
                room
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

    private Room saveRoom(
            String number,
            Ward ward
    ) {
        return roomRepository.saveAndFlush(
                Room.builder()
                        .number(number)
                        .floor(2)
                        .roomType(RoomType.CARDIOLOGY_ICU)
                        .status(RoomStatus.AVAILABLE)
                        .capacity(10)
                        .ward(ward)
                        .notes(number + " notes")
                        .build()
        );
    }

    private Bed saveBed(
            String bedNumber,
            Room room,
            BedStatus status
    ) {
        return bedRepository.saveAndFlush(
                bed(
                        bedNumber,
                        room,
                        status
                )
        );
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

    private record Hierarchy(
            Department department,
            Ward ward,
            Room room
    ) {
    }
}

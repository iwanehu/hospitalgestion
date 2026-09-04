package com.hospital.gestion.api.bed.integration;

import com.hospital.gestion.api.bed.entity.Bed;
import com.hospital.gestion.api.bed.repository.BedRepository;
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
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class BedControllerIntegrationTest {

    private static final String TEST_BED_NUMBER =
            "BED-101";

    private static final DepartmentType TEST_DEPARTMENT_TYPE =
            DepartmentType.LABORATORY;

    private static final String JWT_SECRET =
            "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";

    @Container
    static final PostgreSQLContainer POSTGRES =
            new PostgreSQLContainer(
                    "postgres:18.4-alpine"
            )
                    .withDatabaseName(
                            "hospital_bed_controller_test"
                    )
                    .withUsername("hospital_test")
                    .withPassword("hospital_test");

    @DynamicPropertySource
    static void configureProperties(
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
        registry.add(
                "security.jwt.secret",
                () -> JWT_SECRET
        );
        registry.add(
                "security.jwt.expiration-ms",
                () -> 900_000L
        );
        registry.add(
                "security.jwt.issuer",
                () -> "hospital-integration-test"
        );
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BedRepository bedRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private WardRepository wardRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    private Department department;
    private Ward ward;
    private Room room;

    @BeforeEach
    void setUp() {
        removeTestData();

        department = saveDepartment();
        ward = saveWard();
        room = saveRoom();
    }

    @AfterEach
    void cleanUp() {
        removeTestData();
    }

    @Test
    void authorizedUsersCanCompleteBedLifecycle()
            throws Exception {

        MvcResult createResult =
                mockMvc.perform(
                                post("/api/beds")
                                        .with(admin())
                                        .contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                        .content(
                                                validCreateBody(
                                                        room.getId()
                                                )
                                        )
                        )
                        .andExpect(status().isCreated())
                        .andExpect(
                                content().contentTypeCompatibleWith(
                                        MediaType.APPLICATION_JSON
                                )
                        )
                        .andExpect(
                                header().exists("Location")
                        )
                        .andExpect(
                                jsonPath("$.id").isNumber()
                        )
                        .andExpect(
                                jsonPath("$.bedNumber")
                                        .value(TEST_BED_NUMBER)
                        )
                        .andExpect(
                                jsonPath("$.status")
                                        .value("AVAILABLE")
                        )
                        .andExpect(
                                jsonPath("$.isOccupied")
                                        .value(false)
                        )
                        .andExpect(
                                jsonPath("$.roomId")
                                        .value(room.getId())
                        )
                        .andExpect(
                                jsonPath("$.roomNumber")
                                        .value("801")
                        )
                        .andExpect(
                                jsonPath("$.roomFloor")
                                        .value(8)
                        )
                        .andExpect(
                                jsonPath("$.wardId")
                                        .value(ward.getId())
                        )
                        .andExpect(
                                jsonPath("$.departmentId")
                                        .value(department.getId())
                        )
                        .andExpect(
                                jsonPath("$.departmentType")
                                        .value("LABORATORY")
                        )
                        .andExpect(
                                jsonPath("$.notes")
                                        .value("Integration bed")
                        )
                        .andExpect(
                                jsonPath("$.createdAt")
                                        .exists()
                        )
                        .andReturn();

        Number bedId =
                JsonPath.read(
                        createResult.getResponse()
                                .getContentAsString(),
                        "$.id"
                );

        long id = bedId.longValue();
        String endpoint = "/api/beds/" + id;

        assertTrue(bedRepository.existsById(id));

        mockMvc.perform(
                        get(endpoint)
                                .with(doctor())
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id").value(id)
                )
                .andExpect(
                        jsonPath("$.bedNumber")
                                .value(TEST_BED_NUMBER)
                );

        // AVAILABLE -> RESERVED
        mockMvc.perform(
                        patch(endpoint + "/reserve")
                                .with(receptionist())
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.status")
                                .value("RESERVED")
                )
                .andExpect(
                        jsonPath("$.isOccupied")
                                .value(false)
                );

        // RESERVED -> AVAILABLE
        mockMvc.perform(
                        patch(
                                endpoint
                                        + "/cancel-reservation"
                        )
                                .with(nurse())
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.status")
                                .value("AVAILABLE")
                );

        // AVAILABLE -> OCCUPIED
        mockMvc.perform(
                        patch(endpoint + "/occupy")
                                .with(admin())
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.status")
                                .value("OCCUPIED")
                )
                .andExpect(
                        jsonPath("$.isOccupied")
                                .value(true)
                );

        // OCCUPIED -> CLEANING
        mockMvc.perform(
                        patch(endpoint + "/release")
                                .with(admin())
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.status")
                                .value("CLEANING")
                )
                .andExpect(
                        jsonPath("$.isOccupied")
                                .value(false)
                );

        // CLEANING -> AVAILABLE
        mockMvc.perform(
                        patch(
                                endpoint
                                        + "/finish-cleaning"
                        )
                                .with(nurse())
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.status")
                                .value("AVAILABLE")
                );

        // AVAILABLE -> MAINTENANCE
        mockMvc.perform(
                        patch(endpoint + "/maintenance")
                                .with(nurse())
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.status")
                                .value("MAINTENANCE")
                );

        // MAINTENANCE -> AVAILABLE
        mockMvc.perform(
                        patch(
                                endpoint
                                        + "/finish-maintenance"
                        )
                                .with(nurse())
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.status")
                                .value("AVAILABLE")
                );

        mockMvc.perform(
                        put(endpoint)
                                .with(admin())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "bedNumber": "BED-102",
                                          "notes":
                                            "Updated integration bed"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id").value(id)
                )
                .andExpect(
                        jsonPath("$.bedNumber")
                                .value("BED-102")
                )
                .andExpect(
                        jsonPath("$.notes").value(
                                "Updated integration bed"
                        )
                );

        mockMvc.perform(
                        delete(endpoint)
                                .with(admin())
                )
                .andExpect(status().isNoContent());

        assertFalse(bedRepository.existsById(id));

        mockMvc.perform(
                        get(endpoint)
                                .with(doctor())
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void doctorCannotCreateBed()
            throws Exception {

        mockMvc.perform(
                        post("/api/beds")
                                .with(doctor())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        validCreateBody(
                                                room.getId()
                                        )
                                )
                )
                .andExpect(status().isForbidden())
                .andExpect(
                        jsonPath("$.status").value(403)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value("Access denied")
                );

        assertFalse(
                bedRepository
                        .existsByBedNumberIgnoreCaseAndRoom_Id(
                                TEST_BED_NUMBER,
                                room.getId()
                        )
        );
    }

    @Test
    void invalidBedNumberReturns400()
            throws Exception {

        mockMvc.perform(
                        post("/api/beds")
                                .with(admin())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "bedNumber": "101",
                                          "roomId": %d,
                                          "notes": "Invalid bed"
                                        }
                                        """.formatted(
                                        room.getId()
                                ))
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        content().contentTypeCompatibleWith(
                                MediaType.APPLICATION_JSON
                        )
                );

        assertFalse(
                bedRepository
                        .existsByBedNumberIgnoreCaseAndRoom_Id(
                                "101",
                                room.getId()
                        )
        );
    }

    @Test
    void duplicateBedNumberReturns409()
            throws Exception {

        saveBed(
                TEST_BED_NUMBER,
                BedStatus.AVAILABLE
        );

        mockMvc.perform(
                        post("/api/beds")
                                .with(admin())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        validCreateBody(
                                                room.getId()
                                        )
                                )
                )
                .andExpect(status().isConflict())
                .andExpect(
                        content().contentTypeCompatibleWith(
                                MediaType.APPLICATION_JSON
                        )
                );

        assertTrue(
                bedRepository
                        .existsByBedNumberIgnoreCaseAndRoom_Id(
                                TEST_BED_NUMBER,
                                room.getId()
                        )
        );
    }

    @Test
    void paginationFiltersBeds()
            throws Exception {

        saveBed(
                TEST_BED_NUMBER,
                BedStatus.AVAILABLE
        );

        mockMvc.perform(
                        get("/api/beds/page")
                                .with(doctor())
                                .param(
                                        "bedNumber",
                                        "BED"
                                )
                                .param(
                                        "status",
                                        "AVAILABLE"
                                )
                                .param(
                                        "roomId",
                                        room.getId().toString()
                                )
                                .param(
                                        "wardId",
                                        ward.getId().toString()
                                )
                                .param(
                                        "departmentId",
                                        department.getId()
                                                .toString()
                                )
                                .param("page", "0")
                                .param("size", "10")
                                .param(
                                        "sort",
                                        "bedNumber,asc"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        content().contentTypeCompatibleWith(
                                MediaType.APPLICATION_JSON
                        )
                )
                .andExpect(
                        jsonPath("$.content", hasSize(1))
                )
                .andExpect(
                        jsonPath("$.content[0].bedNumber")
                                .value(TEST_BED_NUMBER)
                )
                .andExpect(
                        jsonPath("$.content[0].status")
                                .value("AVAILABLE")
                )
                .andExpect(
                        jsonPath("$.content[0].roomId")
                                .value(room.getId())
                )
                .andExpect(
                        jsonPath("$.content[0].wardId")
                                .value(ward.getId())
                )
                .andExpect(
                        jsonPath("$.content[0].departmentId")
                                .value(department.getId())
                )
                .andExpect(
                        jsonPath("$.page").value(0)
                )
                .andExpect(
                        jsonPath("$.size").value(10)
                )
                .andExpect(
                        jsonPath("$.numberOfElements")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.totalElements")
                                .value(1)
                );
    }

    private RequestPostProcessor admin() {
        return user("admin@hospital.test")
                .roles("ADMIN");
    }

    private RequestPostProcessor doctor() {
        return user("doctor@hospital.test")
                .roles("DOCTOR");
    }

    private RequestPostProcessor nurse() {
        return user("nurse@hospital.test")
                .roles("NURSE");
    }

    private RequestPostProcessor receptionist() {
        return user("receptionist@hospital.test")
                .roles("RECEPTIONIST");
    }

    private Department saveDepartment() {
        Department entity =
                Department.builder()
                        .departmentType(
                                TEST_DEPARTMENT_TYPE
                        )
                        .location(
                                "Bed integration department"
                        )
                        .phoneExtension("801")
                        .description(
                                "Department for bed tests"
                        )
                        .isActive(true)
                        .build();

        return departmentRepository.saveAndFlush(
                entity
        );
    }

    private Ward saveWard() {
        Ward entity =
                Ward.builder()
                        .name("Bed Integration Ward")
                        .description(
                                "Ward for bed tests"
                        )
                        .isActive(true)
                        .department(department)
                        .build();

        return wardRepository.saveAndFlush(entity);
    }

    private Room saveRoom() {
        Room entity =
                Room.builder()
                        .number("801")
                        .floor(8)
                        .roomType(
                                RoomType.LABORATORY_PROCEDURE_ROOM
                        )
                        .status(RoomStatus.AVAILABLE)
                        .capacity(3)
                        .ward(ward)
                        .notes("Room for bed tests")
                        .build();

        return roomRepository.saveAndFlush(entity);
    }

    private Bed saveBed(
            String bedNumber,
            BedStatus status
    ) {
        Bed entity =
                Bed.builder()
                        .bedNumber(bedNumber)
                        .room(room)
                        .status(status)
                        .notes("Integration bed")
                        .build();

        return bedRepository.saveAndFlush(entity);
    }

    private void removeTestData() {
        departmentRepository
                .findByDepartmentType(
                        TEST_DEPARTMENT_TYPE
                )
                .ifPresent(existingDepartment -> {
                    wardRepository
                            .findByDepartment_Id(
                                    existingDepartment.getId()
                            )
                            .forEach(existingWard -> {
                                roomRepository
                                        .findByWard_Id(
                                                existingWard.getId()
                                        )
                                        .forEach(existingRoom -> {
                                            bedRepository
                                                    .findByRoom_Id(
                                                            existingRoom
                                                                    .getId()
                                                    )
                                                    .forEach(
                                                            bedRepository
                                                                    ::delete
                                                    );

                                            bedRepository.flush();

                                            roomRepository.delete(
                                                    existingRoom
                                            );
                                        });

                                roomRepository.flush();

                                wardRepository.delete(
                                        existingWard
                                );
                            });

                    wardRepository.flush();

                    departmentRepository.delete(
                            existingDepartment
                    );
                    departmentRepository.flush();
                });
    }

    private String validCreateBody(Long roomId) {
        return """
                {
                  "bedNumber": "BED-101",
                  "roomId": %d,
                  "notes": "Integration bed"
                }
                """.formatted(roomId);
    }
}

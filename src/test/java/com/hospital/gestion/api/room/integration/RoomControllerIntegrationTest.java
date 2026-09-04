package com.hospital.gestion.api.room.integration;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class RoomControllerIntegrationTest {

    private static final String TEST_ROOM_NUMBER = "501";

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
                            "hospital_room_controller_test"
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
    private RoomRepository roomRepository;

    @Autowired
    private WardRepository wardRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    private Department department;
    private Ward ward;

    @BeforeEach
    void setUp() {
        removeTestData();
        department = saveDepartment();
        ward = saveWard();
    }

    @AfterEach
    void cleanUp() {
        removeTestData();
    }

    @Test
    void adminCanCompleteRoomLifecycle()
            throws Exception {

        MvcResult createResult =
                mockMvc.perform(
                                post("/api/rooms")
                                        .with(admin())
                                        .contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                        .content(
                                                validCreateBody(
                                                        ward.getId()
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
                                jsonPath("$.number")
                                        .value(TEST_ROOM_NUMBER)
                        )
                        .andExpect(
                                jsonPath("$.floor").value(5)
                        )
                        .andExpect(
                                jsonPath("$.roomType").value(
                                        "LABORATORY_PROCEDURE_ROOM"
                                )
                        )
                        .andExpect(
                                jsonPath("$.status")
                                        .value("AVAILABLE")
                        )
                        .andExpect(
                                jsonPath("$.capacity").value(3)
                        )
                        .andExpect(
                                jsonPath("$.totalBeds").value(0)
                        )
                        .andExpect(
                                jsonPath("$.wardId")
                                        .value(ward.getId())
                        )
                        .andExpect(
                                jsonPath("$.wardName").value(
                                        "Room Integration Ward"
                                )
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
                                jsonPath("$.notes").value(
                                        "Integration room"
                                )
                        )
                        .andExpect(
                                jsonPath("$.createdAt").exists()
                        )
                        .andReturn();

        Number roomId =
                JsonPath.read(
                        createResult.getResponse()
                                .getContentAsString(),
                        "$.id"
                );

        long id = roomId.longValue();
        String endpoint = "/api/rooms/" + id;

        assertTrue(roomRepository.existsById(id));

        mockMvc.perform(
                        get(endpoint)
                                .with(doctor())
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id").value(id)
                )
                .andExpect(
                        jsonPath("$.number")
                                .value(TEST_ROOM_NUMBER)
                );

        mockMvc.perform(
                        put(endpoint)
                                .with(admin())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "number": "502",
                                          "floor": 6,
                                          "roomType":
                                            "LABORATORY_PROCEDURE_ROOM",
                                          "capacity": 4,
                                          "notes":
                                            "Updated integration room"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id").value(id)
                )
                .andExpect(
                        jsonPath("$.number").value("502")
                )
                .andExpect(
                        jsonPath("$.floor").value(6)
                )
                .andExpect(
                        jsonPath("$.capacity").value(4)
                )
                .andExpect(
                        jsonPath("$.notes").value(
                                "Updated integration room"
                        )
                );

        mockMvc.perform(
                        delete(endpoint)
                                .with(admin())
                )
                .andExpect(status().isNoContent());

        assertFalse(roomRepository.existsById(id));

        mockMvc.perform(
                        get(endpoint)
                                .with(doctor())
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void nonAdminCannotCreateRoom()
            throws Exception {

        mockMvc.perform(
                        post("/api/rooms")
                                .with(doctor())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        validCreateBody(
                                                ward.getId()
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
                roomRepository.existsByNumberIgnoreCase(
                        TEST_ROOM_NUMBER
                )
        );
    }

    @Test
    void invalidCreateRequestReturns400()
            throws Exception {

        mockMvc.perform(
                        post("/api/rooms")
                                .with(admin())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "number": "",
                                          "floor": 100,
                                          "roomType": null,
                                          "capacity": 0,
                                          "wardId": null,
                                          "notes": "Invalid"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        content().contentTypeCompatibleWith(
                                MediaType.APPLICATION_JSON
                        )
                );

        assertFalse(
                roomRepository.existsByNumberIgnoreCase(
                        TEST_ROOM_NUMBER
                )
        );
    }

    @Test
    void duplicateRoomNumberReturns409()
            throws Exception {

        saveRoom(TEST_ROOM_NUMBER);

        mockMvc.perform(
                        post("/api/rooms")
                                .with(admin())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        validCreateBody(
                                                ward.getId()
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
                roomRepository.existsByNumberIgnoreCase(
                        TEST_ROOM_NUMBER
                )
        );
    }

    @Test
    void paginationFiltersRooms()
            throws Exception {

        saveRoom(TEST_ROOM_NUMBER);

        mockMvc.perform(
                        get("/api/rooms/page")
                                .with(doctor())
                                .param("number", "50")
                                .param("floor", "5")
                                .param(
                                        "roomType",
                                        "LABORATORY_PROCEDURE_ROOM"
                                )
                                .param(
                                        "status",
                                        "AVAILABLE"
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
                                        "number,asc"
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
                        jsonPath("$.content[0].number")
                                .value(TEST_ROOM_NUMBER)
                )
                .andExpect(
                        jsonPath("$.content[0].floor")
                                .value(5)
                )
                .andExpect(
                        jsonPath("$.content[0].roomType").value(
                                "LABORATORY_PROCEDURE_ROOM"
                        )
                )
                .andExpect(
                        jsonPath("$.content[0].status")
                                .value("AVAILABLE")
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

    private Department saveDepartment() {
        Department entity =
                Department.builder()
                        .departmentType(
                                TEST_DEPARTMENT_TYPE
                        )
                        .location(
                                "Room integration department"
                        )
                        .phoneExtension("701")
                        .description(
                                "Department for room tests"
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
                        .name("Room Integration Ward")
                        .description(
                                "Ward for room integration tests"
                        )
                        .isActive(true)
                        .department(department)
                        .build();

        return wardRepository.saveAndFlush(entity);
    }

    private Room saveRoom(String number) {
        Room entity =
                Room.builder()
                        .number(number)
                        .floor(5)
                        .roomType(
                                RoomType.LABORATORY_PROCEDURE_ROOM
                        )
                        .status(RoomStatus.AVAILABLE)
                        .capacity(3)
                        .ward(ward)
                        .notes("Integration room")
                        .build();

        return roomRepository.saveAndFlush(entity);
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
                                        .forEach(
                                                roomRepository::delete
                                        );

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

    private String validCreateBody(Long wardId) {
        return """
                {
                  "number": "501",
                  "floor": 5,
                  "roomType":
                    "LABORATORY_PROCEDURE_ROOM",
                  "capacity": 3,
                  "wardId": %d,
                  "notes": "Integration room"
                }
                """.formatted(wardId);
    }
}


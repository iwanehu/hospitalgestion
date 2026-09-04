package com.hospital.gestion.api.ward.integration;

import com.hospital.gestion.api.common.enums.DepartmentType;
import com.hospital.gestion.api.department.entity.Department;
import com.hospital.gestion.api.department.repository.DepartmentRepository;
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
class WardControllerIntegrationTest {

    private static final DepartmentType TEST_DEPARTMENT_TYPE =
            DepartmentType.LABORATORY;

    private static final String TEST_WARD_NAME =
            "Integration Laboratory Ward";

    private static final String JWT_SECRET =
            "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";

    @Container
    static final PostgreSQLContainer POSTGRES =
            new PostgreSQLContainer(
                    "postgres:18.4-alpine"
            )
                    .withDatabaseName(
                            "hospital_ward_controller_test"
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
    private WardRepository wardRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    private Department department;

    @BeforeEach
    void setUp() {
        removeTestData();
        department = saveDepartment();
    }

    @AfterEach
    void cleanUp() {
        removeTestData();
    }

    @Test
    void adminCanCompleteWardLifecycle()
            throws Exception {

        MvcResult createResult =
                mockMvc.perform(
                                post("/api/wards")
                                        .with(admin())
                                        .contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                        .content(
                                                validCreateBody(
                                                        department.getId()
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
                                jsonPath("$.name")
                                        .value(TEST_WARD_NAME)
                        )
                        .andExpect(
                                jsonPath("$.description").value(
                                        "Integration ward description"
                                )
                        )
                        .andExpect(
                                jsonPath("$.isActive")
                                        .value(true)
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
                                jsonPath("$.totalRooms")
                                        .value(0)
                        )
                        .andExpect(
                                jsonPath("$.createdAt")
                                        .exists()
                        )
                        .andReturn();

        Number wardId =
                JsonPath.read(
                        createResult.getResponse()
                                .getContentAsString(),
                        "$.id"
                );

        long id = wardId.longValue();
        String endpoint = "/api/wards/" + id;

        assertTrue(wardRepository.existsById(id));

        mockMvc.perform(
                        get(endpoint)
                                .with(admin())
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id").value(id)
                )
                .andExpect(
                        jsonPath("$.name")
                                .value(TEST_WARD_NAME)
                );

        mockMvc.perform(
                        put(endpoint)
                                .with(admin())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "name":
                                            "Updated Integration Ward",
                                          "description":
                                            "Updated ward description",
                                          "isActive": true
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id").value(id)
                )
                .andExpect(
                        jsonPath("$.name").value(
                                "Updated Integration Ward"
                        )
                )
                .andExpect(
                        jsonPath("$.description").value(
                                "Updated ward description"
                        )
                )
                .andExpect(
                        jsonPath("$.isActive").value(true)
                );

        mockMvc.perform(
                        patch(endpoint + "/deactivate")
                                .with(admin())
                )
                .andExpect(status().isNoContent());

        mockMvc.perform(
                        get(endpoint)
                                .with(admin())
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.isActive").value(false)
                );

        mockMvc.perform(
                        patch(endpoint + "/activate")
                                .with(admin())
                )
                .andExpect(status().isNoContent());

        mockMvc.perform(
                        get(endpoint)
                                .with(admin())
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.isActive").value(true)
                );

        mockMvc.perform(
                        delete(endpoint)
                                .with(admin())
                )
                .andExpect(status().isNoContent());

        assertFalse(wardRepository.existsById(id));

        mockMvc.perform(
                        get(endpoint)
                                .with(admin())
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void nonAdminCannotCreateWard()
            throws Exception {

        mockMvc.perform(
                        post("/api/wards")
                                .with(doctor())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        validCreateBody(
                                                department.getId()
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
                wardRepository
                        .existsByNameIgnoreCaseAndDepartment_Id(
                                TEST_WARD_NAME,
                                department.getId()
                        )
        );
    }

    @Test
    void invalidCreateRequestReturns400()
            throws Exception {

        mockMvc.perform(
                        post("/api/wards")
                                .with(admin())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "name": "",
                                          "description": "Invalid",
                                          "departmentId": null
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
                wardRepository
                        .existsByNameIgnoreCaseAndDepartment_Id(
                                TEST_WARD_NAME,
                                department.getId()
                        )
        );
    }

    @Test
    void duplicateWardNameReturns409()
            throws Exception {

        saveWard(
                TEST_WARD_NAME,
                true
        );

        mockMvc.perform(
                        post("/api/wards")
                                .with(admin())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        validCreateBody(
                                                department.getId()
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
                wardRepository
                        .existsByNameIgnoreCaseAndDepartment_Id(
                                TEST_WARD_NAME,
                                department.getId()
                        )
        );
    }

    @Test
    void paginationFiltersWards()
            throws Exception {

        saveWard(
                TEST_WARD_NAME,
                true
        );

        mockMvc.perform(
                        get("/api/wards/page")
                                .with(doctor())
                                .param(
                                        "name",
                                        "Integration"
                                )
                                .param(
                                        "description",
                                        "integration"
                                )
                                .param(
                                        "isActive",
                                        "true"
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
                                        "name,asc"
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
                        jsonPath("$.content[0].name")
                                .value(TEST_WARD_NAME)
                )
                .andExpect(
                        jsonPath(
                                "$.content[0].departmentId"
                        ).value(department.getId())
                )
                .andExpect(
                        jsonPath("$.content[0].isActive")
                                .value(true)
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
                                "Ward integration department"
                        )
                        .phoneExtension("601")
                        .description(
                                "Department used by ward integration tests"
                        )
                        .isActive(true)
                        .build();

        return departmentRepository.saveAndFlush(
                entity
        );
    }

    private Ward saveWard(
            String name,
            boolean active
    ) {
        Ward entity =
                Ward.builder()
                        .name(name)
                        .description(
                                "Integration ward description"
                        )
                        .isActive(active)
                        .department(department)
                        .build();

        return wardRepository.saveAndFlush(entity);
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
                            .forEach(
                                    wardRepository::delete
                            );

                    wardRepository.flush();

                    departmentRepository.delete(
                            existingDepartment
                    );
                    departmentRepository.flush();
                });
    }

    private String validCreateBody(
            Long departmentId
    ) {
        return """
                {
                  "name": "Integration Laboratory Ward",
                  "description":
                    "Integration ward description",
                  "departmentId": %d
                }
                """.formatted(departmentId);
    }
}


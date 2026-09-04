package com.hospital.gestion.api.department.integration;

import com.hospital.gestion.api.common.enums.DepartmentType;
import com.hospital.gestion.api.department.entity.Department;
import com.hospital.gestion.api.department.repository.DepartmentRepository;
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
class DepartmentControllerIntegrationTest {

    private static final DepartmentType TEST_TYPE =
            DepartmentType.LABORATORY;

    private static final String TEST_LOCATION =
            "Integration Laboratory Floor";

    private static final String JWT_SECRET =
            "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";

    @Container
    static final PostgreSQLContainer POSTGRES =
            new PostgreSQLContainer(
                    "postgres:18.4-alpine"
            )
                    .withDatabaseName(
                            "hospital_department_controller_test"
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
    private DepartmentRepository departmentRepository;

    @BeforeEach
    void setUp() {
        removeTestDepartment();
    }

    @AfterEach
    void cleanUp() {
        removeTestDepartment();
    }

    @Test
    void adminCanCompleteDepartmentLifecycle()
            throws Exception {

        MvcResult createResult =
                mockMvc.perform(
                                post("/api/departments")
                                        .with(admin())
                                        .contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                        .content(validCreateBody())
                        )
                        .andExpect(status().isCreated())
                        .andExpect(
                                content().contentTypeCompatibleWith(
                                        MediaType.APPLICATION_JSON
                                )
                        )
                        .andExpect(
                                jsonPath("$.id").isNumber()
                        )
                        .andExpect(
                                jsonPath("$.departmentType")
                                        .value("LABORATORY")
                        )
                        .andExpect(
                                jsonPath("$.location")
                                        .value(TEST_LOCATION)
                        )
                        .andExpect(
                                jsonPath("$.phoneExtension")
                                        .value("501")
                        )
                        .andExpect(
                                jsonPath("$.description")
                                        .value(
                                                "Integration test laboratory"
                                        )
                        )
                        .andExpect(
                                jsonPath("$.isActive")
                                        .value(true)
                        )
                        .andExpect(
                                jsonPath("$.totalWards")
                                        .value(0)
                        )
                        .andExpect(
                                jsonPath("$.createdAt")
                                        .exists()
                        )
                        .andExpect(
                                header().exists("Location")
                        )
                        .andReturn();

        Number departmentId =
                JsonPath.read(
                        createResult.getResponse()
                                .getContentAsString(),
                        "$.id"
                );

        long id = departmentId.longValue();

        String endpoint =
                "/api/departments/" + id;

        assertTrue(
                departmentRepository
                        .existsByDepartmentType(TEST_TYPE)
        );

        mockMvc.perform(
                        get(endpoint)
                                .with(admin())
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id").value(id)
                )
                .andExpect(
                        jsonPath("$.departmentType")
                                .value("LABORATORY")
                )
                .andExpect(
                        jsonPath("$.location")
                                .value(TEST_LOCATION)
                );

        mockMvc.perform(
                        put(endpoint)
                                .with(admin())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "location":
                                            "Updated Integration Laboratory",
                                          "phoneExtension": "502",
                                          "description":
                                            "Updated integration description",
                                          "isActive": true
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id").value(id)
                )
                .andExpect(
                        jsonPath("$.location").value(
                                "Updated Integration Laboratory"
                        )
                )
                .andExpect(
                        jsonPath("$.phoneExtension")
                                .value("502")
                )
                .andExpect(
                        jsonPath("$.description").value(
                                "Updated integration description"
                        )
                )
                .andExpect(
                        jsonPath("$.isActive")
                                .value(true)
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
                        jsonPath("$.isActive")
                                .value(false)
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
                        jsonPath("$.isActive")
                                .value(true)
                );

        mockMvc.perform(
                        delete(endpoint)
                                .with(admin())
                )
                .andExpect(status().isNoContent());

        assertFalse(
                departmentRepository
                        .existsByDepartmentType(TEST_TYPE)
        );

        mockMvc.perform(
                        get(endpoint)
                                .with(admin())
                )
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath("$.status").value(404)
                )
                .andExpect(
                        jsonPath("$.timestamp").exists()
                );
    }

    @Test
    void nonAdminCannotCreateDepartment()
            throws Exception {

        mockMvc.perform(
                        post("/api/departments")
                                .with(doctor())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(validCreateBody())
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
                departmentRepository
                        .existsByDepartmentType(TEST_TYPE)
        );
    }

    @Test
    void authenticatedUserCanReadDepartments()
            throws Exception {

        saveTestDepartment(true);

        mockMvc.perform(
                        get("/api/departments")
                                .with(doctor())
                )
                .andExpect(status().isOk())
                .andExpect(
                        content().contentTypeCompatibleWith(
                                MediaType.APPLICATION_JSON
                        )
                )
                .andExpect(
                        jsonPath(
                                "$[?(@.departmentType == 'LABORATORY')]",
                                hasSize(1)
                        )
                );
    }

    @Test
    void invalidCreateRequestReturns400()
            throws Exception {

        mockMvc.perform(
                        post("/api/departments")
                                .with(admin())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "departmentType": null,
                                          "location": "",
                                          "phoneExtension":
                                            "12345678901",
                                          "description": "Invalid"
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
                departmentRepository
                        .existsByDepartmentType(TEST_TYPE)
        );
    }

    @Test
    void paginationFiltersDepartments()
            throws Exception {

        saveTestDepartment(true);

        mockMvc.perform(
                        get("/api/departments/page")
                                .with(doctor())
                                .param(
                                        "departmentType",
                                        "LABORATORY"
                                )
                                .param(
                                        "isActive",
                                        "true"
                                )
                                .param(
                                        "location",
                                        "Integration"
                                )
                                .param("page", "0")
                                .param("size", "10")
                                .param(
                                        "sort",
                                        "departmentType,asc"
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
                        jsonPath(
                                "$.content[0].departmentType"
                        ).value("LABORATORY")
                )
                .andExpect(
                        jsonPath("$.content[0].location")
                                .value(TEST_LOCATION)
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

    private Department saveTestDepartment(
            boolean active
    ) {
        Department department =
                Department.builder()
                        .departmentType(TEST_TYPE)
                        .location(TEST_LOCATION)
                        .phoneExtension("501")
                        .description(
                                "Integration test laboratory"
                        )
                        .isActive(active)
                        .build();

        return departmentRepository.saveAndFlush(
                department
        );
    }

    private void removeTestDepartment() {
        departmentRepository
                .findByDepartmentType(TEST_TYPE)
                .ifPresent(
                        departmentRepository::delete
                );

        departmentRepository.flush();
    }

    private String validCreateBody() {
        return """
                {
                  "departmentType": "LABORATORY",
                  "location": "Integration Laboratory Floor",
                  "phoneExtension": "501",
                  "description": "Integration test laboratory"
                }
                """;
    }
}
package com.hospital.gestion.api.receptionist.integration;

import com.hospital.gestion.api.common.enums.DepartmentType;
import com.hospital.gestion.api.common.enums.Role;
import com.hospital.gestion.api.department.entity.Department;
import com.hospital.gestion.api.department.repository.DepartmentRepository;
import com.hospital.gestion.api.receptionist.repository.ReceptionistRepository;
import com.hospital.gestion.api.user.entity.User;
import com.hospital.gestion.api.user.repository.UserRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
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
class ReceptionistControllerIntegrationTest {

    private static final String TEST_EMAIL =
            "receptionist.integration@hospital.test";

    private static final String TEST_DOCUMENT =
            "RECTEST001";

    private static final String TEST_DESK =
            "DESK-101";

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
                            "hospital_receptionist_controller_test"
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
    private ReceptionistRepository receptionistRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User receptionistUser;
    private Department department;

    @BeforeEach
    void setUp() {
        removeTestData();

        receptionistUser = saveReceptionistUser();
        department = saveDepartment();
    }

    @AfterEach
    void cleanUp() {
        removeTestData();
    }

    @Test
    void adminCanCompleteReceptionistLifecycle()
            throws Exception {

        MvcResult createResult =
                mockMvc.perform(
                                post("/api/receptionists")
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
                                header().exists("Location")
                        )
                        .andExpect(
                                jsonPath("$.id").isNumber()
                        )
                        .andExpect(
                                jsonPath("$.userId")
                                        .value(receptionistUser.getId())
                        )
                        .andExpect(
                                jsonPath("$.fullName").value(
                                        "Integration Receptionist"
                                )
                        )
                        .andExpect(
                                jsonPath("$.email")
                                        .value(TEST_EMAIL)
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
                                jsonPath("$.deskNumber")
                                        .value(TEST_DESK)
                        )
                        .andExpect(
                                jsonPath("$.shiftType")
                                        .value("MORNING")
                        )
                        .andExpect(
                                jsonPath("$.createdAt")
                                        .exists()
                        )
                        .andReturn();

        Number receptionistId =
                JsonPath.read(
                        createResult.getResponse()
                                .getContentAsString(),
                        "$.id"
                );

        long id = receptionistId.longValue();

        String endpoint =
                "/api/receptionists/" + id;

        assertTrue(
                receptionistRepository.existsById(id)
        );

        mockMvc.perform(
                        get(endpoint)
                                .with(receptionist())
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id").value(id)
                )
                .andExpect(
                        jsonPath("$.deskNumber")
                                .value(TEST_DESK)
                );

        mockMvc.perform(
                        put(endpoint)
                                .with(admin())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "departmentId": %d,
                                          "deskNumber": "DESK-202",
                                          "shiftType": "AFTERNOON"
                                        }
                                        """.formatted(
                                        department.getId()
                                ))
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id").value(id)
                )
                .andExpect(
                        jsonPath("$.departmentId")
                                .value(department.getId())
                )
                .andExpect(
                        jsonPath("$.deskNumber")
                                .value("DESK-202")
                )
                .andExpect(
                        jsonPath("$.shiftType")
                                .value("AFTERNOON")
                );

        mockMvc.perform(
                        delete(endpoint)
                                .with(admin())
                )
                .andExpect(status().isNoContent());

        assertFalse(
                receptionistRepository.existsById(id)
        );

        assertTrue(
                userRepository.existsById(
                        receptionistUser.getId()
                )
        );

        mockMvc.perform(
                        get(endpoint)
                                .with(receptionist())
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void nurseCannotAccessReceptionistController()
            throws Exception {

        mockMvc.perform(
                        post("/api/receptionists")
                                .with(nurse())
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
                receptionistRepository.existsByUser_Id(
                        receptionistUser.getId()
                )
        );
    }

    @Test
    void invalidDeskNumberReturns400()
            throws Exception {

        mockMvc.perform(
                        post("/api/receptionists")
                                .with(admin())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "userId": %d,
                                          "departmentId": %d,
                                          "deskNumber": "INVALID",
                                          "shiftType": "MORNING"
                                        }
                                        """.formatted(
                                        receptionistUser.getId(),
                                        department.getId()
                                ))
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        content().contentTypeCompatibleWith(
                                MediaType.APPLICATION_JSON
                        )
                );

        assertFalse(
                receptionistRepository.existsByUser_Id(
                        receptionistUser.getId()
                )
        );
    }

    @Test
    void duplicateReceptionistForSameUserReturns409()
            throws Exception {

        createReceptionistProfile();

        mockMvc.perform(
                        post("/api/receptionists")
                                .with(admin())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(validCreateBody())
                )
                .andExpect(status().isConflict())
                .andExpect(
                        content().contentTypeCompatibleWith(
                                MediaType.APPLICATION_JSON
                        )
                );

        assertTrue(
                receptionistRepository.existsByUser_Id(
                        receptionistUser.getId()
                )
        );
    }

    @Test
    void paginationFiltersReceptionists()
            throws Exception {

        createReceptionistProfile();

        mockMvc.perform(
                        get("/api/receptionists/page")
                                .with(receptionist())
                                .param(
                                        "text",
                                        "Integration"
                                )
                                .param(
                                        "departmentId",
                                        department.getId()
                                                .toString()
                                )
                                .param(
                                        "shiftType",
                                        "MORNING"
                                )
                                .param(
                                        "isActive",
                                        "true"
                                )
                                .param(
                                        "deskNumber",
                                        "DESK"
                                )
                                .param("page", "0")
                                .param("size", "10")
                                .param(
                                        "sort",
                                        "user.lastName,asc"
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
                        jsonPath("$.content[0].userId")
                                .value(receptionistUser.getId())
                )
                .andExpect(
                        jsonPath("$.content[0].email")
                                .value(TEST_EMAIL)
                )
                .andExpect(
                        jsonPath("$.content[0].departmentId")
                                .value(department.getId())
                )
                .andExpect(
                        jsonPath("$.content[0].deskNumber")
                                .value(TEST_DESK)
                )
                .andExpect(
                        jsonPath("$.content[0].shiftType")
                                .value("MORNING")
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

    private void createReceptionistProfile()
            throws Exception {

        mockMvc.perform(
                        post("/api/receptionists")
                                .with(admin())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(validCreateBody())
                )
                .andExpect(status().isCreated());
    }

    private RequestPostProcessor admin() {
        return user("admin@hospital.test")
                .roles("ADMIN");
    }

    private RequestPostProcessor receptionist() {
        return user("receptionist@hospital.test")
                .roles("RECEPTIONIST");
    }

    private RequestPostProcessor nurse() {
        return user("nurse@hospital.test")
                .roles("NURSE");
    }

    private User saveReceptionistUser() {
        User entity =
                User.builder()
                        .role(Role.RECEPTIONIST)
                        .email(TEST_EMAIL)
                        .password(
                                passwordEncoder.encode(
                                        "StrongPass123!"
                                )
                        )
                        .isActive(true)
                        .documentId(TEST_DOCUMENT)
                        .firstName("Integration")
                        .lastName("Receptionist")
                        .phone("600222333")
                        .build();

        return userRepository.saveAndFlush(entity);
    }

    private Department saveDepartment() {
        Department entity =
                Department.builder()
                        .departmentType(
                                TEST_DEPARTMENT_TYPE
                        )
                        .location(
                                "Receptionist integration department"
                        )
                        .phoneExtension("921")
                        .description(
                                "Department for receptionist tests"
                        )
                        .isActive(true)
                        .build();

        return departmentRepository.saveAndFlush(
                entity
        );
    }

    private void removeTestData() {
        userRepository.findByDocumentIdIgnoreCase(
                        TEST_DOCUMENT
                )
                .ifPresent(existingUser -> {
                    receptionistRepository
                            .findByUser_Id(
                                    existingUser.getId()
                            )
                            .ifPresent(
                                    receptionistRepository::delete
                            );

                    receptionistRepository.flush();

                    userRepository.delete(existingUser);
                    userRepository.flush();
                });

        departmentRepository
                .findByDepartmentType(
                        TEST_DEPARTMENT_TYPE
                )
                .ifPresent(existingDepartment -> {
                    departmentRepository.delete(
                            existingDepartment
                    );
                    departmentRepository.flush();
                });
    }

    private String validCreateBody() {
        return """
                {
                  "userId": %d,
                  "departmentId": %d,
                  "deskNumber": "DESK-101",
                  "shiftType": "MORNING"
                }
                """.formatted(
                receptionistUser.getId(),
                department.getId()
        );
    }
}

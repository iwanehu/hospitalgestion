package com.hospital.gestion.api.user.integration;

import com.hospital.gestion.api.common.enums.Role;
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
class UserControllerIntegrationTest {

    private static final String TEST_EMAIL =
            "integration.user@hospital.test";

    private static final String UPDATED_EMAIL =
            "updated.integration@hospital.test";



    private static final String TEST_DOCUMENT =
            "USRTEST001";

    private static final String TEST_PASSWORD =
            "StrongPass123!";

    private static final String JWT_SECRET =
            "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";

    @Container
    static final PostgreSQLContainer POSTGRES =
            new PostgreSQLContainer(
                    "postgres:18.4-alpine"
            )
                    .withDatabaseName(
                            "hospital_user_controller_test"
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
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        removeTestUsers();
    }

    @AfterEach
    void cleanUp() {
        removeTestUsers();
    }

    @Test
    void adminCanCompleteUserLifecycle()
            throws Exception {

        MvcResult createResult =
                mockMvc.perform(
                                post("/api/users")
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
                                jsonPath("$.role")
                                        .value("PATIENT")
                        )
                        .andExpect(
                                jsonPath("$.email")
                                        .value(TEST_EMAIL)
                        )
                        .andExpect(
                                jsonPath("$.isActive")
                                        .value(true)
                        )
                        .andExpect(
                                jsonPath("$.documentId")
                                        .value(TEST_DOCUMENT)
                        )
                        .andExpect(
                                jsonPath("$.firstName")
                                        .value("Integration")
                        )
                        .andExpect(
                                jsonPath("$.lastName")
                                        .value("Patient")
                        )
                        .andExpect(
                                jsonPath("$.phone")
                                        .value("600100200")
                        )
                        .andExpect(
                                jsonPath("$.createdAt")
                                        .exists()
                        )
                        .andReturn();

        Number userId =
                JsonPath.read(
                        createResult.getResponse()
                                .getContentAsString(),
                        "$.id"
                );

        long id = userId.longValue();
        String endpoint = "/api/users/" + id;

        User savedUser =
                userRepository.findById(id)
                        .orElseThrow();

        assertTrue(
                passwordEncoder.matches(
                        TEST_PASSWORD,
                        savedUser.getPassword()
                )
        );

        assertFalse(
                TEST_PASSWORD.equals(
                        savedUser.getPassword()
                )
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
                        jsonPath("$.email")
                                .value(TEST_EMAIL)
                );

        mockMvc.perform(
                        put(endpoint)
                                .with(admin())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "email":
                                            "updated.integration@hospital.test",
                                          "firstName": "Updated",
                                          "lastName": "Integration",
                                          "phone": "600300400"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id").value(id)
                )
                .andExpect(
                        jsonPath("$.email")
                                .value(UPDATED_EMAIL)
                )
                .andExpect(
                        jsonPath("$.firstName")
                                .value("Updated")
                )
                .andExpect(
                        jsonPath("$.lastName")
                                .value("Integration")
                )
                .andExpect(
                        jsonPath("$.phone")
                                .value("600300400")
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

        /*
         * El servicio exige desactivar el usuario
         * antes de poder eliminarlo.
         */
        mockMvc.perform(
                        patch(endpoint + "/deactivate")
                                .with(admin())
                )
                .andExpect(status().isNoContent());

        mockMvc.perform(
                        delete(endpoint)
                                .with(admin())
                )
                .andExpect(status().isNoContent());

        assertFalse(userRepository.existsById(id));

        mockMvc.perform(
                        get(endpoint)
                                .with(admin())
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void nonAdminCannotCreateUser()
            throws Exception {

        mockMvc.perform(
                        post("/api/users")
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
                userRepository.existsByEmailIgnoreCase(
                        TEST_EMAIL
                )
        );
    }

    @Test
    void weakPasswordReturns400()
            throws Exception {

        mockMvc.perform(
                        post("/api/users")
                                .with(admin())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "role": "PATIENT",
                                          "email":
                                            "integration.user@hospital.test",
                                          "password": "weak",
                                          "documentId": "USRTEST001",
                                          "firstName": "Integration",
                                          "lastName": "Patient",
                                          "phone": "600100200"
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
                userRepository.existsByEmailIgnoreCase(
                        TEST_EMAIL
                )
        );
    }

    @Test
    void duplicateEmailReturns409()
            throws Exception {

        saveUser(
                TEST_EMAIL,
                TEST_DOCUMENT,
                true
        );

        mockMvc.perform(
                        post("/api/users")
                                .with(admin())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "role": "PATIENT",
                                          "email":
                                            "INTEGRATION.USER@HOSPITAL.TEST",
                                          "password": "StrongPass123!",
                                          "documentId": "USRTEST002",
                                          "firstName": "Other",
                                          "lastName": "Patient",
                                          "phone": "600500600"
                                        }
                                        """)
                )
                .andExpect(status().isConflict())
                .andExpect(
                        content().contentTypeCompatibleWith(
                                MediaType.APPLICATION_JSON
                        )
                );

        assertTrue(
                userRepository.existsByEmailIgnoreCase(
                        TEST_EMAIL
                )
        );
    }

    @Test
    void paginationFiltersUsers()
            throws Exception {

        saveUser(
                TEST_EMAIL,
                TEST_DOCUMENT,
                true
        );

        mockMvc.perform(
                        get("/api/users/page")
                                .with(admin())
                                .param(
                                        "text",
                                        "Integration"
                                )
                                .param(
                                        "role",
                                        "PATIENT"
                                )
                                .param(
                                        "isActive",
                                        "true"
                                )
                                .param("page", "0")
                                .param("size", "10")
                                .param(
                                        "sort",
                                        "lastName,asc"
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
                        jsonPath("$.content[0].email")
                                .value(TEST_EMAIL)
                )
                .andExpect(
                        jsonPath("$.content[0].role")
                                .value("PATIENT")
                )
                .andExpect(
                        jsonPath("$.content[0].isActive")
                                .value(true)
                )
                .andExpect(
                        jsonPath("$.content[0].documentId")
                                .value(TEST_DOCUMENT)
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

    private User saveUser(
            String email,
            String document,
            boolean active
    ) {
        User entity =
                User.builder()
                        .role(Role.PATIENT)
                        .email(email)
                        .password(
                                passwordEncoder.encode(
                                        TEST_PASSWORD
                                )
                        )
                        .isActive(active)
                        .documentId(document)
                        .firstName("Integration")
                        .lastName("Patient")
                        .phone("600100200")
                        .build();

        return userRepository.saveAndFlush(entity);
    }

    private void removeTestUsers() {
        userRepository.findByDocumentIdIgnoreCase(
                        TEST_DOCUMENT
                )
                .ifPresent(userRepository::delete);

        userRepository.findByDocumentIdIgnoreCase(
                        "USRTEST002"
                )
                .ifPresent(userRepository::delete);

        userRepository.findByEmailIgnoreCase(
                        UPDATED_EMAIL
                )
                .ifPresent(userRepository::delete);

        userRepository.flush();
    }

    private String validCreateBody() {
        return """
                {
                  "role": "PATIENT",
                  "email":
                    "integration.user@hospital.test",
                  "password": "StrongPass123!",
                  "documentId": "USRTEST001",
                  "firstName": "Integration",
                  "lastName": "Patient",
                  "phone": "600100200"
                }
                """;
    }
}

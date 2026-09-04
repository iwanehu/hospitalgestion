package com.hospital.gestion.api.auth.integration;

import com.hospital.gestion.api.auth.service.LoginAttemptService;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class AuthControllerIntegrationTest {

    private static final String EMAIL =
            "integration.doctor@hospital.test";

    private static final String PASSWORD =
            "Integration123!";

    /*
     * Clave Base64 de 32 bytes utilizada solamente durante tests.
     */
    private static final String JWT_SECRET =
            "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";

    @Container
    static final PostgreSQLContainer POSTGRES =
            new PostgreSQLContainer(
                    "postgres:18.4-alpine"
            )
                    .withDatabaseName("hospital_auth_test")
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

    @Autowired
    private LoginAttemptService loginAttemptService;

    @BeforeEach
    void setUp() {
        removeTestUser();
        loginAttemptService.recordSuccessfulLogin(EMAIL);

        createUser(true);
    }

    @AfterEach
    void cleanUp() {
        loginAttemptService.recordSuccessfulLogin(EMAIL);
        removeTestUser();
    }

    @Test
    void loginWithValidCredentialsReturnsJwt()
            throws Exception {

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(validLoginBody())
                )
                .andExpect(status().isOk())
                .andExpect(
                        content().contentTypeCompatibleWith(
                                MediaType.APPLICATION_JSON
                        )
                )
                .andExpect(
                        jsonPath("$.accessToken").isString()
                )
                .andExpect(
                        jsonPath(
                                "$.accessToken",
                                not(blankOrNullString())
                        )
                )
                .andExpect(
                        jsonPath("$.tokenType").value("Bearer")
                )
                .andExpect(
                        jsonPath(
                                "$.expiresIn",
                                greaterThan(0)
                        )
                )
                .andExpect(
                        jsonPath("$.userId").isNumber()
                )
                .andExpect(
                        jsonPath("$.email").value(EMAIL)
                )
                .andExpect(
                        jsonPath("$.role").value("DOCTOR")
                );
    }

    @Test
    void generatedJwtAllowsAccessToProtectedEndpoint()
            throws Exception {

        MvcResult loginResult =
                mockMvc.perform(
                                post("/api/auth/login")
                                        .contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                        .content(validLoginBody())
                        )
                        .andExpect(status().isOk())
                        .andReturn();

        String responseBody =
                loginResult.getResponse()
                        .getContentAsString();

        String accessToken =
                JsonPath.read(
                        responseBody,
                        "$.accessToken"
                );

        assertNotNull(accessToken);

        mockMvc.perform(
                        get("/api/departments")
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer " + accessToken
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        content().contentTypeCompatibleWith(
                                MediaType.APPLICATION_JSON
                        )
                );
    }

    @Test
    void loginWithInvalidPasswordReturns401()
            throws Exception {

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "email":
                                            "integration.doctor@hospital.test",
                                          "password":
                                            "IncorrectPassword123!"
                                        }
                                        """)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(
                        content().contentTypeCompatibleWith(
                                MediaType.APPLICATION_JSON
                        )
                )
                .andExpect(
                        jsonPath("$.status").value(401)
                )
                .andExpect(
                        jsonPath("$.timestamp").exists()
                );
    }

    private void createUser(boolean active) {
        User user = User.builder()
                .role(Role.DOCTOR)
                .email(EMAIL)
                .password(
                        passwordEncoder.encode(PASSWORD)
                )
                .isActive(active)
                .documentId("AUTH-INTEGRATION-001")
                .firstName("Integration")
                .lastName("Doctor")
                .phone("600000001")
                .build();

        userRepository.saveAndFlush(user);
    }

    private void removeTestUser() {
        userRepository.findByEmailIgnoreCase(EMAIL)
                .ifPresent(userRepository::delete);

        userRepository.flush();
    }

    private String validLoginBody() {
        return """
                {
                  "email": "integration.doctor@hospital.test",
                  "password": "Integration123!"
                }
                """;
    }
}

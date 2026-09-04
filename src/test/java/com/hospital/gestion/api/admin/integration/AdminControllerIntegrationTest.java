package com.hospital.gestion.api.admin.integration;


import com.hospital.gestion.api.admin.repository.AdminRepository;
import com.hospital.gestion.api.common.enums.Role;
import com.hospital.gestion.api.security.authorization.HospitalAuthorization;
import com.hospital.gestion.api.user.entity.User;
import com.hospital.gestion.api.user.repository.UserRepository;

import com.jayway.jsonpath.JsonPath;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import static org.springframework.security.test.web.servlet.request
        .SecurityMockMvcRequestPostProcessors.user;

import static org.springframework.test.web.servlet.request
        .MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request
        .MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request
        .MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request
        .MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request
        .MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result
        .MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result
        .MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result
        .MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result
        .MockMvcResultMatchers.status;






@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class AdminControllerIntegrationTest {

    private static final String JWT_SECRET =
            "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";

    @Container
    static final PostgreSQLContainer POSTGRESQL_CONTAINER =
            new PostgreSQLContainer("postgres:18.4-alpine")
                    .withDatabaseName("hospital_admin_controller_test")
                    .withUsername("hospital_test")
                    .withPassword("hospital_test");

    @DynamicPropertySource
    static void configureProperties(
            DynamicPropertyRegistry registry
    ) {
        registry.add(
                "spring.datasource.url",
                POSTGRESQL_CONTAINER::getJdbcUrl
        );

        registry.add(
                "spring.datasource.username",
                POSTGRESQL_CONTAINER::getUsername
        );

        registry.add(
                "spring.datasource.password",
                POSTGRESQL_CONTAINER::getPassword
        );

        registry.add(
                "spring.datasource.driver-class-name",
                POSTGRESQL_CONTAINER::getDriverClassName
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
    private AdminRepository adminRepository;

    @Autowired
    private UserRepository userRepository;

    /*
     * Estos endpoints tienen autorización de negocio adicional:
     *
     * @hospitalAuthorization.isSuperAdmin(authentication)
     *
     * Se simula únicamente esa comprobación. El controlador,
     * servicio, repositorios, validación, transacciones y base
     * PostgreSQL continúan siendo reales.
     */
    @MockitoBean
    private HospitalAuthorization hospitalAuthorization;

    @BeforeEach
    void setUp() {
        adminRepository.deleteAll();
        adminRepository.flush();

        userRepository.deleteAll();
        userRepository.flush();

        when(
                hospitalAuthorization.isSuperAdmin(
                        any(Authentication.class)
                )
        ).thenReturn(true);
    }

    @Test
    void adminLifecycleWorks() throws Exception {
        User targetUser = saveUser(
                Role.ADMIN,
                "target.admin@hospital.test",
                "ADMIN-DOC-001",
                "Laura",
                "Administrador"
        );

        String createRequest = """
                {
                  "userId": %d,
                  "adminLevel": "SYSTEM_ADMIN",
                  "departmentId": null,
                  "permissions": [
                    "VIEW_USERS",
                    "MANAGE_USERS"
                  ]
                }
                """.formatted(targetUser.getId());

        String createResponse = mockMvc.perform(
                        post("/api/admins")
                                .with(superAdmin())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createRequest)
                )
                .andExpect(status().isCreated())
                .andExpect(
                        header().exists("Location")
                )
                .andExpect(
                        jsonPath("$.id").isNumber()
                )
                .andExpect(
                        jsonPath("$.userId")
                                .value(targetUser.getId())
                )
                .andExpect(
                        jsonPath("$.email")
                                .value("target.admin@hospital.test")
                )
                .andExpect(
                        jsonPath("$.adminLevel")
                                .value("SYSTEM_ADMIN")
                )
                .andExpect(
                        jsonPath("$.departmentId")
                                .doesNotExist()
                )
                .andExpect(
                        jsonPath("$.isSuperAdmin")
                                .value(false)
                )
                .andExpect(
                        jsonPath("$.permissions",
                                hasItem("VIEW_USERS"))
                )
                .andReturn()
                .getResponse()
                .getContentAsString();

        Number createdId = JsonPath.read(
                createResponse,
                "$.id"
        );

        Long adminId = createdId.longValue();

        mockMvc.perform(
                        get("/api/admins/{id}", adminId)
                                .with(superAdmin())
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id").value(adminId)
                )
                .andExpect(
                        jsonPath("$.userId")
                                .value(targetUser.getId())
                );

        mockMvc.perform(
                        patch(
                                "/api/admins/{adminId}/permissions"
                                        + "/{permission}/grant",
                                adminId,
                                "VIEW_DASHBOARD"
                        ).with(superAdmin())
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath(
                                "$.permissions",
                                hasItem("VIEW_DASHBOARD")
                        )
                );

        mockMvc.perform(
                        get(
                                "/api/admins/{adminId}/permissions"
                                        + "/{permission}/exists",
                                adminId,
                                "VIEW_DASHBOARD"
                        ).with(superAdmin())
                )
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        mockMvc.perform(
                        patch(
                                "/api/admins/{adminId}/permissions"
                                        + "/{permission}/revoke",
                                adminId,
                                "VIEW_DASHBOARD"
                        ).with(superAdmin())
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        get(
                                "/api/admins/{adminId}/permissions"
                                        + "/{permission}/exists",
                                adminId,
                                "VIEW_DASHBOARD"
                        ).with(superAdmin())
                )
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        String updateRequest = """
                {
                  "adminLevel": "SUPER_ADMIN",
                  "departmentId": null,
                  "permissions": [
                    "VIEW_DASHBOARD",
                    "VIEW_USERS",
                    "MANAGE_USERS",
                    "MANAGE_ROLES"
                  ]
                }
                """;

        mockMvc.perform(
                        put("/api/admins/{id}", adminId)
                                .with(superAdmin())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateRequest)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id").value(adminId)
                )
                .andExpect(
                        jsonPath("$.adminLevel")
                                .value("SUPER_ADMIN")
                )
                .andExpect(
                        jsonPath("$.isSuperAdmin")
                                .value(true)
                )
                .andExpect(
                        jsonPath(
                                "$.permissions",
                                hasItem("MANAGE_ROLES")
                        )
                );

        mockMvc.perform(
                        delete("/api/admins/{id}", adminId)
                                .with(superAdmin())
                )
                .andExpect(status().isNoContent());

        mockMvc.perform(
                        get("/api/admins/{id}", adminId)
                                .with(superAdmin())
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void duplicateAdminProfileReturns409() throws Exception {
        User targetUser = saveUser(
                Role.ADMIN,
                "duplicate.admin@hospital.test",
                "ADMIN-DOC-002",
                "Carlos",
                "Duplicado"
        );

        String request = """
                {
                  "userId": %d,
                  "adminLevel": "SYSTEM_ADMIN",
                  "departmentId": null,
                  "permissions": [
                    "VIEW_USERS"
                  ]
                }
                """.formatted(targetUser.getId());

        mockMvc.perform(
                        post("/api/admins")
                                .with(superAdmin())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isCreated());

        mockMvc.perform(
                        post("/api/admins")
                                .with(superAdmin())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isConflict())
                .andExpect(
                        jsonPath("$.status").value(409)
                );
    }

    @Test
    void invalidCreateRequestReturns400() throws Exception {
        String invalidRequest = """
                {
                  "userId": null,
                  "adminLevel": null,
                  "departmentId": -1,
                  "permissions": []
                }
                """;

        mockMvc.perform(
                        post("/api/admins")
                                .with(superAdmin())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(invalidRequest)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        content().contentTypeCompatibleWith(
                                MediaType.APPLICATION_JSON
                        )
                );
    }

    @Test
    void filteredPageReturnsCreatedAdmin() throws Exception {
        User targetUser = saveUser(
                Role.ADMIN,
                "page.admin@hospital.test",
                "ADMIN-DOC-003",
                "Elena",
                "Buscada"
        );

        String request = """
                {
                  "userId": %d,
                  "adminLevel": "SYSTEM_ADMIN",
                  "departmentId": null,
                  "permissions": [
                    "VIEW_DASHBOARD",
                    "VIEW_STATISTICS"
                  ]
                }
                """.formatted(targetUser.getId());

        mockMvc.perform(
                        post("/api/admins")
                                .with(superAdmin())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isCreated());

        mockMvc.perform(
                        get("/api/admins/page")
                                .with(superAdmin())
                                .param("text", "Buscada")
                                .param(
                                        "adminLevel",
                                        "SYSTEM_ADMIN"
                                )
                                .param(
                                        "permission",
                                        "VIEW_STATISTICS"
                                )
                                .param("isActive", "true")
                                .param(
                                        "isSuperAdmin",
                                        "false"
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
                        jsonPath("$.content.length()")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.content[0].userId")
                                .value(targetUser.getId())
                )
                .andExpect(
                        jsonPath("$.content[0].adminLevel")
                                .value("SYSTEM_ADMIN")
                )
                .andExpect(
                        jsonPath(
                                "$.content[0].permissions",
                                hasItem("VIEW_STATISTICS")
                        )
                );
    }

    @Test
    void nonAdminCannotAccessAdmins() throws Exception {
        mockMvc.perform(
                        get("/api/admins")
                                .with(patient())
                )
                .andExpect(status().isForbidden())
                .andExpect(
                        jsonPath("$.status").value(403)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value("Access denied")
                );
    }

    private User saveUser(
            Role role,
            String email,
            String documentId,
            String firstName,
            String lastName
    ) {
        User user = User.builder()
                .role(role)
                .email(email)
                .password("Password123!")
                .isActive(true)
                .documentId(documentId)
                .firstName(firstName)
                .lastName(lastName)
                .phone("600123456")
                .build();

        return userRepository.saveAndFlush(user);
    }

    private RequestPostProcessor superAdmin() {
        return user("super.admin@hospital.test")
                .roles("ADMIN");
    }

    private RequestPostProcessor patient() {
        return user("patient@hospital.test")
                .roles("PATIENT");
    }
}

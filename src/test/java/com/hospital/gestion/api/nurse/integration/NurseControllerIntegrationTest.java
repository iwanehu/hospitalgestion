package com.hospital.gestion.api.nurse.integration;

import com.hospital.gestion.api.common.enums.DepartmentType;
import com.hospital.gestion.api.common.enums.Role;
import com.hospital.gestion.api.department.entity.Department;
import com.hospital.gestion.api.department.repository.DepartmentRepository;
import com.hospital.gestion.api.nurse.repository.NurseRepository;
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
class NurseControllerIntegrationTest {

    private static final String TEST_EMAIL =
            "nurse.integration@hospital.test";

    private static final String TEST_DOCUMENT =
            "NURTEST001";

    private static final String TEST_LICENSE =
            "NUR-12345";

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
                            "hospital_nurse_controller_test"
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
    private NurseRepository nurseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User nurseUser;
    private Department department;

    @BeforeEach
    void setUp() {
        removeTestData();

        nurseUser = saveNurseUser();
        department = saveDepartment();
    }

    @AfterEach
    void cleanUp() {
        removeTestData();
    }

    @Test
    void adminCanCompleteNurseLifecycle()
            throws Exception {

        MvcResult createResult =
                mockMvc.perform(
                                post("/api/nurses")
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
                                        .value(nurseUser.getId())
                        )
                        .andExpect(
                                jsonPath("$.fullName").value(
                                        "Integration Nurse"
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
                                jsonPath("$.licenseNumber")
                                        .value(TEST_LICENSE)
                        )
                        .andExpect(
                                jsonPath("$.specialty")
                                        .value("LABORATORY")
                        )
                        .andExpect(
                                jsonPath("$.shiftType")
                                        .value("MORNING")
                        )
                        .andExpect(
                                jsonPath("$.yearsOfExperience")
                                        .value(6)
                        )
                        .andExpect(
                                jsonPath("$.hireDate")
                                        .value("2020-01-01")
                        )
                        .andExpect(
                                jsonPath("$.maxPatientsPerShift")
                                        .value(8)
                        )
                        .andExpect(
                                jsonPath("$.isChargeNurse")
                                        .value(false)
                        )
                        .andExpect(
                                jsonPath("$.vacationDaysAvailable")
                                        .value(22)
                        )
                        .andExpect(
                                jsonPath("$.createdAt")
                                        .exists()
                        )
                        .andReturn();

        Number nurseId =
                JsonPath.read(
                        createResult.getResponse()
                                .getContentAsString(),
                        "$.id"
                );

        long id = nurseId.longValue();
        String endpoint = "/api/nurses/" + id;

        assertTrue(nurseRepository.existsById(id));

        mockMvc.perform(
                        get(endpoint)
                                .with(doctor())
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id").value(id)
                )
                .andExpect(
                        jsonPath("$.licenseNumber")
                                .value(TEST_LICENSE)
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
                                          "specialty": "GENERAL",
                                          "shiftType": "AFTERNOON",
                                          "yearsOfExperience": 9,
                                          "hireDate": "2018-06-01",
                                          "biography":
                                            "Updated nurse biography",
                                          "emergencyContactName":
                                            "Updated Contact",
                                          "emergencyContactPhone":
                                            "600333444",
                                          "emergencyContactRelationship":
                                            "Parent",
                                          "maxPatientsPerShift": 10,
                                          "isChargeNurse": true,
                                          "vacationDaysAvailable": 25
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
                        jsonPath("$.specialty")
                                .value("GENERAL")
                )
                .andExpect(
                        jsonPath("$.shiftType")
                                .value("AFTERNOON")
                )
                .andExpect(
                        jsonPath("$.yearsOfExperience")
                                .value(9)
                )
                .andExpect(
                        jsonPath("$.hireDate")
                                .value("2018-06-01")
                )
                .andExpect(
                        jsonPath("$.biography").value(
                                "Updated nurse biography"
                        )
                )
                .andExpect(
                        jsonPath("$.emergencyContactName")
                                .value("Updated Contact")
                )
                .andExpect(
                        jsonPath("$.maxPatientsPerShift")
                                .value(10)
                )
                .andExpect(
                        jsonPath("$.isChargeNurse")
                                .value(true)
                )
                .andExpect(
                        jsonPath("$.vacationDaysAvailable")
                                .value(25)
                )
                .andExpect(
                        jsonPath("$.licenseNumber")
                                .value(TEST_LICENSE)
                );

        mockMvc.perform(
                        delete(endpoint)
                                .with(admin())
                )
                .andExpect(status().isNoContent());

        assertFalse(nurseRepository.existsById(id));

        assertTrue(
                userRepository.existsById(
                        nurseUser.getId()
                )
        );

        mockMvc.perform(
                        get(endpoint)
                                .with(doctor())
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void doctorCannotCreateNurse()
            throws Exception {

        mockMvc.perform(
                        post("/api/nurses")
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
                nurseRepository.existsByUser_Id(
                        nurseUser.getId()
                )
        );
    }

    @Test
    void invalidNurseRequestReturns400()
            throws Exception {

        mockMvc.perform(
                        post("/api/nurses")
                                .with(admin())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "userId": %d,
                                          "departmentId": %d,
                                          "licenseNumber": "INVALID",
                                          "specialty": "LABORATORY",
                                          "shiftType": "MORNING",
                                          "yearsOfExperience": 61,
                                          "hireDate": "2020-01-01",
                                          "biography": "Invalid",
                                          "emergencyContactName":
                                            "Contact",
                                          "emergencyContactPhone":
                                            "600111222",
                                          "emergencyContactRelationship":
                                            "Sibling",
                                          "maxPatientsPerShift": 21,
                                          "isChargeNurse": false,
                                          "vacationDaysAvailable": 61
                                        }
                                        """.formatted(
                                        nurseUser.getId(),
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
                nurseRepository.existsByUser_Id(
                        nurseUser.getId()
                )
        );
    }

    @Test
    void duplicateNurseForSameUserReturns409()
            throws Exception {

        createNurseProfile();

        mockMvc.perform(
                        post("/api/nurses")
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
                nurseRepository.existsByUser_Id(
                        nurseUser.getId()
                )
        );
    }

    @Test
    void paginationFiltersNurses()
            throws Exception {

        createNurseProfile();

        mockMvc.perform(
                        get("/api/nurses/page")
                                .with(doctor())
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
                                        "specialty",
                                        "LABORATORY"
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
                                        "isChargeNurse",
                                        "false"
                                )
                                .param(
                                        "minimumExperience",
                                        "5"
                                )
                                .param(
                                        "maximumExperience",
                                        "10"
                                )
                                .param(
                                        "hiredFrom",
                                        "2019-01-01"
                                )
                                .param(
                                        "hiredTo",
                                        "2021-12-31"
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
                                .value(nurseUser.getId())
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
                        jsonPath("$.content[0].specialty")
                                .value("LABORATORY")
                )
                .andExpect(
                        jsonPath("$.content[0].shiftType")
                                .value("MORNING")
                )
                .andExpect(
                        jsonPath(
                                "$.content[0].yearsOfExperience"
                        ).value(6)
                )
                .andExpect(
                        jsonPath("$.content[0].isChargeNurse")
                                .value(false)
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

    private void createNurseProfile()
            throws Exception {

        mockMvc.perform(
                        post("/api/nurses")
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

    private RequestPostProcessor doctor() {
        return user("doctor@hospital.test")
                .roles("DOCTOR");
    }

    private User saveNurseUser() {
        User entity =
                User.builder()
                        .role(Role.NURSE)
                        .email(TEST_EMAIL)
                        .password(
                                passwordEncoder.encode(
                                        "StrongPass123!"
                                )
                        )
                        .isActive(true)
                        .documentId(TEST_DOCUMENT)
                        .firstName("Integration")
                        .lastName("Nurse")
                        .phone("600111222")
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
                                "Nurse integration department"
                        )
                        .phoneExtension("911")
                        .description(
                                "Department for nurse tests"
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
                    nurseRepository
                            .findByUser_Id(
                                    existingUser.getId()
                            )
                            .ifPresent(
                                    nurseRepository::delete
                            );

                    nurseRepository.flush();

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
                  "licenseNumber": "NUR-12345",
                  "specialty": "LABORATORY",
                  "shiftType": "MORNING",
                  "yearsOfExperience": 6,
                  "hireDate": "2020-01-01",
                  "biography":
                    "Integration nurse biography",
                  "emergencyContactName":
                    "Emergency Contact",
                  "emergencyContactPhone":
                    "600999888",
                  "emergencyContactRelationship":
                    "Sibling",
                  "maxPatientsPerShift": 8,
                  "isChargeNurse": false,
                  "vacationDaysAvailable": 22
                }
                """.formatted(
                nurseUser.getId(),
                department.getId()
        );
    }
}

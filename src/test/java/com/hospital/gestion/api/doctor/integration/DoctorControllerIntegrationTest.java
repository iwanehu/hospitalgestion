package com.hospital.gestion.api.doctor.integration;

import com.hospital.gestion.api.common.enums.DepartmentType;
import com.hospital.gestion.api.common.enums.Role;
import com.hospital.gestion.api.common.enums.Specialty;
import com.hospital.gestion.api.department.entity.Department;
import com.hospital.gestion.api.department.repository.DepartmentRepository;
import com.hospital.gestion.api.doctor.entity.Doctor;
import com.hospital.gestion.api.doctor.repository.DoctorRepository;
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
class DoctorControllerIntegrationTest {

    private static final String TEST_EMAIL =
            "doctor.integration@hospital.test";

    private static final String TEST_DOCUMENT =
            "DOCTEST001";

    private static final String TEST_LICENSE =
            "MED-12345";

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
                            "hospital_doctor_controller_test"
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
    private DoctorRepository doctorRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User doctorUser;
    private Department department;

    @BeforeEach
    void setUp() {
        removeTestData();

        doctorUser = saveDoctorUser();
        department = saveDepartment();
    }

    @AfterEach
    void cleanUp() {
        removeTestData();
    }

    @Test
    void adminCanCompleteDoctorLifecycle()
            throws Exception {

        MvcResult createResult =
                mockMvc.perform(
                                post("/api/doctors")
                                        .with(admin())
                                        .contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                        .content(
                                                validCreateBody(
                                                        doctorUser.getId(),
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
                                jsonPath("$.userId")
                                        .value(doctorUser.getId())
                        )
                        .andExpect(
                                jsonPath("$.fullName").value(
                                        "Integration Doctor"
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
                                jsonPath("$.specialty").value(
                                        "CLINICAL_PATHOLOGY"
                                )
                        )
                        .andExpect(
                                jsonPath(
                                        "$.medicalLicenseNumber"
                                ).value(TEST_LICENSE)
                        )
                        .andExpect(
                                jsonPath(
                                        "$.yearsOfExperience"
                                ).value(8)
                        )
                        .andExpect(
                                jsonPath("$.biography").value(
                                        "Integration doctor biography"
                                )
                        )
                        .andExpect(
                                jsonPath("$.createdAt").exists()
                        )
                        .andReturn();

        Number doctorId =
                JsonPath.read(
                        createResult.getResponse()
                                .getContentAsString(),
                        "$.id"
                );

        long id = doctorId.longValue();
        String endpoint = "/api/doctors/" + id;

        assertTrue(doctorRepository.existsById(id));

        mockMvc.perform(
                        get(endpoint)
                                .with(nurse())
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id").value(id)
                )
                .andExpect(
                        jsonPath("$.medicalLicenseNumber")
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
                                          "specialty": "MICROBIOLOGY",
                                          "yearsOfExperience": 12,
                                          "biography":
                                            "Updated doctor biography"
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
                        jsonPath("$.specialty")
                                .value("MICROBIOLOGY")
                )
                .andExpect(
                        jsonPath("$.yearsOfExperience")
                                .value(12)
                )
                .andExpect(
                        jsonPath("$.biography").value(
                                "Updated doctor biography"
                        )
                )
                .andExpect(
                        jsonPath("$.medicalLicenseNumber")
                                .value(TEST_LICENSE)
                );

        mockMvc.perform(
                        delete(endpoint)
                                .with(admin())
                )
                .andExpect(status().isNoContent());

        assertFalse(doctorRepository.existsById(id));

        /*
         * El perfil médico se elimina, pero el usuario
         * asociado permanece.
         */
        assertTrue(
                userRepository.existsById(
                        doctorUser.getId()
                )
        );

        mockMvc.perform(
                        get(endpoint)
                                .with(nurse())
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void nurseCannotCreateDoctor()
            throws Exception {

        mockMvc.perform(
                        post("/api/doctors")
                                .with(nurse())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        validCreateBody(
                                                doctorUser.getId(),
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
                doctorRepository.existsByUser_Id(
                        doctorUser.getId()
                )
        );
    }

    @Test
    void invalidMedicalLicenseReturns400()
            throws Exception {

        mockMvc.perform(
                        post("/api/doctors")
                                .with(admin())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "userId": %d,
                                          "departmentId": %d,
                                          "specialty":
                                            "CLINICAL_PATHOLOGY",
                                          "medicalLicenseNumber":
                                            "INVALID",
                                          "yearsOfExperience": 8,
                                          "biography":
                                            "Invalid doctor"
                                        }
                                        """.formatted(
                                        doctorUser.getId(),
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
                doctorRepository.existsByUser_Id(
                        doctorUser.getId()
                )
        );
    }

    @Test
    void duplicateDoctorForSameUserReturns409()
            throws Exception {

        saveDoctor();

        mockMvc.perform(
                        post("/api/doctors")
                                .with(admin())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        validCreateBody(
                                                doctorUser.getId(),
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
                doctorRepository.existsByUser_Id(
                        doctorUser.getId()
                )
        );
    }

    @Test
    void paginationFiltersDoctors()
            throws Exception {

        saveDoctor();

        mockMvc.perform(
                        get("/api/doctors/page")
                                .with(nurse())
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
                                        "CLINICAL_PATHOLOGY"
                                )
                                .param(
                                        "isActive",
                                        "true"
                                )
                                .param(
                                        "minimumExperience",
                                        "5"
                                )
                                .param(
                                        "maximumExperience",
                                        "10"
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
                                .value(doctorUser.getId())
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
                                .value("CLINICAL_PATHOLOGY")
                )
                .andExpect(
                        jsonPath(
                                "$.content[0].yearsOfExperience"
                        ).value(8)
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

    private RequestPostProcessor nurse() {
        return user("nurse@hospital.test")
                .roles("NURSE");
    }

    private User saveDoctorUser() {
        User entity =
                User.builder()
                        .role(Role.DOCTOR)
                        .email(TEST_EMAIL)
                        .password(
                                passwordEncoder.encode(
                                        "StrongPass123!"
                                )
                        )
                        .isActive(true)
                        .documentId(TEST_DOCUMENT)
                        .firstName("Integration")
                        .lastName("Doctor")
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
                                "Doctor integration department"
                        )
                        .phoneExtension("901")
                        .description(
                                "Department for doctor tests"
                        )
                        .isActive(true)
                        .build();

        return departmentRepository.saveAndFlush(
                entity
        );
    }

    private Doctor saveDoctor() {
        Doctor entity =
                Doctor.builder()
                        .user(doctorUser)
                        .department(department)
                        .specialty(
                                Specialty.CLINICAL_PATHOLOGY
                        )
                        .medicalLicenseNumber(
                                TEST_LICENSE
                        )
                        .yearsOfExperience(8)
                        .biography(
                                "Integration doctor biography"
                        )
                        .build();

        return doctorRepository.saveAndFlush(entity);
    }

    private void removeTestData() {
        userRepository.findByDocumentIdIgnoreCase(
                        TEST_DOCUMENT
                )
                .ifPresent(existingUser -> {
                    doctorRepository
                            .findByUser_Id(
                                    existingUser.getId()
                            )
                            .ifPresent(
                                    doctorRepository::delete
                            );

                    doctorRepository.flush();

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

    private String validCreateBody(
            Long userId,
            Long departmentId
    ) {
        return """
                {
                  "userId": %d,
                  "departmentId": %d,
                  "specialty": "CLINICAL_PATHOLOGY",
                  "medicalLicenseNumber": "MED-12345",
                  "yearsOfExperience": 8,
                  "biography":
                    "Integration doctor biography"
                }
                """.formatted(
                userId,
                departmentId
        );
    }
}

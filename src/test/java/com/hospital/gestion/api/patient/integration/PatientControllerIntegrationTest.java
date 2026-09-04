package com.hospital.gestion.api.patient.integration;

import com.hospital.gestion.api.common.enums.BloodType;
import com.hospital.gestion.api.common.enums.Role;
import com.hospital.gestion.api.patient.entity.EmergencyContact;
import com.hospital.gestion.api.patient.entity.Patient;
import com.hospital.gestion.api.patient.repository.PatientRepository;
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

import java.time.LocalDate;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class PatientControllerIntegrationTest {

    private static final String TEST_EMAIL =
            "patient.integration@hospital.test";

    private static final String TEST_DOCUMENT =
            "PATTEST001";

    private static final String JWT_SECRET =
            "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";

    @Container
    static final PostgreSQLContainer POSTGRES =
            new PostgreSQLContainer(
                    "postgres:18.4-alpine"
            )
                    .withDatabaseName(
                            "hospital_patient_controller_test"
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
    private PatientRepository patientRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User patientUser;

    @BeforeEach
    void setUp() {
        removeTestData();
        patientUser = savePatientUser();
    }

    @AfterEach
    void cleanUp() {
        removeTestData();
    }

    @Test
    void adminCanCompletePatientLifecycle()
            throws Exception {

        MvcResult createResult =
                mockMvc.perform(
                                post("/api/patients")
                                        .with(admin())
                                        .contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                        .content(
                                                validCreateBody(
                                                        patientUser.getId()
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
                                        .value(patientUser.getId())
                        )
                        .andExpect(
                                jsonPath("$.fullName").value(
                                        "Integration Patient"
                                )
                        )
                        .andExpect(
                                jsonPath("$.email")
                                        .value(TEST_EMAIL)
                        )
                        .andExpect(
                                jsonPath("$.documentId")
                                        .value(TEST_DOCUMENT)
                        )
                        .andExpect(
                                jsonPath("$.bloodType")
                                        .value("O_POSITIVE")
                        )
                        .andExpect(
                                jsonPath("$.birthDate")
                                        .value("1990-05-10")
                        )
                        .andExpect(
                                jsonPath(
                                        "$.emergencyContactName"
                                ).value("Emergency Contact")
                        )
                        .andExpect(
                                jsonPath(
                                        "$.emergencyContactPhone"
                                ).value("600900800")
                        )
                        .andExpect(
                                jsonPath(
                                        "$.emergencyContactRelationship"
                                ).value("Sibling")
                        )
                        .andExpect(
                                jsonPath("$.hasHealthInsurance")
                                        .value(true)
                        )
                        .andExpect(
                                jsonPath(
                                        "$.healthInsuranceProvider"
                                ).value("Integration Insurance")
                        )
                        .andExpect(
                                jsonPath(
                                        "$.healthInsuranceNumber"
                                ).value("INS001")
                        )
                        .andExpect(
                                jsonPath("$.createdAt")
                                        .exists()
                        )
                        .andReturn();

        Number patientId =
                JsonPath.read(
                        createResult.getResponse()
                                .getContentAsString(),
                        "$.id"
                );

        long id = patientId.longValue();
        String endpoint = "/api/patients/" + id;

        assertTrue(patientRepository.existsById(id));

        mockMvc.perform(
                        get(endpoint)
                                .with(doctor())
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id").value(id)
                )
                .andExpect(
                        jsonPath("$.userId")
                                .value(patientUser.getId())
                );

        mockMvc.perform(
                        patch(endpoint)
                                .with(admin())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "bloodType": "A_POSITIVE",
                                          "emergencyContactName":
                                            "Updated Contact",
                                          "emergencyContactPhone":
                                            "600700600",
                                          "emergencyContactRelationship":
                                            "Parent",
                                          "allergies": "Penicillin",
                                          "hasHealthInsurance": true,
                                          "healthInsuranceProvider":
                                            "Updated Insurance",
                                          "healthInsuranceNumber":
                                            "INS002",
                                          "medicalHistory":
                                            "Updated medical history"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id").value(id)
                )
                .andExpect(
                        jsonPath("$.bloodType")
                                .value("A_POSITIVE")
                )
                .andExpect(
                        jsonPath(
                                "$.emergencyContactName"
                        ).value("Updated Contact")
                )
                .andExpect(
                        jsonPath("$.allergies")
                                .value("Penicillin")
                )
                .andExpect(
                        jsonPath(
                                "$.healthInsuranceProvider"
                        ).value("Updated Insurance")
                )
                .andExpect(
                        jsonPath(
                                "$.healthInsuranceNumber"
                        ).value("INS002")
                )
                .andExpect(
                        jsonPath("$.medicalHistory").value(
                                "Updated medical history"
                        )
                );

        mockMvc.perform(
                        delete(endpoint)
                                .with(admin())
                )
                .andExpect(status().isNoContent());

        assertFalse(patientRepository.existsById(id));

        /*
         * El perfil se elimina, pero el usuario relacionado
         * debe seguir existiendo.
         */
        assertTrue(
                userRepository.existsById(
                        patientUser.getId()
                )
        );

        mockMvc.perform(
                        get(endpoint)
                                .with(doctor())
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void doctorCannotCreatePatient()
            throws Exception {

        mockMvc.perform(
                        post("/api/patients")
                                .with(doctor())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        validCreateBody(
                                                patientUser.getId()
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
                patientRepository.existsByUser_Id(
                        patientUser.getId()
                )
        );
    }

    @Test
    void futureBirthDateReturns400()
            throws Exception {

        mockMvc.perform(
                        post("/api/patients")
                                .with(admin())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "userId": %d,
                                          "bloodType": "O_POSITIVE",
                                          "birthDate": "2099-01-01",
                                          "emergencyContactName":
                                            "Emergency Contact",
                                          "emergencyContactPhone":
                                            "600900800",
                                          "emergencyContactRelationship":
                                            "Sibling",
                                          "allergies": null,
                                          "hasHealthInsurance": false,
                                          "healthInsuranceProvider": null,
                                          "healthInsuranceNumber": null,
                                          "medicalHistory": null
                                        }
                                        """.formatted(
                                        patientUser.getId()
                                ))
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        content().contentTypeCompatibleWith(
                                MediaType.APPLICATION_JSON
                        )
                );

        assertFalse(
                patientRepository.existsByUser_Id(
                        patientUser.getId()
                )
        );
    }

    @Test
    void duplicatePatientForSameUserReturns409()
            throws Exception {

        savePatient();

        mockMvc.perform(
                        post("/api/patients")
                                .with(admin())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        validCreateBody(
                                                patientUser.getId()
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
                patientRepository.existsByUser_Id(
                        patientUser.getId()
                )
        );
    }

    @Test
    void paginationFiltersPatients()
            throws Exception {

        savePatient();

        mockMvc.perform(
                        get("/api/patients/page")
                                .with(doctor())
                                .param(
                                        "text",
                                        "Integration"
                                )
                                .param(
                                        "bloodType",
                                        "O_POSITIVE"
                                )
                                .param(
                                        "hasHealthInsurance",
                                        "true"
                                )
                                .param(
                                        "insuranceProvider",
                                        "Integration"
                                )
                                .param(
                                        "isActive",
                                        "true"
                                )
                                .param(
                                        "birthDateFrom",
                                        "1980-01-01"
                                )
                                .param(
                                        "birthDateTo",
                                        "2000-12-31"
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
                                .value(patientUser.getId())
                )
                .andExpect(
                        jsonPath("$.content[0].email")
                                .value(TEST_EMAIL)
                )
                .andExpect(
                        jsonPath("$.content[0].bloodType")
                                .value("O_POSITIVE")
                )
                .andExpect(
                        jsonPath(
                                "$.content[0].hasHealthInsurance"
                        ).value(true)
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

    private User savePatientUser() {
        User entity =
                User.builder()
                        .role(Role.PATIENT)
                        .email(TEST_EMAIL)
                        .password(
                                passwordEncoder.encode(
                                        "StrongPass123!"
                                )
                        )
                        .isActive(true)
                        .documentId(TEST_DOCUMENT)
                        .firstName("Integration")
                        .lastName("Patient")
                        .phone("600100200")
                        .build();

        return userRepository.saveAndFlush(entity);
    }

    private Patient savePatient() {
        Patient entity =
                Patient.builder()
                        .user(patientUser)
                        .bloodType(BloodType.O_POSITIVE)
                        .birthDate(
                                LocalDate.of(
                                        1990,
                                        5,
                                        10
                                )
                        )
                        .emergencyContact(
                                EmergencyContact.builder()
                                        .name(
                                                "Emergency Contact"
                                        )
                                        .phone("600900800")
                                        .relationship("Sibling")
                                        .build()
                        )
                        .allergies("None")
                        .hasHealthInsurance(true)
                        .healthInsuranceProvider(
                                "Integration Insurance"
                        )
                        .healthInsuranceNumber("INS001")
                        .medicalHistory(
                                "Integration medical history"
                        )
                        .build();

        return patientRepository.saveAndFlush(entity);
    }

    private void removeTestData() {
        userRepository.findByDocumentIdIgnoreCase(
                        TEST_DOCUMENT
                )
                .ifPresent(existingUser -> {
                    patientRepository
                            .findByUser_Id(
                                    existingUser.getId()
                            )
                            .ifPresent(
                                    patientRepository::delete
                            );

                    patientRepository.flush();

                    userRepository.delete(existingUser);
                    userRepository.flush();
                });
    }

    private String validCreateBody(Long userId) {
        return """
                {
                  "userId": %d,
                  "bloodType": "O_POSITIVE",
                  "birthDate": "1990-05-10",
                  "emergencyContactName":
                    "Emergency Contact",
                  "emergencyContactPhone":
                    "600900800",
                  "emergencyContactRelationship":
                    "Sibling",
                  "allergies": "None",
                  "hasHealthInsurance": true,
                  "healthInsuranceProvider":
                    "Integration Insurance",
                  "healthInsuranceNumber": "INS001",
                  "medicalHistory":
                    "Integration medical history"
                }
                """.formatted(userId);
    }
}

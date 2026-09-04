package com.hospital.gestion.api.security.integration;

import com.hospital.gestion.api.auth.service.LoginAttemptService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;



import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {



    private static final String BLOCKED_EMAIL =
            "blocked.test@hospital.com";

    @Autowired
    private LoginAttemptService loginAttemptService;

    @AfterEach
    void clearLoginAttempts() {
        loginAttemptService.recordSuccessfulLogin(
                BLOCKED_EMAIL
        );
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    void protectedEndpointWithoutAuthenticationReturns401()
            throws Exception {

        mockMvc.perform(
                        get("/api/departments")
                )
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value(
                        "Authentication is required"
                ));
    }

    @Test
    void protectedEndpointWithInvalidJwtReturns401()
            throws Exception {

        mockMvc.perform(
                        get("/api/departments")
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer invalid.jwt.token"
                                )
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value(
                        "Authentication is required"
                ));
    }

    @Test
    void authenticatedUserWithoutRequiredRoleReturns403()
            throws Exception {

        mockMvc.perform(
                        get("/api/admins")
                                .with(
                                        user("doctor@hospital.com")
                                                .roles("DOCTOR")
                                )
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value(
                        "Access denied"
                ));
    }

    @Test
    void authenticatedUserCanAccessAuthenticatedEndpoint()
            throws Exception {

        mockMvc.perform(
                        get("/api/departments")
                                .with(
                                        user("doctor@hospital.com")
                                                .roles("DOCTOR")
                                )
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ));
    }

    @Test
    void loginEndpointIsPublic()
            throws Exception {

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "email": "",
                                          "password": ""
                                        }
                                        """)
                )
                /*
                 * Debe responder 400 por validación del DTO,
                 * no 401 por ausencia de autenticación.
                 */
                .andExpect(status().isBadRequest());
    }


    @Test
    void viteOriginPreflightRequestIsAllowed()
            throws Exception {

        mockMvc.perform(
                        options("/api/departments")
                                .header(
                                        HttpHeaders.ORIGIN,
                                        "http://localhost:5173"
                                )
                                .header(
                                        HttpHeaders
                                                .ACCESS_CONTROL_REQUEST_METHOD,
                                        "GET"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        header().string(
                                HttpHeaders
                                        .ACCESS_CONTROL_ALLOW_ORIGIN,
                                "http://localhost:5173"
                        )
                )
                .andExpect(
                        header().string(
                                HttpHeaders
                                        .ACCESS_CONTROL_ALLOW_METHODS,
                                containsString("GET")
                        )
                );
    }



    @Test
    void unauthorizedOriginPreflightRequestIsRejected()
            throws Exception {

        mockMvc.perform(
                        options("/api/departments")
                                .header(
                                        HttpHeaders.ORIGIN,
                                        "https://malicious.example"
                                )
                                .header(
                                        HttpHeaders
                                                .ACCESS_CONTROL_REQUEST_METHOD,
                                        "GET"
                                )
                )
                .andExpect(status().isForbidden())
                .andExpect(
                        header().doesNotExist(
                                HttpHeaders
                                        .ACCESS_CONTROL_ALLOW_ORIGIN
                        )
                );
    }



    @Test
    void blockedLoginReturns429()
            throws Exception {

        for (int attempt = 0; attempt < 5; attempt++) {
            loginAttemptService.recordFailedAttempt(
                    BLOCKED_EMAIL
            );
        }

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                    {
                                      "email": "blocked.test@hospital.com",
                                      "password": "ValidPassword123!"
                                    }
                                    """)
                )
                .andExpect(
                        status().isTooManyRequests()
                )
                .andExpect(
                        content().contentTypeCompatibleWith(
                                MediaType.APPLICATION_JSON
                        )
                )
                .andExpect(
                        jsonPath("$.status").value(429)
                )
                .andExpect(
                        jsonPath("$.message").value(
                                "Too many failed login attempts. Try again later"
                        )
                )
                .andExpect(
                        jsonPath("$.timestamp").exists()
                );
    }
}

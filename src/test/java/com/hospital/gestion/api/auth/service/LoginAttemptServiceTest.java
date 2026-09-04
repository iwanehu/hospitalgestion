package com.hospital.gestion.api.auth.service;

import com.hospital.gestion.api.common.exception.LoginRateLimitException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LoginAttemptServiceTest {

    private static final String EMAIL =
            "admin@hospital.com";

    private LoginAttemptService loginAttemptService;

    @BeforeEach
    void setUp() {
        loginAttemptService =
                new LoginAttemptService(
                        5,
                        900_000,
                        900_000
                );
    }

    @Test
    void accountIsInitiallyAllowed() {
        assertDoesNotThrow(() ->
                loginAttemptService.checkAllowed(EMAIL)
        );
    }

    @Test
    void fourFailedAttemptsDoNotBlockLogin() {
        for (int attempt = 0; attempt < 4; attempt++) {
            loginAttemptService.recordFailedAttempt(
                    EMAIL
            );
        }

        assertDoesNotThrow(() ->
                loginAttemptService.checkAllowed(EMAIL)
        );
    }

    @Test
    void fifthFailedAttemptBlocksSubsequentLogin() {
        for (int attempt = 0; attempt < 5; attempt++) {
            loginAttemptService.recordFailedAttempt(
                    EMAIL
            );
        }

        assertThrows(
                LoginRateLimitException.class,
                () -> loginAttemptService
                        .checkAllowed(EMAIL)
        );
    }

    @Test
    void successfulLoginClearsFailedAttempts() {
        for (int attempt = 0; attempt < 5; attempt++) {
            loginAttemptService.recordFailedAttempt(
                    EMAIL
            );
        }

        loginAttemptService.recordSuccessfulLogin(
                EMAIL
        );

        assertDoesNotThrow(() ->
                loginAttemptService.checkAllowed(EMAIL)
        );
    }

    @Test
    void emailIsNormalized() {
        for (int attempt = 0; attempt < 5; attempt++) {
            loginAttemptService.recordFailedAttempt(
                    "  ADMIN@Hospital.COM "
            );
        }

        assertThrows(
                LoginRateLimitException.class,
                () -> loginAttemptService
                        .checkAllowed(EMAIL)
        );
    }

    @Test
    void invalidConfigurationIsRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new LoginAttemptService(
                        0,
                        900_000,
                        900_000
                )
        );
    }
}

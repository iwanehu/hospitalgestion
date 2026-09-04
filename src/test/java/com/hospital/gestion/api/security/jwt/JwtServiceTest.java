package com.hospital.gestion.api.security.jwt;

import com.hospital.gestion.api.common.enums.Role;
import com.hospital.gestion.api.security.user.HospitalUserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String ISSUER =
            "hospital-api-test";

    private static final long EXPIRATION_MS =
            900_000L;

    private JwtService jwtService;
    private HospitalUserPrincipal principal;

    @BeforeEach
    void setUp() {
        byte[] secretBytes = new byte[32];

        for (int index = 0;
             index < secretBytes.length;
             index++) {
            secretBytes[index] = (byte) (index + 1);
        }

        String encodedSecret = Base64.getEncoder()
                .encodeToString(secretBytes);

        jwtService = new JwtService(
                encodedSecret,
                EXPIRATION_MS,
                ISSUER
        );

        principal = new HospitalUserPrincipal(
                1L,
                "admin@hospital.com",
                "encoded-password",
                Role.ADMIN,
                true
        );
    }

    @Test
    void generateTokenContainsExpectedClaims() {
        String token =
                jwtService.generateAccessToken(principal);

        assertNotNull(token);
        assertFalse(token.isBlank());

        assertEquals(
                principal.getUsername(),
                jwtService.extractUsername(token)
        );

        assertEquals(
                principal.id(),
                jwtService.extractUserId(token)
        );

        assertEquals(
                principal.role().name(),
                jwtService.extractRole(token)
        );

        assertNotNull(jwtService.extractIssuedAt(token));
        assertNotNull(jwtService.extractExpiration(token));
    }

    @Test
    void generatedTokenIsValidForItsPrincipal() {
        String token =
                jwtService.generateAccessToken(principal);

        assertTrue(
                jwtService.isTokenValid(
                        token,
                        principal
                )
        );
    }

    @Test
    void tokenIsInvalidForDifferentPrincipal() {
        String token =
                jwtService.generateAccessToken(principal);

        HospitalUserPrincipal differentPrincipal =
                new HospitalUserPrincipal(
                        2L,
                        "doctor@hospital.com",
                        "encoded-password",
                        Role.DOCTOR,
                        true
                );

        assertFalse(
                jwtService.isTokenValid(
                        token,
                        differentPrincipal
                )
        );
    }

    @Test
    void tokenIsInvalidForInactivePrincipal() {
        String token =
                jwtService.generateAccessToken(principal);

        HospitalUserPrincipal inactivePrincipal =
                new HospitalUserPrincipal(
                        principal.id(),
                        principal.email(),
                        principal.password(),
                        principal.role(),
                        false
                );

        assertFalse(
                jwtService.isTokenValid(
                        token,
                        inactivePrincipal
                )
        );
    }

    @Test
    void manipulatedTokenIsInvalid() {
        String token =
                jwtService.generateAccessToken(principal);

        String[] parts = token.split("\\.");

        String signature = parts[2];

        char replacement =
                signature.charAt(0) == 'A'
                        ? 'B'
                        : 'A';

        String manipulatedSignature =
                replacement + signature.substring(1);

        String manipulatedToken =
                parts[0]
                        + "."
                        + parts[1]
                        + "."
                        + manipulatedSignature;

        assertFalse(
                jwtService.isTokenValid(
                        manipulatedToken,
                        principal
                )
        );
    }

    @Test
    void blankTokenIsInvalid() {
        assertFalse(
                jwtService.isTokenValid(
                        "",
                        principal
                )
        );
    }

    @Test
    void expirationIsReportedInSeconds() {
        assertEquals(
                900L,
                jwtService.getExpirationSeconds()
        );
    }
}

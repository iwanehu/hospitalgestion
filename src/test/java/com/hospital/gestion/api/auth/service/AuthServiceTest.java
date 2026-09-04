package com.hospital.gestion.api.auth.service;

import com.hospital.gestion.api.admin.entity.Admin;
import com.hospital.gestion.api.admin.repository.AdminRepository;
import com.hospital.gestion.api.auth.dto.LoginRequestDTO;
import com.hospital.gestion.api.auth.dto.LoginResponseDTO;
import com.hospital.gestion.api.common.enums.Role;
import com.hospital.gestion.api.security.jwt.JwtService;
import com.hospital.gestion.api.security.user.HospitalUserPrincipal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private LoginAttemptService loginAttemptService;

    @InjectMocks
    private AuthService authService;

    @Test
    void adminLoginGeneratesTokenAndRegistersLastLogin() {
        HospitalUserPrincipal principal =
                principal(
                        1L,
                        "admin@hospital.com",
                        Role.ADMIN
                );

        Authentication authentication =
                authenticated(principal);

        Admin admin = mock(Admin.class);
        when(admin.getId()).thenReturn(1L);

        when(
                authenticationManager.authenticate(
                        any(Authentication.class)
                )
        ).thenReturn(authentication);

        when(adminRepository.findByUser_Id(1L))
                .thenReturn(Optional.of(admin));

        when(jwtService.generateAccessToken(principal))
                .thenReturn("signed-jwt");

        when(jwtService.getExpirationSeconds())
                .thenReturn(900L);

        LoginResponseDTO response =
                authService.login(
                        new LoginRequestDTO(
                                " ADMIN@HOSPITAL.COM ",
                                "ValidPassword123!"
                        )
                );

        assertEquals(
                "signed-jwt",
                response.accessToken()
        );

        assertEquals(
                "Bearer",
                response.tokenType()
        );

        assertEquals(
                900L,
                response.expiresIn()
        );

        assertEquals(
                1L,
                response.userId()
        );

        assertEquals(
                "admin@hospital.com",
                response.email()
        );

        assertEquals(
                "ADMIN",
                response.role()
        );

        verify(admin).registerLogin();
        verify(adminRepository).save(admin);
        verify(jwtService).generateAccessToken(principal);

        ArgumentCaptor<Authentication> captor =
                ArgumentCaptor.forClass(
                        Authentication.class
                );

        verify(authenticationManager)
                .authenticate(captor.capture());

        Authentication submittedAuthentication =
                captor.getValue();

        assertEquals(
                "admin@hospital.com",
                submittedAuthentication.getName()
        );

        assertEquals(
                "ValidPassword123!",
                submittedAuthentication.getCredentials()
        );


        verify(loginAttemptService)
                .checkAllowed("admin@hospital.com");

        verify(loginAttemptService)
                .recordSuccessfulLogin(
                        "admin@hospital.com"
                );

        verify(loginAttemptService, never())
                .recordFailedAttempt(anyString());
    }

    @Test
    void nonAdminLoginDoesNotAccessAdminRepository() {
        HospitalUserPrincipal principal =
                principal(
                        2L,
                        "doctor@hospital.com",
                        Role.DOCTOR
                );

        when(
                authenticationManager.authenticate(
                        any(Authentication.class)
                )
        ).thenReturn(authenticated(principal));

        when(jwtService.generateAccessToken(principal))
                .thenReturn("doctor-jwt");

        when(jwtService.getExpirationSeconds())
                .thenReturn(900L);

        LoginResponseDTO response =
                authService.login(
                        new LoginRequestDTO(
                                "doctor@hospital.com",
                                "ValidPassword123!"
                        )
                );

        assertEquals("doctor-jwt", response.accessToken());
        assertEquals("DOCTOR", response.role());

        verifyNoInteractions(adminRepository);

        verify(loginAttemptService)
                .checkAllowed("doctor@hospital.com");

        verify(loginAttemptService)
                .recordSuccessfulLogin(
                        "doctor@hospital.com"
                );
    }

    @Test
    void adminWithoutProfileCanStillAuthenticate() {
        HospitalUserPrincipal principal =
                principal(
                        10L,
                        "orphan.admin@hospital.com",
                        Role.ADMIN
                );

        when(
                authenticationManager.authenticate(
                        any(Authentication.class)
                )
        ).thenReturn(authenticated(principal));

        when(adminRepository.findByUser_Id(10L))
                .thenReturn(Optional.empty());

        when(jwtService.generateAccessToken(principal))
                .thenReturn("admin-jwt");

        when(jwtService.getExpirationSeconds())
                .thenReturn(900L);

        LoginResponseDTO response =
                authService.login(
                        new LoginRequestDTO(
                                "orphan.admin@hospital.com",
                                "ValidPassword123!"
                        )
                );

        assertEquals("admin-jwt", response.accessToken());

        verify(adminRepository, never())
                .save(any(Admin.class));
    }

    @Test
    void invalidCredentialsDoNotGenerateTokenOrUpdateAdmin() {
        when(
                authenticationManager.authenticate(
                        any(Authentication.class)
                )
        ).thenThrow(
                new BadCredentialsException(
                        "Bad credentials"
                )
        );

        assertThrows(
                BadCredentialsException.class,
                () -> authService.login(
                        new LoginRequestDTO(
                                "admin@hospital.com",
                                "WrongPassword123!"
                        )
                )
        );

        verify(loginAttemptService)
                .checkAllowed("admin@hospital.com");

        verify(loginAttemptService)
                .recordFailedAttempt(
                        "admin@hospital.com"
                );

        verify(loginAttemptService, never())
                .recordSuccessfulLogin(anyString());

        verifyNoInteractions(jwtService);
        verifyNoInteractions(adminRepository);
    }

    private HospitalUserPrincipal principal(
            Long id,
            String email,
            Role role
    ) {
        return new HospitalUserPrincipal(
                id,
                email,
                "encoded-password",
                role,
                true
        );
    }

    private Authentication authenticated(
            HospitalUserPrincipal principal
    ) {
        return new UsernamePasswordAuthenticationToken(
                principal,
                null,
                principal.getAuthorities()
        );
    }
}

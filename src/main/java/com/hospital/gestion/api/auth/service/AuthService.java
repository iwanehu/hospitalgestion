package com.hospital.gestion.api.auth.service;

import com.hospital.gestion.api.admin.repository.AdminRepository;
import com.hospital.gestion.api.auth.dto.LoginRequestDTO;
import com.hospital.gestion.api.auth.dto.LoginResponseDTO;
import com.hospital.gestion.api.common.enums.Role;
import com.hospital.gestion.api.security.jwt.JwtService;
import com.hospital.gestion.api.security.user.HospitalUserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.AuthenticationException;

import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AdminRepository adminRepository;
    private final LoginAttemptService loginAttemptService;



    @Transactional
    public LoginResponseDTO login(
            LoginRequestDTO request
    ) {
        String normalizedEmail = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        loginAttemptService.checkAllowed(normalizedEmail);

        Authentication authentication;

        try {
            authentication =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    normalizedEmail,
                                    request.password()
                            )
                    );

        } catch (AuthenticationException exception) {
            loginAttemptService.recordFailedAttempt(
                    normalizedEmail
            );

            throw exception;
        }

        loginAttemptService.recordSuccessfulLogin(
                normalizedEmail
        );

        HospitalUserPrincipal principal =
                (HospitalUserPrincipal)
                        authentication.getPrincipal();

        registerAdminLogin(principal);

        String accessToken =
                jwtService.generateAccessToken(principal);

        log.info(
                "User authenticated successfully: {}",
                principal.getUsername()
        );

        return new LoginResponseDTO(
                accessToken,
                "Bearer",
                jwtService.getExpirationSeconds(),
                principal.id(),
                principal.getUsername(),
                principal.role().name()
        );
    }

    private void registerAdminLogin(
            HospitalUserPrincipal principal
    ) {
        if (principal.role() != Role.ADMIN) {
            return;
        }

        adminRepository.findByUser_Id(principal.id())
                .ifPresentOrElse(
                        admin -> {
                            admin.registerLogin();
                            adminRepository.save(admin);

                            log.info(
                                    "Last login automatically "
                                            + "registered for admin: {}",
                                    admin.getId()
                            );
                        },
                        () -> log.warn(
                                "Authenticated ADMIN user {} "
                                        + "does not have an admin profile",
                                principal.id()
                        )
                );
    }
}
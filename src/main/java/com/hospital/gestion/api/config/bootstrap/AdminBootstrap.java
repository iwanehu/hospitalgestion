package com.hospital.gestion.api.config.bootstrap;

import com.hospital.gestion.api.admin.entity.Admin;
import com.hospital.gestion.api.admin.repository.AdminRepository;
import com.hospital.gestion.api.common.enums.AdminLevel;
import com.hospital.gestion.api.common.enums.AdminPermission;
import com.hospital.gestion.api.common.enums.Role;
import com.hospital.gestion.api.user.entity.User;
import com.hospital.gestion.api.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition
        .ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

@Component
@Profile("prod")
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(
        prefix = "app.bootstrap-admin",
        name = "enabled",
        havingValue = "true"
)
public class AdminBootstrap implements ApplicationRunner {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap-admin.email:}")
    private String email;

    @Value("${app.bootstrap-admin.password:}")
    private String password;

    @Value("${app.bootstrap-admin.document-id:}")
    private String documentId;

    @Value("${app.bootstrap-admin.first-name:}")
    private String firstName;

    @Value("${app.bootstrap-admin.last-name:}")
    private String lastName;

    @Value("${app.bootstrap-admin.phone:}")
    private String phone;

    @Override
    @Transactional
    public void run(ApplicationArguments arguments) {
        validateConfiguration();

        String normalizedEmail = email
                .trim()
                .toLowerCase(Locale.ROOT);

        User user = userRepository
                .findByEmailIgnoreCase(normalizedEmail)
                .orElseGet(() -> createAdminUser(
                        normalizedEmail
                ));

        if (user.getRole() != Role.ADMIN) {
            throw new IllegalStateException(
                    "Bootstrap email already belongs to "
                            + "a user without ADMIN role"
            );
        }

        if (adminRepository.existsByUser_Id(user.getId())) {
            log.info(
                    "Bootstrap administrator already exists "
                            + "for user id: {}",
                    user.getId()
            );

            return;
        }

        Admin admin = Admin.builder()
                .user(user)
                .adminLevel(AdminLevel.SUPER_ADMIN)
                .department(null)
                .permissions(
                        new ArrayList<>(
                                Arrays.asList(
                                        AdminPermission.values()
                                )
                        )
                )
                .isSuperAdmin(true)
                .build();

        Admin savedAdmin =
                adminRepository.saveAndFlush(admin);

        log.warn(
                "Initial SUPER_ADMIN created with id: {}. "
                        + "Disable BOOTSTRAP_ADMIN_ENABLED "
                        + "and remove the bootstrap password.",
                savedAdmin.getId()
        );
    }

    private User createAdminUser(
            String normalizedEmail
    ) {
        String normalizedDocument =
                documentId.trim();

        if (userRepository.existsByDocumentIdIgnoreCase(
                normalizedDocument
        )) {
            throw new IllegalStateException(
                    "Bootstrap document ID is already registered"
            );
        }

        User user = User.builder()
                .role(Role.ADMIN)
                .email(normalizedEmail)
                .password(
                        passwordEncoder.encode(password)
                )
                .isActive(true)
                .documentId(normalizedDocument)
                .firstName(firstName.trim())
                .lastName(lastName.trim())
                .phone(normalizeNullable(phone))
                .build();

        User savedUser =
                userRepository.saveAndFlush(user);

        log.info(
                "Bootstrap ADMIN user created with id: {}",
                savedUser.getId()
        );

        return savedUser;
    }

    private void validateConfiguration() {
        requireText(email, "BOOTSTRAP_ADMIN_EMAIL");
        requireText(password, "BOOTSTRAP_ADMIN_PASSWORD");
        requireText(
                documentId,
                "BOOTSTRAP_ADMIN_DOCUMENT_ID"
        );
        requireText(
                firstName,
                "BOOTSTRAP_ADMIN_FIRST_NAME"
        );
        requireText(
                lastName,
                "BOOTSTRAP_ADMIN_LAST_NAME"
        );

        if (password.length() < 12) {
            throw new IllegalStateException(
                    "BOOTSTRAP_ADMIN_PASSWORD must contain "
                            + "at least 12 characters"
            );
        }
    }

    private void requireText(
            String value,
            String variableName
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    variableName
                            + " is required when administrator "
                            + "bootstrap is enabled"
            );
        }
    }

    private String normalizeNullable(
            String value
    ) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}

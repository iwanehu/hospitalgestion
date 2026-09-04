package com.hospital.gestion.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME =
            "bearerAuth";

    @Bean
    public OpenAPI hospitalOpenAPI() {
        return new OpenAPI()
                .info(
                        new Info()
                                .title(
                                        "Hospital Management API"
                                )
                                .description(
                                        """
                                        REST API for hospital management.

                                        It manages users, patients, doctors,
                                        nurses, receptionists, administrators,
                                        departments, wards, rooms, beds,
                                        admissions and appointments.
                                        """
                                )
                                .version("1.0.0")
                                .contact(
                                        new Contact()
                                                .name("spriemar")
                                )
                                .license(
                                        new License()
                                                .name(
                                                        "Portfolio project"
                                                )
                                )
                )
                .servers(
                        java.util.List.of(
                                new Server()
                                        .url(
                                                "http://localhost:8080"
                                        )
                                        .description(
                                                "Local development server"
                                        )
                        )
                )
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        SECURITY_SCHEME,
                                        new SecurityScheme()
                                                .name(
                                                        SECURITY_SCHEME
                                                )
                                                .type(
                                                        SecurityScheme.Type.HTTP
                                                )
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                                .description(
                                                        """
                                                        Enter the JWT access token.

                                                        Do not include the
                                                        'Bearer ' prefix.
                                                        """
                                                )
                                )
                )
                .addSecurityItem(
                        new SecurityRequirement()
                                .addList(SECURITY_SCHEME)
                );
    }
}


package com.hospital.gestion.api.auth.dto;

import com.hospital.gestion.api.common.validation.ValidEmail;
import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(

        @ValidEmail
        @NotBlank(message = "Email is required")
        String email,

        @NotBlank(message = "Password is required")
        String password

) {
}
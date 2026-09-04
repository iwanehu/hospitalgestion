package com.hospital.gestion.api.user.dto;

import com.hospital.gestion.api.common.enums.Role;
import com.hospital.gestion.api.common.validation.ValidDocument;
import com.hospital.gestion.api.common.validation.ValidEmail;
import com.hospital.gestion.api.common.validation.ValidPassword;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserRequestDTO(
        @NotNull(message = "ROLE ID is required")
        Role role,


        @ValidEmail
        @NotBlank(message = "Email is required")
        @Size(max = 150)
        String email,


        @ValidPassword
        @NotBlank(message = "Password is required")
         String password,

        @ValidDocument
        @NotBlank(message = "Document ID is required")
        String documentId,


        @NotBlank(message = "First name is required")
        String firstName,

        @NotBlank(message = "Last name is required")
        String lastName,

        @Size(max = 20)
        String phone







) {
}

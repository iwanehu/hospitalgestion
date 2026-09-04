package com.hospital.gestion.api.user.dto;


import com.hospital.gestion.api.common.validation.ValidEmail;
import jakarta.validation.constraints.NotBlank;


public record UserUpdateDTO(


        @ValidEmail
        @NotBlank(message = "Email is required")
        String email,


        @NotBlank(message = "First name is required")
        String firstName,

        @NotBlank(message = "Last name is required")
        String lastName,

        String phone
) {}
package com.hospital.gestion.api.user.dto;

import com.hospital.gestion.api.common.validation.ValidPassword;
import jakarta.validation.constraints.NotBlank;

public record PasswordChangeDTO(
        @NotBlank(message = "Old password is required")
        String oldPassword,

        @ValidPassword
        @NotBlank(message = "New password is required")
        String newPassword,

        @NotBlank(message = "Confirm password is required")
        String confirmPassword
) {
}

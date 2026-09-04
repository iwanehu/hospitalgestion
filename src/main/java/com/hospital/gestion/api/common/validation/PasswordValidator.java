package com.hospital.gestion.api.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordValidator
        implements ConstraintValidator<ValidPassword, String> {

    private static final String PASSWORD_PATTERN =
            "^(?=.*[a-z])"
                    + "(?=.*[A-Z])"
                    + "(?=.*\\d)"
                    + "(?=.*[!@#$%^&*()_+\\-=\\[\\]{};"
                    + "':\"\\\\|,.<>/?])"
                    + ".{8,72}$";

    @Override
    public boolean isValid(
            String password,
            ConstraintValidatorContext context
    ) {
        if (password == null || password.isBlank()) {
            return false;
        }

        return password.matches(PASSWORD_PATTERN);
    }
}
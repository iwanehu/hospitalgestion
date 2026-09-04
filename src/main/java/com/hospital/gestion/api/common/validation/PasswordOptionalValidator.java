package com.hospital.gestion.api.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;


public class PasswordOptionalValidator implements ConstraintValidator<ValidPasswordOptional,String> {
    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {


        if (password == null || password.isBlank()) {
            return true;
        }

        return password.matches(
                "^(?=.*[a-z])" +
                        "(?=.*[A-Z])" +
                        "(?=.*\\d)" +
                        "(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?])" +
                        ".{8,12}$");

    }
}

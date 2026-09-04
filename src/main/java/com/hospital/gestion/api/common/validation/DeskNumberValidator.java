package com.hospital.gestion.api.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;


public class DeskNumberValidator implements ConstraintValidator<ValidDeskNumber, String> {

    private static final String REGEX = "^[A-Za-z]+-[0-9]+$";


    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

        if (value == null || value.isBlank()) {
            return false;
        }
        return value.matches(REGEX);

    }
}

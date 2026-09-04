package com.hospital.gestion.api.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class BedNumberValidator implements ConstraintValidator<ValidBedNumber, String> {


    private static final String REGEX = "^[A-Za-z]{1,4}-?\\d{1,3}$";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return false;
        }
        return value.matches(REGEX);
    }
}

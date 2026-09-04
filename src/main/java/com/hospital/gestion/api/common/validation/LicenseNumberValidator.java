package com.hospital.gestion.api.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;


public class LicenseNumberValidator implements ConstraintValidator<ValidLicenceNumber,String> {
    @Override
    public boolean isValid(String license, ConstraintValidatorContext context) {

        if (license ==null || license.isBlank()) {
            return false;
        }

        return license.matches("^[A-Z]{3}-\\d{5,10}$");



    }
}

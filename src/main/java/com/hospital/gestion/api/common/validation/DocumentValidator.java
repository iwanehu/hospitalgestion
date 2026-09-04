package com.hospital.gestion.api.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DocumentValidator implements ConstraintValidator<ValidDocument, String> {

    @Override
    public boolean isValid(String document, ConstraintValidatorContext context) {
        log.info("validating document: {}", document);

        if (document == null || document.isBlank()) {
            return false;
        }

        boolean isValid = document.matches("^[A-Za-z0-9]{5,20}$");
        log.info("valid document: {}", isValid);
        return isValid;
    }
}
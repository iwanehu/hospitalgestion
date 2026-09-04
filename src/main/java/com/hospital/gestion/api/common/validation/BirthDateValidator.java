package com.hospital.gestion.api.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.Period;

public class BirthDateValidator implements ConstraintValidator<ValidBirthDate, LocalDate> {


    private  int minAge;
    private  int maxAge;

    @Override
    public void initialize(ValidBirthDate constraintAnnotation) {
        this.minAge = constraintAnnotation.minAge();
        this.maxAge = constraintAnnotation.maxAge();
    }

    @Override
    public boolean isValid(LocalDate birthDate, ConstraintValidatorContext context) {
        if (birthDate == null) {
            return false;
        }


        LocalDate today = LocalDate.now();

        // 1. Must be a past date
        if (!birthDate.isBefore(today)) {
            buildCustomMessage(context, "Birth date must be a past date");
            return false;
        }

        // 2. Calculate current age
        int age = Period.between(birthDate, today).getYears();

        // 3. Validate minimum age
        if (age < minAge) {
            buildCustomMessage(context, String.format("Minimum required age is %d years old", minAge));
            return false;
        }

        // 4. Validate maximum age
        if (age > maxAge) {
            buildCustomMessage(context, String.format("Age cannot exceed %d years old", maxAge));
            return false;
        }

        return true;
    }

    private void buildCustomMessage(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
                .addConstraintViolation();
    }
}

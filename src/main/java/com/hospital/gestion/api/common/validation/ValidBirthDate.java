package com.hospital.gestion.api.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = BirthDateValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)


public @interface ValidBirthDate {

    String message() default "La fecha de nacimiento no es válida";

    int minAge() default 0;

    int maxAge() default 130;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

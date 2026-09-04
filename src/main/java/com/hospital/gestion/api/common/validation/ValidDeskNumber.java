package com.hospital.gestion.api.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = DeskNumberValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)

public @interface ValidDeskNumber {

    String message() default "Invalid desk number format";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

package com.hospital.gestion.api.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = BedNumberValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidBedNumber {
    String message() default "Invalid bed number format";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

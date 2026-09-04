package com.hospital.gestion.api.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = {DocumentValidator.class})
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)


public @interface ValidDocument {
    String message() default "Invalid identity document";

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

package com.hospital.gestion.api.common.validation;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = {LicenseNumberValidator.class})
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)


public @interface ValidLicenceNumber {
    String message() default "Invalid medical license format";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

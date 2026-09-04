package com.hospital.gestion.api.common.validation;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = {PasswordOptionalValidator.class})
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)


public @interface ValidPasswordOptional {
    String message() default "The password does not meet the security requirements";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

}

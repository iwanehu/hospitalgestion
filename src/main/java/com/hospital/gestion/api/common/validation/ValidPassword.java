package com.hospital.gestion.api.common.validation;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = {PasswordValidator.class})
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)

public @interface ValidPassword {

    String message() default "The password does not meet the security requirements";

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

}

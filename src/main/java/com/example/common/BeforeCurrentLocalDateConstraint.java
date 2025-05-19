package com.example.common;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Documented
@Constraint(validatedBy = BeforeCurrentLocalDateValidation.class)
@Target( { ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface BeforeCurrentLocalDateConstraint {
    String message() default "Invalid date - Date must be less than current date";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

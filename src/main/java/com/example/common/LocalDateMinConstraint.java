package com.example.common;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Documented
@Constraint(validatedBy = LocalDateMinValidation.class)
@Target( { ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface LocalDateMinConstraint {
    String value() default "01/01/1900";
    String message() default "Invalid date - Date is less than the minimum allowed";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

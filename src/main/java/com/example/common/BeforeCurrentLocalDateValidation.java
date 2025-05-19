package com.example.common;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;

public class BeforeCurrentLocalDateValidation implements ConstraintValidator<BeforeCurrentLocalDateConstraint, LocalDate> {

    @Override
    public void initialize(BeforeCurrentLocalDateConstraint beforeCurrentLocalDateConstraint) {
    }

    @Override
    public boolean isValid(LocalDate inputDate, ConstraintValidatorContext cxt) {
        return inputDate == null || LocalDate.now().compareTo(inputDate) >= 0; //todo timezone?
    }
}

package com.example.common;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LocalDateMinValidation implements ConstraintValidator<LocalDateMinConstraint, LocalDate> {
    private String stringLocalDateMin;

    @Override
    public void initialize(LocalDateMinConstraint dateMinConstraint) {
        stringLocalDateMin = dateMinConstraint.value();
    }

    @Override
    public boolean isValid(LocalDate dateMin,
                           ConstraintValidatorContext cxt) {

        //convert String to LocalDate with formatter
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate localDateMin = LocalDate.parse(stringLocalDateMin, formatter);


        return dateMin == null || dateMin.compareTo(localDateMin) >= 0;
    }
}

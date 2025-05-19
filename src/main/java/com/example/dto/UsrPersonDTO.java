package com.example.dto;

import java.time.LocalDate;

import com.example.common.BeforeCurrentLocalDateConstraint;
import com.example.common.LocalDateMinConstraint;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UsrPersonDTO {
    private String firstName;
    private String lastName;
    @LocalDateMinConstraint()
    @BeforeCurrentLocalDateConstraint()
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;
    private Long genderId;
    private UsrPersonExtDTO personExt;
}

package com.example.enitity;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
//@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties
//@Cacheable
public class ApplicationEntity {
    private Long id;

    private String submissionDate;
    
    private HouseholdEntity houseHold;

      	
}

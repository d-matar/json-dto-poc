package com.example.enitity;


import java.util.List;

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
public class HouseholdEntity {

    private Long id;

    private String landlineNumber;
    private Integer domesticWorkersNumber;
    
    private List<PersonEntity> persons;
    
}

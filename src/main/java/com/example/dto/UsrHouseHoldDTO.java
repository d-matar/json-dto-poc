package com.example.dto;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UsrHouseHoldDTO {
    private Long version;
    private String caseNumber;
    
    private UsrHouseHoldAddressDTO address;
    private List<UsrPersonDTO> persons;
    private UsrHouseHoldExtDTO houseHoldExt;
}

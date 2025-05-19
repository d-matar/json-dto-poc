package com.example.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UsrHouseHoldAddressDTO {
    private Long mouhafazaId;
    private Long kadaaId;
    private Long localityId;
    private String town;
    private String street;
    private String ownership;
    private String floor;
    private String apartment;
    private String nearestLocation;
    private String mobileNumber;
}

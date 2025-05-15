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

//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String landlineNumber;
    private Integer domesticWorkersNumber;
    
    private List<PersonEntity> persons;
    
    
//    private String neighborhood;
//    private String street;
//    private String ownership;
//    private String floor;
//    private String apartment;
//    private String nearestLocation;
//
//    private Integer familyMembersNumber;
//    private Boolean hasDisabledMember;
//    private Boolean familyMembersLiveTogether;
//
//    private String externalId;
//    private String externalSource;
//
//    private Integer caseVersion;
//    private String caseNumber;

    // Basic JSON field for additional external data
//    @Column(columnDefinition = "TEXT")
//    private String externalDataJson;

    // Relationships (you can uncomment and define the mapped entities as needed)

    //@OneToMany(mappedBy = "houseHold", cascade = CascadeType.ALL, orphanRemoval = true)
    //private List<PersonEntity> persons = new ArrayList<>();

    //@OneToMany(mappedBy = "houseHold", cascade = CascadeType.ALL, orphanRemoval = true)
    //private List<AccommodationEntity> accommodations = new ArrayList<>();

    //@OneToMany(mappedBy = "houseHold", cascade = CascadeType.ALL, orphanRemoval = true)
    //private List<AidCardApplicationEntity> aidCardApplications = new ArrayList<>();

    //@OneToOne(mappedBy = "houseHold", cascade = CascadeType.ALL, orphanRemoval = true)
    //private HouseholdExtEntity houseHoldExt;

    // Optional simple town/address relationship IDs
//    private Long mouhafazaId;
//    private Long kadaaId;
//    private Long townId;
//    private Long localityId;

//    public static HouseholdEntity fromJsonMap(Map<String, Object> json) {
//        return HouseholdEntity.builder()
//                .landlineNumber((String) json.get("landlineNumber"))
//                .domesticWorkersNumber((Integer) json.get("domesticWorkersNumber"))
//                .neighborhood((String) json.get("neighborhood"))
//                .street((String) json.get("street"))
//                .ownership((String) json.get("ownership"))
//                .floor((String) json.get("floor"))
//                .apartment((String) json.get("apartment"))
//                .nearestLocation((String) json.get("nearestLocation"))
//                .familyMembersNumber((Integer) json.get("familyMembersNumber"))
//                .hasDisabledMember((Boolean) json.get("hasDisabledMember"))
//                .familyMembersLiveTogether((Boolean) json.get("familyMembersLiveTogether"))
//                .externalId((String) json.get("externalId"))
//                .externalSource((String) json.get("externalSource"))
//                .caseVersion((Integer) json.get("caseVersion"))
//                .caseNumber((String) json.get("caseNumber"))
//                .externalDataJson(json.containsKey("externalData") ? json.get("externalData").toString() : null)
//                .mouhafazaId(json.get("mouhafazaId") != null ? Long.valueOf(json.get("mouhafazaId").toString()) : null)
//                .kadaaId(json.get("kadaaId") != null ? Long.valueOf(json.get("kadaaId").toString()) : null)
//                .townId(json.get("townId") != null ? Long.valueOf(json.get("townId").toString()) : null)
//                .localityId(json.get("localityId") != null ? Long.valueOf(json.get("localityId").toString()) : null)
//                .build();
//    }
}

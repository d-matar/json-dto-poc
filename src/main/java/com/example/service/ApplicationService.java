package com.example.service;

import org.springframework.stereotype.Service;

import com.example.enitity.ApplicationEntity;
import com.fasterxml.jackson.databind.JsonNode;

@Service
public class ApplicationService {
//
//    private final JsonToModelMapper mapper;
//    private final ApplicationRepository appRepo;
//    private final HouseholdRepository householdRepo;
//    private final PersonRepository personRepo;
//
//    public ApplicationService(JsonToModelMapper mapper,
//                              ApplicationRepository appRepo,
//                              HouseholdRepository householdRepo,
//                              PersonRepository personRepo) {
//        this.mapper = mapper;
//        this.appRepo = appRepo;
//        this.householdRepo = householdRepo;
//        this.personRepo = personRepo;
//    }
//
//    public void processApplicationJson(JsonNode rawJson) {
//        ApplicationEntity app = mapper.mapToApplication(rawJson);
//        appRepo.save(app);
//
//        HouseholdEntity household = mapper.mapToHousehold(rawJson);
//        householdRepo.save(household);
//
//        List<PersonEntity> people = mapper.mapToPersons(rawJson);
//        personRepo.saveAll(people);
//    }
}

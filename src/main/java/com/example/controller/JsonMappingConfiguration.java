package com.example.controller;

import java.util.HashMap;
import java.util.Map;


public class JsonMappingConfiguration {

	 private Map<String, String> jsonPathToClassMapping = new HashMap<>();
	    
	    public JsonMappingConfiguration() {
	        // Configure mappings according to business needs
	        jsonPathToClassMapping.put("/Application", "Application.class");
	        jsonPathToClassMapping.put("/Application/Household", "Household.class");
		jsonPathToClassMapping.put("/Application/Household/Person[0]", "Person.class");
	  
	    }
	    
	    public String getClassPathForJsonPath(String jsonPath) {
	        return jsonPathToClassMapping.get(jsonPath);
	    }

}
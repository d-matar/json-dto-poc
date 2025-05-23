package com.example.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

@Component
public class FieldMappingLoader {
    private final Map<String, Map<String, String>> mapping = new HashMap<>();

    public FieldMappingLoader(){
    	try {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        //map of fields for each DTO/model
        TypeReference<Map<String, Map<String, String>>> typeRef = new TypeReference<>() {};
        //read data from a specified yml file
        InputStream input = getClass().getClassLoader().getResourceAsStream("application-mapping.yml");
        mapping.putAll(mapper.readValue(input, typeRef));
    	}catch (IOException  e) {
            throw new RuntimeException("Failed to load field mappings", e);
		}
    }

    //get custom field map for each class, in yml file we are configuring DTO by simple class name
    public Map<String, String> getFieldMappingFor(Class<?> classentity) {
        return mapping.getOrDefault(classentity.getSimpleName(), Collections.emptyMap());
    }
}

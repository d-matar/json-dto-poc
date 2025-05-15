package com.example.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.enitity.ApplicationEntity;
import com.example.enitity.HouseholdEntity;
import com.example.enitity.PersonEntity;
import com.fasterxml.jackson.databind.module.SimpleModule;

@Configuration
public class JacksonConfig {

	
	 private final FieldMappingLoader fieldMappingLoader;

	    public JacksonConfig(FieldMappingLoader fieldMappingLoader) {
	        this.fieldMappingLoader = fieldMappingLoader;
	    }

    @Bean
    Jackson2ObjectMapperBuilderCustomizer customizer() {
	    return builder -> {
	        builder.modules(new SimpleModule() {/**
				 * 
				 */
				private static final long serialVersionUID = 1393288280652951222L;

			{
	            addDeserializer(ApplicationEntity.class, new DynamicDeserializer<>(
	                ApplicationEntity.class, fieldMappingLoader.getFieldMappingFor(ApplicationEntity.class), fieldMappingLoader
	            ));
	            addSerializer(ApplicationEntity.class, new DynamicSerializer<>(
		                ApplicationEntity.class, fieldMappingLoader.getFieldMappingFor(ApplicationEntity.class), fieldMappingLoader
		            ));
	            
	            addDeserializer(HouseholdEntity.class, new DynamicDeserializer<>(
	            		HouseholdEntity.class, fieldMappingLoader.getFieldMappingFor(HouseholdEntity.class) , fieldMappingLoader
		            ));
	            addSerializer(HouseholdEntity.class, new DynamicSerializer<>(
	            		HouseholdEntity.class, fieldMappingLoader.getFieldMappingFor(HouseholdEntity.class), fieldMappingLoader
	            ));
	            
	            addDeserializer(PersonEntity.class, new DynamicDeserializer<>(
	            		PersonEntity.class, fieldMappingLoader.getFieldMappingFor(PersonEntity.class), fieldMappingLoader
		            ));
	            addSerializer(PersonEntity.class, new DynamicSerializer<>(
	            		PersonEntity.class, fieldMappingLoader.getFieldMappingFor(PersonEntity.class), fieldMappingLoader
	            ));
	        }});
	    };
	}
}

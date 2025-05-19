package com.example.configuration;

import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.dto.UsrHouseHoldAddressDTO;
import com.example.dto.UsrHouseHoldDTO;
import com.example.dto.UsrPersonDTO;
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

    /**
     * Customizes Jackson's ObjectMapper to register custom serializers and deserializers.
     */
    @Bean
    Jackson2ObjectMapperBuilderCustomizer customizer() {
        return builder -> {
            // Register a module with custom (de)serializers for DTOs and entities
            builder.modules(new SimpleModule() {
                private static final long serialVersionUID = 1393288280652951222L;

                {
                    // Register custom deserializer and serializer for ApplicationEntity
                    addDeserializer(ApplicationEntity.class, new DynamicDeserializer<>(
                        ApplicationEntity.class,
                        fieldMappingLoader.getFieldMappingFor(ApplicationEntity.class),
                        fieldMappingLoader
                    ));
                    addSerializer(ApplicationEntity.class, new DynamicSerializer<>(
                        ApplicationEntity.class,
                        fieldMappingLoader.getFieldMappingFor(ApplicationEntity.class),
                        fieldMappingLoader
                    ));

                    // HouseholdEntity (de)serialization
                    addDeserializer(HouseholdEntity.class, new DynamicDeserializer<>(
                        HouseholdEntity.class,
                        fieldMappingLoader.getFieldMappingFor(HouseholdEntity.class),
                        fieldMappingLoader
                    ));
                    addSerializer(HouseholdEntity.class, new DynamicSerializer<>(
                        HouseholdEntity.class,
                        fieldMappingLoader.getFieldMappingFor(HouseholdEntity.class),
                        fieldMappingLoader
                    ));

                    // PersonEntity (de)serialization
                    addDeserializer(PersonEntity.class, new DynamicDeserializer<>(
                        PersonEntity.class,
                        fieldMappingLoader.getFieldMappingFor(PersonEntity.class),
                        fieldMappingLoader
                    ));
                    addSerializer(PersonEntity.class, new DynamicSerializer<>(
                        PersonEntity.class,
                        fieldMappingLoader.getFieldMappingFor(PersonEntity.class),
                        fieldMappingLoader
                    ));

                    // UsrHouseHoldDTO (de)serialization
                    addDeserializer(UsrHouseHoldDTO.class, new DynamicDeserializer<>(
                        UsrHouseHoldDTO.class,
                        fieldMappingLoader.getFieldMappingFor(UsrHouseHoldDTO.class),
                        fieldMappingLoader
                    ));
                    addSerializer(UsrHouseHoldDTO.class, new DynamicSerializer<>(
                        UsrHouseHoldDTO.class,
                        fieldMappingLoader.getFieldMappingFor(UsrHouseHoldDTO.class),
                        fieldMappingLoader
                    ));

                    // UsrHouseHoldAddressDTO (de)serialization
                    addDeserializer(UsrHouseHoldAddressDTO.class, new DynamicDeserializer<>(
                        UsrHouseHoldAddressDTO.class,
                        fieldMappingLoader.getFieldMappingFor(UsrHouseHoldAddressDTO.class),
                        fieldMappingLoader
                    ));
                    addSerializer(UsrHouseHoldAddressDTO.class, new DynamicSerializer<>(
                        UsrHouseHoldAddressDTO.class,
                        fieldMappingLoader.getFieldMappingFor(UsrHouseHoldAddressDTO.class),
                        fieldMappingLoader
                    ));

                    // UsrPersonDTO (de)serialization
                    addDeserializer(UsrPersonDTO.class, new DynamicDeserializer<>(
                        UsrPersonDTO.class,
                        fieldMappingLoader.getFieldMappingFor(UsrPersonDTO.class),
                        fieldMappingLoader
                    ));
                    addSerializer(UsrPersonDTO.class, new DynamicSerializer<>(
                        UsrPersonDTO.class,
                        fieldMappingLoader.getFieldMappingFor(UsrPersonDTO.class),
                        fieldMappingLoader
                    ));

                    // TODO: Consider registering a generic (de)serializer for all DTOs/entities
                    // under specific packages to avoid manual configuration for each class.
                }
            });
        };
    }
}

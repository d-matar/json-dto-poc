package com.example.configuration;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class DynamicSerializer<T> extends JsonSerializer<T> {

    private final Class<T> targetClass;
    private final Map<String, String> fieldMap;
    private final Map<String, String> reverseFieldMap;
    private final FieldMappingLoader fieldMappingLoader;

    public DynamicSerializer(Class<T> targetClass, Map<String, String> fieldMap,
                             FieldMappingLoader fieldMappingLoader) {
        this.targetClass = targetClass;
        this.fieldMap = fieldMap;
        this.reverseFieldMap = invertMap(fieldMap); // Maps JSON field names back to Java field names
        this.fieldMappingLoader = fieldMappingLoader;
    }

    private Map<String, String> invertMap(Map<String, String> original) {
        Map<String, String> inverted = new HashMap<>();
        for (Map.Entry<String, String> entry : original.entrySet()) {
            inverted.put(entry.getValue(), entry.getKey());
        }
        return inverted;
    }

    @Override
    public void serialize(T value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        // Create a separate mapper for primitive types to avoid infinite recursion
        ObjectMapper mapper = new ObjectMapper();
        
        mapper.registerModule(new JavaTimeModule()); // Register module for LocalDate, LocalDateTime, etc.
        // TODO: Optionally register Jdk8Module, ParameterNamesModule if needed for Optional, constructors, etc.

        // Use original mapper (which includes custom serializers) for complex types
        ObjectMapper customMapper = (ObjectMapper) gen.getCodec();

        ObjectNode remappedNode = mapper.createObjectNode(); // Resulting JSON node

        for (Field field : targetClass.getDeclaredFields()) {
            field.setAccessible(true);
            String javaFieldName = field.getName();
            String jsonFieldName = reverseFieldMap.get(javaFieldName); // Map to expected JSON key

            if (jsonFieldName == null) continue;

            Object fieldValue;
            try {
                fieldValue = field.get(value);
            } catch (IllegalAccessException e) {
                e.printStackTrace(); // You could throw a runtime exception here instead
                continue;
            }

            if (fieldValue == null) continue;

            Type genericType = field.getGenericType();
            Class<?> fieldType = field.getType();

            if (genericType instanceof ParameterizedType) {
                // Handle collections like List<T>, Set<T>
                Class<?> elementType = null;
                Type[] typeArgs = ((ParameterizedType) genericType).getActualTypeArguments();
                if (typeArgs.length > 0 && typeArgs[0] instanceof Class<?>) {
                    elementType = (Class<?>) typeArgs[0];
                }

                Map<String, String> nestedFieldMap = fieldMappingLoader.getFieldMappingFor(elementType);
                ArrayNode remappedArray = JsonNodeFactory.instance.arrayNode();

                for (Object element : (Iterable<?>) fieldValue) {
                    JsonNode javaNode = (nestedFieldMap != null && !nestedFieldMap.isEmpty())
                            ? customMapper.valueToTree(element)
                            : mapper.valueToTree(element);
                    remappedArray.add(javaNode);
                }
                remappedNode.set(jsonFieldName, remappedArray);
            } else {
                // Handle simple or nested single objects
                Map<String, String> nestedFieldMap = fieldMappingLoader.getFieldMappingFor(fieldType);
                JsonNode javaNode = (nestedFieldMap != null && !nestedFieldMap.isEmpty())
                        ? customMapper.valueToTree(fieldValue)
                        : mapper.valueToTree(fieldValue);
                remappedNode.set(jsonFieldName, javaNode);
            }
        }

        mapper.writeTree(gen, remappedNode); // Write final JSON output
    }
}

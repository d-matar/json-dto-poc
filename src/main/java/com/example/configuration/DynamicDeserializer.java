package com.example.configuration;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class DynamicDeserializer<T> extends StdDeserializer<T> {

    private static final long serialVersionUID = -384344171889402531L;

    private final Class<T> targetClass;
    private final Map<String, String> fieldMap;
    private final FieldMappingLoader fieldMappingLoader;

    private static final Logger logger = LoggerFactory.getLogger(DynamicDeserializer.class);

    public DynamicDeserializer(Class<T> targetClass, Map<String, String> fieldMap, FieldMappingLoader fieldMappingLoader) {
        super(targetClass);
        this.targetClass = targetClass;
        this.fieldMap = fieldMap;
        this.fieldMappingLoader = fieldMappingLoader;
    }

    @Override
    public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        try {
            ObjectNode remappedNode = JsonNodeFactory.instance.objectNode();

            // Mapper for default/simple types (e.g., strings, numbers, dates)
            ObjectMapper defaultMapper = new ObjectMapper();
            defaultMapper.registerModule(new JavaTimeModule());
            // TODO: Optionally register additional modules (Jdk8Module, ParameterNamesModule) if needed

            // Custom mapper (with dynamic serializers) from Jackson context
            ObjectMapper customMapper = (ObjectMapper) p.getCodec();

            // Ensure JSON object starts correctly
            if (p.currentToken() != JsonToken.START_OBJECT) {
                p.nextToken();
                if (p.currentToken() != JsonToken.START_OBJECT) {
                    throw JsonMappingException.from(p, "Expected START_OBJECT token");
                }
            }

            // Iterate over all fields in the incoming JSON
            while (p.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = p.currentName();
                p.nextToken();

                // Map incoming field name to corresponding Java field name
                String javaFieldName = fieldMap.get(fieldName);
                if (javaFieldName != null) {
                    JsonNode valueNode = p.readValueAsTree();
                    Map<String, String> nestedFieldMapping = new HashMap<>();

                    Class<?> fieldType = null;
                    Type genericType = null;
                    Class<?> elementType = null;

                    try {
                        Field declaredField = targetClass.getDeclaredField(javaFieldName);
                        genericType = declaredField.getGenericType();
                        fieldType = declaredField.getType();
                    } catch (Exception e) {
                        logger.error("Error accessing field: {}", javaFieldName, e);
                    }

                    if (genericType instanceof ParameterizedType) {
                        // Handle deserialization of parameterized types (e.g., List<T>, Set<T>)
                        Type[] typeArgs = ((ParameterizedType) genericType).getActualTypeArguments();
                        if (typeArgs.length > 0 && typeArgs[0] instanceof Class<?>) {
                            elementType = (Class<?>) typeArgs[0];
                        }

                        nestedFieldMapping = fieldMappingLoader.getFieldMappingFor(elementType);
                        ArrayNode remappedArray = JsonNodeFactory.instance.arrayNode();

                        for (JsonNode element : valueNode) {
                            JsonNode elementNode = defaultMapper.valueToTree(element);

                            if (nestedFieldMapping != null && !nestedFieldMapping.isEmpty()) {
                                Object mappedObj = customMapper.treeToValue(elementNode, elementType);
                                JsonNode nestedNode = defaultMapper.valueToTree(mappedObj);
                                remappedArray.add(nestedNode);
                            } else {
                                remappedArray.add(elementNode);
                            }
                        }

                        remappedNode.set(javaFieldName, remappedArray);
                    } else {
                        // Handle single (non-collection) fields
                        nestedFieldMapping = fieldMappingLoader.getFieldMappingFor(fieldType);

                        if (nestedFieldMapping != null && !nestedFieldMapping.isEmpty()) {
                            // Custom-mapped nested object
                            Object nestedObj = customMapper.treeToValue(valueNode, fieldType);
                            JsonNode nestedNode = defaultMapper.valueToTree(nestedObj);
                            remappedNode.set(javaFieldName, nestedNode);
                        } else {
                            // Simple object
                            remappedNode.set(javaFieldName, valueNode);
                        }
                    }
                } else {
                    // Skip unmapped fields
                    p.skipChildren();
                }
            }

            // Deserialize the remapped node into the target Java object using the default mapper
            @SuppressWarnings("unchecked")
            T result = (T) defaultMapper.treeToValue(remappedNode, targetClass);
            return result;

        } catch (Exception e) {
            logger.error("Deserialization failed for class {}: {}", targetClass.getSimpleName(), e.getMessage(), e);
            throw e;
        }
    }
}

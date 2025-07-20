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

            // Custom mapper (with dynamic serializers) from Jackson context
            ObjectMapper customMapper = (ObjectMapper) p.getCodec();

            // Parse the entire JSON tree first
            JsonNode rootNode = p.readValueAsTree();
            
            // Keep track of processed nested paths to exclude them from wildcard processing
            Map<String, String> processedNestedPaths = new HashMap<>();
            
            // Process nested path mappings first (both forward and reverse)
            processNestedPathMappings(rootNode, remappedNode, processedNestedPaths);
            
            // Process regular field mappings
            processRegularFieldMappings(rootNode, remappedNode, defaultMapper, customMapper, processedNestedPaths);
            
            // Process wildcard mappings for unmapped fields (excluding processed nested paths)
            processWildcardMappings(rootNode, remappedNode, processedNestedPaths);

            // Log the remapped node for debugging
            logger.debug("Remapped node for {}: {}", targetClass.getSimpleName(), remappedNode.toString());

            // Deserialize the remapped node into the target Java object using the default mapper
            @SuppressWarnings("unchecked")
            T result = (T) defaultMapper.treeToValue(remappedNode, targetClass);
            return result;

        } catch (Exception e) {
            logger.error("Deserialization failed for class {}: {}", targetClass.getSimpleName(), e.getMessage(), e);
            throw e;
        }
    }
    
    private void processNestedPathMappings(JsonNode rootNode, ObjectNode remappedNode, Map<String, String> processedNestedPaths) {
        // Handle nested path mappings like "personal.weight" -> "weight" AND "example" -> "address.example"
        for (Map.Entry<String, String> entry : fieldMap.entrySet()) {
            String jsonPath = entry.getKey();
            String javaFieldName = entry.getValue();
            
            // Skip wildcard mappings
            if ("*".equals(jsonPath)) {
                continue;
            }
            
            // Handle forward nested mappings (e.g., "personal.weight" -> "weight")
            if (jsonPath.contains(".")) {
                JsonNode valueNode = getValueFromPath(rootNode, jsonPath);
                if (valueNode != null && !valueNode.isNull()) {
                    remappedNode.set(javaFieldName, valueNode);
                    // Track this path as processed
                    processedNestedPaths.put(jsonPath, javaFieldName);
                }
            }
            // Handle reverse nested mappings (e.g., "example" -> "address.example")
            else if (javaFieldName.contains(".")) {
                JsonNode valueNode = rootNode.get(jsonPath);
                if (valueNode != null && !valueNode.isNull()) {
                    setValueAtPath(remappedNode, javaFieldName, valueNode);
                    // Track this field as processed
                    processedNestedPaths.put(jsonPath, javaFieldName);
                }
            }
        }
    }
    
    private void processRegularFieldMappings(JsonNode rootNode, ObjectNode remappedNode, 
                                           ObjectMapper defaultMapper, ObjectMapper customMapper,
                                           Map<String, String> processedNestedPaths) throws IOException {
        rootNode.fields().forEachRemaining(entry -> {
            String fieldName = entry.getKey();
            JsonNode valueNode = entry.getValue();
            
            // Skip if this field was already processed as part of nested path mappings
            if (processedNestedPaths.containsKey(fieldName)) {
                return;
            }
            
            // Skip if this field is part of a forward nested path that we've already processed
            if (isPartOfForwardNestedPath(fieldName)) {
                return;
            }
            
            // Map incoming field name to corresponding Java field name
            String javaFieldName = fieldMap.get(fieldName);
            if (javaFieldName != null && !javaFieldName.contains(".")) {
                try {
                    processFieldMapping(javaFieldName, valueNode, remappedNode, defaultMapper, customMapper);
                } catch (Exception e) {
                    logger.error("Error processing field mapping for {}: {}", fieldName, e.getMessage(), e);
                }
            }
        });
    }
    
    private void processFieldMapping(String javaFieldName, JsonNode valueNode, ObjectNode remappedNode,
                                   ObjectMapper defaultMapper, ObjectMapper customMapper) throws IOException {
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
            return; // Early return if field doesn't exist
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
    }
    
    private void processWildcardMappings(JsonNode rootNode, ObjectNode remappedNode, Map<String, String> processedNestedPaths) {
        String wildcardTarget = fieldMap.get("*");
        if (wildcardTarget == null) {
            return;
        }
        
        processNodeForWildcard(rootNode, remappedNode, wildcardTarget, "", processedNestedPaths);
    }
    
    private void processNodeForWildcard(JsonNode node, ObjectNode remappedNode, String wildcardTarget, 
                                       String currentPath, Map<String, String> processedNestedPaths) {
        if (node.isObject()) {
            node.fields().forEachRemaining(entry -> {
                String fieldName = entry.getKey();
                JsonNode valueNode = entry.getValue();
                String fullPath = currentPath.isEmpty() ? fieldName : currentPath + "." + fieldName;
                
                // Skip fields that have explicit mappings
                if (fieldMap.containsKey(fieldName) || fieldMap.containsKey(fullPath)) {
                    return;
                }
                
                // Check if this field or path was processed as part of nested path mappings
                boolean isProcessedNestedPath = processedNestedPaths.containsKey(fullPath) || processedNestedPaths.containsKey(fieldName);
                
                if (valueNode.isObject()) {
                    // For nested objects, we need to check if any of their children were processed
                    ObjectNode filteredObject = filterProcessedFields(valueNode, fullPath, processedNestedPaths);
                    if (filteredObject.size() > 0) {
                        addToWildcardTarget(remappedNode, wildcardTarget, fieldName, filteredObject);
                    }
                } else {
                    // For primitive values, add them if they weren't processed
                    if (!isProcessedNestedPath) {
                        addToWildcardTarget(remappedNode, wildcardTarget, fieldName, valueNode);
                    }
                }
            });
        }
    }
    
    private ObjectNode filterProcessedFields(JsonNode originalNode, String parentPath, Map<String, String> processedNestedPaths) {
        ObjectNode filteredNode = JsonNodeFactory.instance.objectNode();
        
        originalNode.fields().forEachRemaining(entry -> {
            String fieldName = entry.getKey();
            JsonNode valueNode = entry.getValue();
            String fullPath = parentPath + "." + fieldName;
            
            // Only include fields that weren't processed as part of nested path mappings
            if (!processedNestedPaths.containsKey(fullPath)) {
                if (valueNode.isObject()) {
                    ObjectNode nestedFiltered = filterProcessedFields(valueNode, fullPath, processedNestedPaths);
                    if (nestedFiltered.size() > 0) {
                        filteredNode.set(fieldName, nestedFiltered);
                    }
                } else {
                    filteredNode.set(fieldName, valueNode);
                }
            }
        });
        
        return filteredNode;
    }
    
    private void addToWildcardTarget(ObjectNode remappedNode, String wildcardTarget, String fieldName, JsonNode valueNode) {
        String[] path = wildcardTarget.split("\\.");
        ObjectNode currentNode = remappedNode;

        // Traverse or build each part of the path except the last one
        for (int i = 0; i < path.length - 1; i++) {
            String part = path[i];
            JsonNode nextNode = currentNode.get(part);
            if (nextNode == null || !nextNode.isObject()) {
                ObjectNode newNode = JsonNodeFactory.instance.objectNode();
                currentNode.set(part, newNode);
                currentNode = newNode;
            } else {
                currentNode = (ObjectNode) nextNode;
            }
        }

        // Set the field at the final level
        String finalFieldName = path[path.length - 1];
        JsonNode finalNode = currentNode.get(finalFieldName);
        if (finalNode == null || !finalNode.isObject()) {
            finalNode = JsonNodeFactory.instance.objectNode();
            currentNode.set(finalFieldName, finalNode);
        }

        ((ObjectNode) finalNode).set(fieldName, valueNode);
    }
    
    private void setValueAtPath(ObjectNode remappedNode, String path, JsonNode valueNode) {
        String[] pathParts = path.split("\\.");
        ObjectNode currentNode = remappedNode;
        
        // Navigate/create the nested structure up to the parent of the final field
        for (int i = 0; i < pathParts.length - 1; i++) {
            String part = pathParts[i];
            JsonNode nextNode = currentNode.get(part);
            if (nextNode == null || !nextNode.isObject()) {
                ObjectNode newNode = JsonNodeFactory.instance.objectNode();
                currentNode.set(part, newNode);
                currentNode = newNode;
            } else {
                currentNode = (ObjectNode) nextNode;
            }
        }
        
        // Set the final field value
        String finalFieldName = pathParts[pathParts.length - 1];
        currentNode.set(finalFieldName, valueNode);
    }
    
    private JsonNode getValueFromPath(JsonNode rootNode, String path) {
        String[] pathParts = path.split("\\.");
        JsonNode currentNode = rootNode;
        
        for (String part : pathParts) {
            if (currentNode == null || !currentNode.has(part)) {
                return null;
            }
            currentNode = currentNode.get(part);
        }
        
        return currentNode;
    }
    
    private boolean isPartOfForwardNestedPath(String fieldName) {
        return fieldMap.keySet().stream()
                .anyMatch(key -> key.contains(".") && key.startsWith(fieldName + "."));
    }
}
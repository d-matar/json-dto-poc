package com.example.configuration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class DynamicSerializer<T> extends StdSerializer<T> {

    /**Generated serialVersionUID*/
	private static final long serialVersionUID = 8124979037034847980L;
	
	private final Map<String, String> fieldMap; // Original mapping (JSON -> Java)
    private final Map<String, String> reverseFieldMap; // Inverted mapping (Java -> JSON)
    private final FieldMappingLoader fieldMappingLoader;

    public DynamicSerializer(Class<T> targetClass, Map<String, String> fieldMap, FieldMappingLoader fieldMappingLoader) {
        super(targetClass);
        this.fieldMap = fieldMap;
        this.reverseFieldMap = invertMap(fieldMap);
        this.fieldMappingLoader = fieldMappingLoader;
    }

    private Map<String, String> invertMap(Map<String, String> original) {
        Map<String, String> inverted = new HashMap<>();
        original.forEach((jsonPath, javaPath) -> {
            // For wildcards, the java path is the key
            if ("*".equals(jsonPath)) {
                inverted.put(javaPath, jsonPath);
            } else {
                inverted.put(javaPath, jsonPath);
            }
        });
        return inverted;
    }

    @Override
    public void serialize(T value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        // Mapper for primitive types. Configured to exclude nulls.
        ObjectMapper defaultMapper = new ObjectMapper();
        defaultMapper.registerModule(new JavaTimeModule());
        defaultMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        defaultMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL); // FIX: Excludes null fields

        // Original mapper from context for serializing nested objects with custom logic
        ObjectMapper customMapper = (ObjectMapper) gen.getCodec();
        customMapper.registerModule(new JavaTimeModule());

        ObjectNode resultNode = JsonNodeFactory.instance.objectNode();

        // --- Step 1: Handle all declared fields and their forward mappings, including wildcard containers ---
        for (Field field : _handledType.getDeclaredFields()) {
            field.setAccessible(true);
            String javaFieldName = field.getName();
            Object fieldValue;
            try {
                fieldValue = field.get(value);
            } catch (IllegalAccessException e) {
                continue;
            }

            if (fieldValue == null) {
                continue;
            }

            // Check if this field is the container for wildcard attributes (e.g., houseHoldExt, personExt)
            // We need to find the specific javaPath for the wildcard (e.g., "houseHoldExt.extendedAttributes")
            String wildcardJavaPathForThisField = null;
            for (Map.Entry<String, String> entry : fieldMap.entrySet()) {
                if ("*".equals(entry.getKey()) && entry.getValue().startsWith(javaFieldName + ".")) {
                    wildcardJavaPathForThisField = entry.getValue();
                    break;
                }
            }

            if (wildcardJavaPathForThisField != null) {
                try {
                    // Get the actual Map<String, Object> or JsonNode from the nested 'extendedAttributes'
                    Object extendedAttributesValue = getValueFromPath(value, wildcardJavaPathForThisField);
                    if (extendedAttributesValue != null) {
                        if (extendedAttributesValue instanceof Map) {
                            // Convert the map of extended attributes to an ObjectNode
                            ObjectMapper tempMapper = new ObjectMapper(); // Use a temp mapper to avoid recursion issues
                            ObjectNode wildcardContentNode = tempMapper.valueToTree(extendedAttributesValue);
                            mergeWildcardFields(resultNode, wildcardContentNode);
                        } else if (extendedAttributesValue instanceof JsonNode) {
                            mergeWildcardFields(resultNode, (ObjectNode) extendedAttributesValue);
                        }
                    }
                } catch (Exception e) {
                    // Handle reflection exceptions
                }
                continue; // Skip further processing for this field as its contents are merged
            }


            String jsonPath = reverseFieldMap.get(javaFieldName);
            if (jsonPath != null) {
                JsonNode valueNode = createValueNode(fieldValue, field, defaultMapper, customMapper);
                setValueAtPath(resultNode, jsonPath, valueNode);
            }
        }
        
        // --- Step 2: Handle reverse nested mappings (e.g., "address.example" -> "example") ---
        for (Map.Entry<String, String> mapping : fieldMap.entrySet()) {
            String jsonPath = mapping.getKey();
            String javaPath = mapping.getValue();

            // Skip wildcard mapping here as it's handled in Step 1
            if ("*".equals(jsonPath)) {
                continue;
            }

            if (javaPath.contains(".")) { // This identifies a reverse nested mapping
                try {
                    Object fieldValue = getValueFromPath(value, javaPath);
                    if (fieldValue == null) continue;

                    Field field = getFieldFromPath(_handledType, javaPath);
                    JsonNode valueNode = createValueNode(fieldValue, field, defaultMapper, customMapper);
                    
                    // Set the value at the top-level JSON path
                    setValueAtPath(resultNode, jsonPath, valueNode); // Using setValueAtPath for consistency

                    // Remove the duplicated key from the nested object
                    removeFieldFromNestedNode(resultNode, javaPath);

                } catch (Exception e) {
                    // Handle reflection exceptions
                }
            }
        }

        gen.writeTree(resultNode);
    }

    /**
     * Merges fields from the wildcard JsonNode into the final result node.
     * This will correctly merge objects like 'specific' and 'personal'.
     */
    private void mergeWildcardFields(ObjectNode resultNode, ObjectNode wildcardNode) {
        wildcardNode.fields().forEachRemaining(entry -> {
            String fieldName = entry.getKey();
            JsonNode valueNode = entry.getValue();

            JsonNode existingNode = resultNode.get(fieldName);
            if (existingNode != null && existingNode.isObject() && valueNode.isObject()) {
                // If the key exists and both are objects, merge them
                ((ObjectNode) existingNode).setAll((ObjectNode) valueNode);
            } else {
                // Otherwise, just set/overwrite the field
                resultNode.set(fieldName, valueNode);
            }
        });
    }

    /**
     * After moving a field (like 'example') to the top level, this removes it from its original
     * nested location (e.g., from within the 'address' object) to prevent duplication.
     */
    private void removeFieldFromNestedNode(ObjectNode resultNode, String javaPath) {
        String[] javaPathParts = javaPath.split("\\.");
        if (javaPathParts.length < 2) return;

        String parentJavaField = javaPathParts[0];
        String fieldToRemove = javaPathParts[javaPathParts.length - 1];

        // Find the JSON path where the parent object was serialized
        String parentJsonPath = reverseFieldMap.get(parentJavaField);
        // If parentJsonPath is null, it means the parent field itself wasn't directly mapped at the top level
        // For example, if "address" (the parent of "address.example") isn't a direct field of UsrHouseHoldDTO
        // but is part of "specific.address". We need to navigate to the correct parent node.
        if (parentJsonPath == null) {
            // Attempt to find the parent JsonNode by navigating the path segments
            JsonNode currentNode = resultNode;
            for (int i = 0; i < javaPathParts.length - 1; i++) {
                String part = javaPathParts[i];
                // Need to find the JSON name for this Java field part
                String jsonPartName = reverseFieldMap.get(part);
                if (jsonPartName == null) { // If the Java field itself isn't directly mapped
                    jsonPartName = part; // Assume direct name if no explicit mapping
                }
                
                if (currentNode != null && currentNode.isObject() && currentNode.has(jsonPartName)) {
                    currentNode = currentNode.get(jsonPartName);
                } else {
                    currentNode = null; // Path not found
                    break;
                }
            }
            if (currentNode != null && currentNode.isObject()) {
                ((ObjectNode) currentNode).remove(fieldToRemove);
            }
        } else {
            JsonNode parentNode = getNodeFromPath(resultNode, parentJsonPath);
            if (parentNode != null && parentNode.isObject()) {
                ((ObjectNode) parentNode).remove(fieldToRemove);
            }
        }
    }


    // --- Helper Methods ---

    /**
     * Sets a value at a potentially nested path within a parent ObjectNode,
     * creating the nested structure if it doesn't exist.
     *
     * @param node The parent node to modify.
     * @param jsonPath The dot-separated path (e.g., "specific.address").
     * @param value The JsonNode to set at the destination path.
     */
    private void setValueAtPath(ObjectNode node, String jsonPath, JsonNode value) {
        String[] parts = jsonPath.split("\\.");
        ObjectNode currentNode = node;
        
        // Navigate or create the path until the second-to-last part
        for (int i = 0; i < parts.length - 1; i++) {
            String part = parts[i];
            JsonNode nextNode = currentNode.get(part);
            
            // If the next node in the path doesn't exist or isn't an object, create it.
            if (nextNode == null || !nextNode.isObject()) {
                ObjectNode newNode = JsonNodeFactory.instance.objectNode();
                currentNode.set(part, newNode);
                currentNode = newNode;
            } else {
                currentNode = (ObjectNode) nextNode;
            }
        }
        
        // Set the final value at the destination
        currentNode.set(parts[parts.length - 1], value);
    }

    private JsonNode createValueNode(Object fieldValue, Field field, ObjectMapper defaultMapper, ObjectMapper customMapper) {
        Type genericType = field.getGenericType();
        Class<?> fieldType = field.getType();

        // Check for custom mappings on the nested type
        // This is crucial for distinguishing between default serialization and custom serializer for DTOs.
        // It specifically tries to find the custom mapping for the actual type of the nested object, not the collection type.
        Class<?> actualFieldClass = fieldType;
        if (genericType instanceof ParameterizedType) {
            Type[] actualTypeArguments = ((ParameterizedType) genericType).getActualTypeArguments();
            if (actualTypeArguments.length > 0) {
                Type typeArg = actualTypeArguments[0];
                if (typeArg instanceof Class) {
                    actualFieldClass = (Class<?>) typeArg;
                }
            }
        }

        boolean hasCustomMapping = fieldMappingLoader.getFieldMappingFor(actualFieldClass) != null;
        
        ObjectMapper mapperToUse = hasCustomMapping ? customMapper : defaultMapper;
        
        if (fieldValue instanceof Iterable) {
            ArrayNode arrayNode = JsonNodeFactory.instance.arrayNode();
            // Ensure each element in the iterable is also mapped using the correct mapper
            ((Iterable<?>) fieldValue).forEach(element -> arrayNode.add(mapperToUse.valueToTree(element)));
            return arrayNode;
        } else {
            return mapperToUse.valueToTree(fieldValue);
        }
    }

    private Object getValueFromPath(Object source, String path) throws Exception {
        Object current = source;
        for (String part : path.split("\\.")) {
            if (current == null) return null;
            Field field = findField(current.getClass(), part);
            field.setAccessible(true);
            current = field.get(current);
        }
        return current;
    }

    private JsonNode getNodeFromPath(ObjectNode source, String path) {
        JsonNode current = source;
        for (String part : path.split("\\.")) {
            if (current == null || !current.has(part)) return null;
            current = current.get(part);
        }
        return current;
    }

    private Field getFieldFromPath(Class<?> clazz, String path) throws NoSuchFieldException {
        Class<?> currentClass = clazz;
        Field field = null;
        for (String part : path.split("\\.")) {
            if (field != null) {
                currentClass = field.getType();
            }
            field = findField(currentClass, part);
        }
        return field;
    }

    private Field findField(Class<?> clazz, String name) throws NoSuchFieldException {
        Class<?> current = clazz;
        while (current != null) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException("No such field: " + name + " in class " + clazz);
    }
}
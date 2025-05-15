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

public class DynamicDeserializer<T> extends StdDeserializer<T> {
    
    private static final long serialVersionUID = -384344171889402531L;
    
    private final Class<T> targetClass;
    private final Map<String, String> fieldMap;
    
    private FieldMappingLoader fieldMappingLoader;
    
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
            ObjectMapper defaulatMapper = new ObjectMapper();
            ObjectMapper customMapper = (ObjectMapper) p.getCodec();


            // Ensure we're at the start of an object
            if (p.currentToken() != JsonToken.START_OBJECT) {
                p.nextToken();
                if (p.currentToken() != JsonToken.START_OBJECT) {
                	throw JsonMappingException.from(p, "Expected START_OBJECT token");
                }
            }
            
            while (p.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = p.currentName();
                p.nextToken(); 
                
                // Get the mapped field name
                String javaFieldName = fieldMap.get(fieldName);
				if (javaFieldName != null) {
					
					Map<String, String> fieldMapping = new HashMap<>();
					JsonNode valueNode = p.readValueAsTree();
					Class<?> fieldType = null;
					Type genericType = null;
					Class<?> elementType = null;
					try {
						Field declaredField = targetClass.getDeclaredField(javaFieldName);
						genericType = declaredField.getGenericType();
						fieldType = declaredField.getType();

					} catch (Exception e) {
						logger.error(e.getMessage(),e);
					}

					if (genericType != null && genericType instanceof ParameterizedType) {
						//handle custom deserialization for parameterized parameters
						Type[] typeArgs = ((ParameterizedType) genericType).getActualTypeArguments();
						if (typeArgs.length > 0 && typeArgs[0] instanceof Class) {
							elementType = (Class<?>) typeArgs[0];
						}
							fieldMapping = fieldMappingLoader.getFieldMappingFor(elementType);

							ArrayNode remappedArray = JsonNodeFactory.instance.arrayNode();

							for (JsonNode element : valueNode) {
								JsonNode elementNode = defaulatMapper.valueToTree(element);
								if (fieldMapping != null && !fieldMapping.isEmpty()) {

									Object mappedObj = customMapper.treeToValue(elementNode, elementType);
									JsonNode nestedNode = defaulatMapper.valueToTree(mappedObj);
									remappedArray.add(nestedNode);
								} else {
									remappedArray.add(elementNode);
								}

							}
							remappedNode.set(javaFieldName, remappedArray);
					}

					else {
						System.out.println("field type " + fieldType);
						fieldMapping = fieldMappingLoader.getFieldMappingFor(fieldType);
						if (fieldMapping != null && !fieldMapping.isEmpty()) {
							
							//handle nested objects custom deserialization
							Object nestedObj = customMapper.treeToValue(valueNode, fieldType);

							JsonNode nestedNode = defaulatMapper.valueToTree(nestedObj);
							remappedNode.set(javaFieldName, nestedNode);

						}

						else {
							remappedNode.set(javaFieldName, valueNode);
						}
					}
				} else {
                    // Skip this field if it doesn't have a mapping
                    p.skipChildren();
                }
            }
            
            // Use default deserializer to avoid infinite recursion 
            @SuppressWarnings("unchecked")
            T result = (T) defaulatMapper.treeToValue(remappedNode, targetClass);
            
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

}
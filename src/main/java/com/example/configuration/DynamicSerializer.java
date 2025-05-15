package com.example.configuration;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class DynamicSerializer<T> extends JsonSerializer<T> {

	private final Class<T> targetClass;
	private final Map<String, String> fieldMap;
	private final Map<String, String> reverseFieldMap;
	private FieldMappingLoader fieldMappingLoader;

	public DynamicSerializer(Class<T> targetClass, Map<String, String> fieldMap,
			FieldMappingLoader fieldMappingLoader) {
		this.targetClass = targetClass;
		this.fieldMap = fieldMap;
		this.reverseFieldMap = invertMap(fieldMap);
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
		
		ObjectMapper mapper = new ObjectMapper();
		ObjectMapper intialMapper = (ObjectMapper) gen.getCodec();
		ObjectNode remappedNode = mapper.createObjectNode();
		
		
		for (Field field : targetClass.getDeclaredFields()) {
			field.setAccessible(true);
			Class<?> fieldType = field.getType();
			Type genericType = field.getGenericType();
			String javaFieldName = field.getName();
			String jsonFieldName = reverseFieldMap.get(javaFieldName);

			if (jsonFieldName == null)
				continue;
			
			Object fieldValue = null;
			try {
				fieldValue = field.get(value);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (fieldValue == null)
				continue;

			if (genericType != null && genericType instanceof ParameterizedType) {
				//custom serialization for parameterized objects
				Class<?> elementType = null;
				Type[] typeArgs = ((ParameterizedType) genericType).getActualTypeArguments();
				if (typeArgs.length > 0 && typeArgs[0] instanceof Class) {
					elementType = (Class<?>) typeArgs[0];
				}
				Map<String, String> fieldMap = fieldMappingLoader.getFieldMappingFor(elementType);
				
				ArrayNode remappedArray = JsonNodeFactory.instance.arrayNode();
				
				for (Object element : (Iterable<?>) fieldValue) {
					if(fieldMap!=null && !fieldMap.isEmpty()) {
						JsonNode javaNode = intialMapper.valueToTree(element);
						remappedArray.add(javaNode);
					}
					else {
						JsonNode javaNode = mapper.valueToTree(element);
						remappedArray.add(javaNode);
					}

				}
				remappedNode.set(jsonFieldName, remappedArray);

			}
			else {
				Map<String, String> nestedFieldMap = fieldMappingLoader.getFieldMappingFor(fieldType);

				if (nestedFieldMap != null && !nestedFieldMap.isEmpty()) {
					//custom serialization for nested objects
					JsonNode javaNode = intialMapper.valueToTree(fieldValue);
					remappedNode.set(jsonFieldName, javaNode);
				} else {
					//custom serialization for simple objects 
					JsonNode javaNode = mapper.valueToTree(fieldValue);
					remappedNode.set(jsonFieldName, javaNode);
				}
			}
		}
		mapper.writeTree(gen, remappedNode);
	}
}

package com.example.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.enitity.ApplicationEntity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@RestController
@RequestMapping("/application")
public class ApplicationController {


	@Autowired
	private ObjectMapper objectMapper;
	
	private static final Logger logger = LoggerFactory.getLogger(ApplicationController.class);


	public ApplicationController() {
	}

	@PostMapping
	public ResponseEntity<JsonNode> receiveApplication(@RequestBody JsonNode rawJson) {    
		try {
			
			JsonNode applicationNode = rawJson.get("Application");
			
	        if (applicationNode == null) {
	            return ResponseEntity.badRequest().body(
	                objectMapper.createObjectNode().put("error", "Missing 'Application' root element")
	            );
	        }
			ApplicationEntity application = objectMapper.treeToValue(applicationNode, ApplicationEntity.class);
			logger.info("Application deserialized!");
			ObjectNode response = objectMapper.createObjectNode();
			response.set("Application", objectMapper.valueToTree(application));
			return ResponseEntity.ok(response);

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			e.printStackTrace();
			return ResponseEntity.badRequest().body(objectMapper.createObjectNode()
		            .put("error", "Deserialization failed")
		            .put("message", e.getMessage()));		
			}
	}
}

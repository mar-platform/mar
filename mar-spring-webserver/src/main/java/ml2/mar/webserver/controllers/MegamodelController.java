package ml2.mar.webserver.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import mar.analysis.backend.megamodel.TransformationRelationshipsAnalysis;

@RestController
public class MegamodelController {

	@Autowired
	private TransformationRelationshipsAnalysis analysis;
	@Autowired
	private ObjectMapper objectMapper;

	@GetMapping("/graph")
	@CrossOrigin(origins = "http://localhost:3000")
    public String index() throws JsonProcessingException {
        return objectMapper.writeValueAsString(analysis.getRelationships());    	
    }

}

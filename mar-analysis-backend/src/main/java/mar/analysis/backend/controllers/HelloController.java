package mar.analysis.backend.controllers;

import javax.inject.Inject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import mar.analysis.backend.megamodel.TransformationRelationshipsAnalysis;

@Controller("/graph") 
public class HelloController {

	@Inject
	private TransformationRelationshipsAnalysis analysis;
	@Inject
	private ObjectMapper objectMapper;
	
    @Get(produces = MediaType.APPLICATION_JSON) 
    public String index() throws JsonProcessingException {
        return objectMapper.writeValueAsString(analysis.getRelationships());    	
    }
}
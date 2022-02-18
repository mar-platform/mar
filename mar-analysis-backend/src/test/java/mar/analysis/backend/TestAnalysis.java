package mar.analysis.backend;

import java.io.File;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import mar.analysis.backend.megamodel.MegamodelDB;
import mar.analysis.backend.megamodel.TransformationRelationshipsAnalysis;
import mar.analysis.megamodel.model.RelationshipsGraph;

public class TestAnalysis {

	@Test
	public void test() {
    	String fileName = "/home/jesus/projects/mde-ml/mde-repository-analysis/mar-analysis-backend/data/megamodel.db";
    	MegamodelDB db = new MegamodelDB(new File(fileName));
    	TransformationRelationshipsAnalysis analysis = new TransformationRelationshipsAnalysis(db);
    	
    	RelationshipsGraph rel = analysis.getRelationships();
    	System.out.println(rel);
    	
    	ObjectMapper objectMapper = new ObjectMapper();
    	try {
			objectMapper.writeValueAsString(rel);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}

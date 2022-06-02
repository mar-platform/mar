package ml2.mar.webserver;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

import mar.analysis.backend.megamodel.MegamodelDB;
import mar.analysis.backend.megamodel.TransformationRelationshipsAnalysis;

@SpringBootApplication
public class WebserverApplication {

	public static void main(String[] args) {
    	if (args.length != 1) {
    		System.out.println("Megamodel database file required");
    		System.exit(-1);
    	}
    	
    	String fileName = args[0];
    	File megamodelFile = new File(fileName);
    	if (! megamodelFile.exists()) {
    		System.out.println("Megamodel file " + fileName + " doesn't exist");
       		System.exit(-1); 
    	}

    	SpringApplication.run(WebserverApplication.class, args);
	}
	
	@Bean
	@Scope("application")
	public TransformationRelationshipsAnalysis getRelationshipAnalysis(@Autowired ApplicationArguments args) {
		String fileName = args.getNonOptionArgs().get(0);
    	MegamodelDB db = new MegamodelDB(new File(fileName));
    	
		return new TransformationRelationshipsAnalysis(db);
	}
	
	
}

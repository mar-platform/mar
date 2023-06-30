package ml2.mar.webserver;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

import mar.analysis.backend.megamodel.MegamodelDB;
import mar.analysis.backend.megamodel.RawRepositoryDB;
import mar.analysis.backend.megamodel.TransformationRelationshipsAnalysis;

@SpringBootApplication
public class WebserverApplication {

	public static void main(String[] args) {
    	if (args.length != 2) {
    		System.out.println("Megamodel and raw repository database files required");
    		System.exit(-1);
    	}
    	
    	String fileName = args[0];
    	File megamodelFile = new File(fileName);
    	if (! megamodelFile.exists()) {
    		System.out.println("Megamodel file " + fileName + " doesn't exist");
       		System.exit(-1); 
    	}

    	String rawRepoFileName = args[1];
    	File rawRepoFile = new File(rawRepoFileName);
    	if (! rawRepoFile.exists()) {
    		System.out.println("Raw repository file " + rawRepoFileName + " doesn't exist");
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
	
	@Bean
	@Scope("application")
	public MegamodelDB getMegamodelDB(@Autowired ApplicationArguments args) {
		String fileName = args.getNonOptionArgs().get(0);
    	MegamodelDB db = new MegamodelDB(new File(fileName));
    	return db;
	}

	@Bean
	@Scope("application")
	public RawRepositoryDB getRawRepositoryDB(@Autowired ApplicationArguments args) {
		String fileName = args.getNonOptionArgs().get(1);
		RawRepositoryDB db = new RawRepositoryDB(new File(fileName));
    	return db;
	}
}

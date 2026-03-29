package ml2.mar.webserver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.util.ResourceUtils;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import mar.analysis.backend.megamodel.MegamodelDB;
import mar.analysis.backend.megamodel.RawRepositoryDB;
import mar.analysis.backend.megamodel.TransformationRelationshipsAnalysis;
import ml2.mar.webserver.configuration.AnalysisConfiguration;
import ml2.mar.webserver.configuration.AnalysisFilterImpl;

@SpringBootApplication
public class WebserverApplication {

	public static void main(String[] args) {
    	if (args.length < 2) {
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
	public ObjectMapper getMapper() {
		return new ObjectMapper();
	}
	
	@Bean
	@Scope("application")
	public AnalysisConfiguration getConfiguration(@Autowired ApplicationArguments args) throws IOException {
		String configuration;
		if (args.getNonOptionArgs().size() <= 2) {
		    File file = ResourceUtils.getFile("classpath:configuration.yaml");
		    configuration = new String(Files.readAllBytes(file.toPath()));		    
		} else {		
			String fileName = args.getNonOptionArgs().get(2);
			configuration = Files.readString(Path.of(fileName));
		}
	    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		return mapper.readValue(configuration, AnalysisConfiguration.class);
	}
	
	@Bean
	@Scope("application")
	public TransformationRelationshipsAnalysis getRelationshipAnalysis(
			@Autowired MegamodelDB db, 
			@Autowired RawRepositoryDB rawRepository,
			AnalysisConfiguration configuration) {    	
		return new TransformationRelationshipsAnalysis(db, rawRepository, new AnalysisFilterImpl(configuration)).withCache(true);
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

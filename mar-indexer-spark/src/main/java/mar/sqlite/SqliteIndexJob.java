package mar.sqlite;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.internal.resource.UMLResourceFactoryImpl;

import mar.indexer.common.cmd.CmdOptions;
import mar.indexer.common.configuration.IndexJobConfigurationData;
import mar.indexer.common.configuration.InvalidJobSpecification;
import mar.indexer.common.configuration.SingleIndexJob;
import mar.spark.indexer.ModelOrigin;
import mar.validation.AnalysisDB;
import mar.validation.AnalysisDB.Model;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "sqlite-indexer", mixinStandardHelpOptions = true, description = "Index models in Sqlite")
public class SqliteIndexJob implements Callable<Integer> {

	@Parameters(index = "0", description = "The configuration file.")
	private File configurationFile;

	@Parameters(index = "1", description = "The output db file.")
	private File outputDb;
	
	@Option(required = true, names = { "-t", "--type" }, description = "The model type: ecore, genmymodel-bpmn, uml")
	private String modelType;

	@Option(required = false, names = { "-r", "--repository" }, description = "A specific repository in the configuration file")
	private String repository = null;
	
	public static void main(String[] args) throws IOException, InvalidJobSpecification {
		int exitCode = new CommandLine(new SqliteIndexJob()).execute(args);
		System.exit(exitCode);
	}
	
	@Override
	public Integer call() throws Exception {	
	    IndexJobConfigurationData data = CmdOptions.readConfiguration(configurationFile);
	    List<SingleIndexJob> jobs;
	    if (repository == null) { 
	    	jobs = data.getRepositoriesOfType(modelType);
	    } else {
	    	SingleIndexJob repoConf = data.getRepo(repository);
	    	if (repoConf == null)
	    		throw new IllegalArgumentException("Repository " + repository + " not found.");
	    	jobs = Collections.singletonList(repoConf);
	    }
	    
	    if (jobs.isEmpty()) 
	    	throw new IllegalArgumentException("Can't find job for modelType = " + modelType + (repository == null ? "" : "and repository = " + repository));
	    		
		initModels();
		
		List<ModelOrigin> origins = new ArrayList<>();
		for (SingleIndexJob repoConf : jobs) {
			AnalysisDB db = new AnalysisDB(repoConf.getModelDbFile());
			String rootFolder = repoConf.getRootFolder();
			List<Model> allModels = db.getValidModels(f -> rootFolder + File.separator + f);
			for (Model model : allModels) {
				origins.add(new ModelOrigin(model.getFile().getAbsolutePath(), model.getId(), repoConf, model.getMetadata()));
			}			
		}

		long init = System.currentTimeMillis();
		try (SqliteIndexDatabase db = new SqliteIndexDatabase(outputDb)) {
			new SqliteIndexer().index(db, origins);
		}
		long end = System.currentTimeMillis();
		System.out.println("Index time: " + ((end - init) / (1000.0 * 60)));
		
	    return 0;
	}
	
	/* pp */ static void initModels() {
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap( ).put("ecore", new EcoreResourceFactoryImpl());
    	Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap( ).put("xmi", new XMIResourceFactoryImpl());
    	Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap( ).put("uml", new UMLResourceFactoryImpl());
    	EPackage.Registry.INSTANCE.put(UMLPackage.eINSTANCE.getNsURI(), UMLPackage.eINSTANCE);	
	}

}

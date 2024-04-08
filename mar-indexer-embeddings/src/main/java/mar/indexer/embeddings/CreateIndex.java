package mar.indexer.embeddings;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import mar.embeddings.IndexedDB;
import mar.embeddings.IndexedDB.IndexedModel;
import mar.indexer.common.cmd.CmdOptions;
import mar.indexer.common.configuration.IndexJobConfigurationData;
import mar.indexer.common.configuration.SingleIndexJob;
import mar.indexer.embeddings.EmbeddingStrategy.GloveWordE;
import mar.indexer.embeddings.WordExtractor.NameExtractor;
import mar.modelling.loader.ILoader;
import mar.validation.AnalyserRegistry;
import mar.validation.AnalysisDB;
import mar.validation.AnalysisDB.Model;
import mar.validation.ResourceAnalyser.Factory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "create-index", mixinStandardHelpOptions = true, description = "Index models with JVector and Embeddings")
public class CreateIndex implements Callable<Integer> {

	@Parameters(index = "0", description = "The configuration file.")
	private File configurationFile;

	@Parameters(index = "1", description = "The path to the JVector index")
	private File pathIndexVector;

	@Parameters(index = "2", description = "The path to the Index DB")
	private File pathIndexDB;

	@Option(required = false, names = { "-t", "--type" }, description = "The model type: ecore, bpmn2, uml")
	private String type;

	@Option(required = false, names = { "-r",
			"--repository" }, description = "A specific repository in the configuration file")
	private String repository = null;

	@Option(required = false, names = { "-all" }, description = "Index all repositories of the configuration file")
	private boolean all = false;
	
	@Override
	public Integer call() throws Exception {		
		
		if (type == null && repository == null && !all) {
			System.out.println("Invalid arguments. Expecting -type or -repository or -all");
			return -1;
		}

	    IndexJobConfigurationData data = CmdOptions.readConfiguration(configurationFile);	    

		List<SingleIndexJob> jobs = new ArrayList<SingleIndexJob>();
		if (type != null) {
			jobs.addAll(data.getRepositoriesOfType(type));
		} else if (repository != null) {
			SingleIndexJob repo = data.getRepo(repository);
			if (repo == null) {
				System.out.println("No repository " + repository);
				return -1;
			}
			jobs.add(repo);
		} else if (all) {
			jobs.addAll(data.getRepositories());
		}

		Factory registry = AnalyserRegistry.INSTANCE.getFactory(type);
		registry.configureEnvironment();
		
		List<Model> models = new ArrayList<>();
		for (SingleIndexJob repoConf : jobs) {
			try {
				models.addAll(getModels(repoConf));
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Error indexing: " + repoConf.getRootFolder());
			}
		}
		
		if (this.pathIndexDB.exists()) {
			this.pathIndexDB.delete();
		}
		
		//models = models.subList(0, 1000);
		
		long start = System.currentTimeMillis();
		
		Files.deleteIfExists(this.pathIndexDB.toPath());
		
		GloveWordE embedding = new EmbeddingStrategy.GloveWordE(data.getEmbedding("glove"));
		ILoader loader = registry.newLoader();
		WordExtractor extractor = NameExtractor.NAME_EXTRACTOR;
		
		try (IndexedDB db = new IndexedDB(this.pathIndexDB, IndexedDB.Mode.WRITE)) {
			List<WordedModel> newModels = new ArrayList<>();
			for (Model model : models) {
				IndexedModel indexedModel = db.addModel(model);
				newModels.add( new WordedModel(indexedModel, extractor, loader) );
			}
			
			EmbeddingIndexer indexer = new EmbeddingIndexer(embedding);
			indexer.indexModels(pathIndexVector, newModels);
		}
		
		long end = System.currentTimeMillis();
		System.out.println("Time: " + (end - start) / 1000.0);
		
        return 0;
	}

	private List<Model> getModels(SingleIndexJob repoConf) throws SQLException, IOException {
		String rootFolder = repoConf.getRootFolder();					
		
    	try(AnalysisDB analysisDB = new AnalysisDB(repoConf.getModelDbFile())) {
    		List<Model> models = analysisDB.getValidModels(r -> rootFolder + File.separator + r);
    		return models;
    	}
            
	}
	
	public static void main(String[] args) {
		int exitCode = new CommandLine(new CreateIndex()).execute(args);
		System.exit(exitCode);
	}
	

}

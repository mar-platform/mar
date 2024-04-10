package mar.indexer.embeddings;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;

import mar.embeddings.IndexedDB;
import mar.embeddings.IndexedDB.IndexedModel;
import mar.embeddings.JVectorDatabase;
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

	@Parameters(index = "1", description = "Folder where the index will live")
	private File pathIndex;

	@Option(required = false, names = { "-t", "--type" }, description = "The model type: ecore, bpmn2, uml")
	private String type;

	@Option(required = true, names = { "-e", "--embedding" }, description = "The embedding strategy")
	private EmbeddingOption embeddingOption = EmbeddingOption.ALL_NAMES_GLOVE_MDE;
	
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
		
		File pathIndexVector = JVectorDatabase.getJVectorDbFile(pathIndex, type);
		File pathIndexDB = JVectorDatabase.getSqliteInfoDbFile(pathIndex, type);
		File propertiesFile = JVectorDatabase.getDbPropertiesFile(pathIndex, type);

		Files.deleteIfExists(pathIndexDB.toPath());
		
		long start = System.currentTimeMillis();
		
		
		EmbeddingStrategy embedding;
		WordExtractor extractor;
		
		System.out.println("Using embedding strategy: " + embeddingOption);
		switch (embeddingOption) {
		case ALL_NAMES_GLOVE_MDE:
			extractor = NameExtractor.NAME_EXTRACTOR;
			embedding = new EmbeddingStrategy.GloveWordE(data.getEmbedding("glove"));
			break;
		case ALL_NAMES_FASTTEXT:
			extractor = NameExtractor.NAME_EXTRACTOR;
			embedding = new EmbeddingStrategy.FastTextWordE(data.getEmbedding("fasttext"));
			break;
		case CONCAT_GLOVE_MDE:
			extractor = NameExtractor.ECLASS_FEATURE_EXTRACTOR;
			embedding = new EmbeddingStrategy.GloveConcatEmbeddings(data.getEmbedding("glove"));
			break;			
		default:
			throw new IllegalStateException();
		}
		

		ILoader loader = registry.newLoader();		
		
		try (IndexedDB db = new IndexedDB(pathIndexDB, IndexedDB.Mode.WRITE)) {
			List<WordedModel> newModels = new ArrayList<>();
			for (Model model : models) {
				IndexedModel indexedModel = db.addModel(model);
				newModels.add( new WordedModel(indexedModel, extractor, loader) );
			}
			
			EmbeddingIndexer indexer = new EmbeddingIndexer(embedding);
			indexer.indexModels(pathIndexVector, newModels);
		}

		
	    Properties properties = new Properties();	    
	    properties.setProperty("distance", "dot_product");
	    properties.setProperty("embedding", embeddingOption.name());

        try (OutputStream output = new FileOutputStream(propertiesFile)) {
            properties.store(output, "Config Properties");
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
	
	private static enum EmbeddingOption {
		ALL_NAMES_GLOVE_MDE,
		CONCAT_GLOVE_MDE,
		ALL_NAMES_FASTTEXT
	}
	
	public static void main(String[] args) {
		int exitCode = new CommandLine(new CreateIndex()).execute(args);
		System.exit(exitCode);
	}
	
}

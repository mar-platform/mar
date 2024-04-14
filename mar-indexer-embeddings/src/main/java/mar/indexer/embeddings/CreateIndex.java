package mar.indexer.embeddings;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;

import org.agrona.collections.Int2ObjectHashMap;
import org.apache.logging.log4j.util.Strings;

import com.google.common.base.Preconditions;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import mar.embeddings.IndexedDB;
import mar.embeddings.IndexedDB.IndexedModel;
import mar.embeddings.JVectorDatabase;
import mar.embeddings.PathIndexesDB;
import mar.embeddings.PathIndexesDB.Mode;
import mar.indexer.common.cmd.CmdOptions;
import mar.indexer.common.configuration.IndexJobConfigurationData;
import mar.indexer.common.configuration.SingleIndexJob;
import mar.indexer.embeddings.WordExtractor.NameExtractor;
import mar.modelling.loader.ILoader;
import mar.paths.PathParser;
import mar.sqlite.SqliteIndexDatabase;
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

	@Option(required = false, names = { "-p", "--paths" }, description = "The SQLite database containing the paths")
	private File sqlitePathIndex;

	
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

		File pathIndexVector = JVectorDatabase.getJVectorDbFile(pathIndex, type);
		File pathIndexDB = JVectorDatabase.getSqliteInfoDbFile(pathIndex, type);
		File pathPathIndexDB = JVectorDatabase.getJVectorPathIndexDbFile(pathIndex, type);
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

		if (sqlitePathIndex == null) {			
			List<Model> models = new ArrayList<>();
			for (SingleIndexJob repoConf : jobs) {
				try {
					models.addAll(getModels(repoConf));
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("Error indexing: " + repoConf.getRootFolder());
				}
			}
			
			try (IndexedDB db = new IndexedDB(pathIndexDB, IndexedDB.Mode.WRITE)) {
				List<WordedModel> newModels = new ArrayList<>();
				for (Model model : models) {
					IndexedModel indexedModel = db.addModel(model);
					newModels.add( new WordedModel(indexedModel, extractor, loader) );
				}
				
				EmbeddingIndexer indexer = new EmbeddingIndexer(embedding);
				indexer.indexModels(pathIndexVector, newModels);
			}
		} else {
			// Lazy loading 
			
//			try(SqliteIndexDatabase db = new SqliteIndexDatabase(sqlitePathIndex)) {
//				PathEmbeddingIndexer indexer = new PathEmbeddingIndexer(embedding);
//				indexer.indexModels(db, pathIndexVector);
//			}			
			
//			// All paths
//			List<VectorizedPath> paths = new ArrayList<VectorizedPath>(8192);
//			
//			try(SqliteIndexDatabase db = new SqliteIndexDatabase(sqlitePathIndex)) {
//				db.getPaths((pathId, path) -> {
//					String[] words = PathParser.INSTANCE.toAttributeValues(path);
//					Preconditions.checkState(pathId < Integer.MAX_VALUE);
//					int seqId = (int) pathId;
//					
//					
//					float[] vector = embedding.toNormalizedVector(ipath);					
//					
//					VectorizedPath vp = new VectorizedPath(seqId, vector);
//					paths.add(vp);
//				});
//			}
//			System.out.println("Finished computing path embeddings: " + paths.size());
//			
//			EmbeddingIndexer indexer = new EmbeddingIndexer(embedding);
//			indexer.indexModels(pathIndexVector, paths);
			

			List<VectorizedPath> paths = new ArrayList<VectorizedPath>(8192);
			Map<String, VectorizedPath> uniquePaths = new HashMap<>();
			
			try(SqliteIndexDatabase db = new SqliteIndexDatabase(sqlitePathIndex)) {
				db.getPaths((pathId, path) -> {
					//String[] words = PathParser.INSTANCE.toAttributeValues(path);
					String[] words = PathParser.INSTANCE.toFullPath(path);
					Preconditions.checkState(pathId < Integer.MAX_VALUE);
					
					String pathKey = String.join(",", words);
					int seqId = uniquePaths.size() + 1;
					
					VectorizedPath vp = uniquePaths.get(pathKey);
					if (vp == null) {
						IndexedPath ipath = new IndexedPath(seqId, words);	
						float[] vector = embedding.toNormalizedVector(ipath);					
						vp = new VectorizedPath(seqId, vector, path);
						uniquePaths.put(pathKey, vp);
						
						paths.add(vp);
					}
					
					vp.addPathId((int) pathId);
					
				});
			}
			
			
			
			System.out.println("Finished computing path embeddings: " + paths.size());
			
			Files.deleteIfExists(pathPathIndexDB.toPath());
			try(PathIndexesDB pathDB = new PathIndexesDB(pathPathIndexDB, Mode.WRITE)) {
				for (VectorizedPath vp: paths) {
					pathDB.addPath(vp);				
				}
			}
			
			EmbeddingIndexer indexer = new EmbeddingIndexer(embedding);
			indexer.indexModels(pathIndexVector, paths);
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

	/*
	private void indexByPath() {
		Int2ObjectHashMap<Map<String, VectorizedPath>> pathsByIndex = new Int2ObjectHashMap<Map<String, VectorizedPath>>();
		//List<VectorizedPath> paths = new ArrayList<VectorizedPath>(8192);
		//Map<String, VectorizedPath> uniquePaths = new HashMap<>();
		
		try(SqliteIndexDatabase db = new SqliteIndexDatabase(sqlitePathIndex)) {
			db.getPaths((pathId, path) -> {
				//String[] words = PathParser.INSTANCE.toAttributeValues(path);
				String[] words = PathParser.INSTANCE.toFullPath(path);
				Preconditions.checkState(pathId < Integer.MAX_VALUE);
				
				String pathKey = String.join(",", words);
				int pathSize = PathParser.INSTANCE.getPathSize(path);
				Map<String, VectorizedPath> uniquePaths = pathsByIndex.computeIfAbsent(pathSize, (k) -> new HashMap<String, VectorizedPath>());
				
				VectorizedPath vp = uniquePaths.get(pathKey);
				if (vp == null) {
					int seqId = uniquePaths.size() + 1;

					IndexedPath ipath = new IndexedPath(seqId, words);	
					float[] vector = embedding.toNormalizedVector(ipath);					
					vp = new VectorizedPath(seqId, vector);
					uniquePaths.put(pathKey, vp);
				}
				
				vp.addPathId((int) pathId);
				
			});
		}
		
		System.out.println("Finished computing path embeddings: ");
		pathsByIndex.forEach((len, mapOfPaths) -> {
			System.out.println("  " + len + " : " + mapOfPaths.size());
		});
		
		
		pathsByIndex.forEach((len, mapOfPaths) -> {
			File fileSqlite = new File(pathPathIndexDB.toString() + "." + len);
			File fileVector = new File(pathIndexVector.toString() + "." + len);
			
			List<VectorizedPath> pathsOfLen = new ArrayList<>(mapOfPaths.values());
			Collections.sort(pathsOfLen, (v1, v2) -> Integer.compare(v1.getSeqId(), v2.getSeqId()));
			
			
			try {
				Files.deleteIfExists(fileSqlite.toPath());

				try(PathIndexesDB pathDB = new PathIndexesDB(fileSqlite, Mode.WRITE)) {
					for (VectorizedPath vp: pathsOfLen) {
						pathDB.addPath(vp);				
					}
				}
				
				EmbeddingIndexer indexer = new EmbeddingIndexer(embedding);
				indexer.indexModels(fileVector, pathsOfLen);				
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
		
	}
*/
}

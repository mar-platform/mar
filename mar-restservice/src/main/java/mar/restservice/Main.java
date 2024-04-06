package mar.restservice;

import static spark.Spark.get;
import static spark.Spark.port;
import static spark.Spark.post;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexNotFoundException;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TotalHits;
import org.apache.lucene.search.TotalHits.Relation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.uml2.uml.UMLPackage;

import avro.shaded.com.google.common.base.Preconditions;
import edu.umd.cs.findbugs.annotations.NonNull;
import mar.MarChatBotConfiguration;
import mar.MarConfiguration;
import mar.indexer.common.cmd.CmdOptions;
import mar.indexer.common.configuration.EnvironmentVariables;
import mar.indexer.common.configuration.EnvironmentVariables.MAR;
import mar.indexer.common.configuration.IndexJobConfigurationData;
import mar.indexer.common.configuration.InvalidJobSpecification;
import mar.indexer.common.configuration.SingleIndexJob;
import mar.indexer.embeddings.EmbeddingStrategy;
import mar.indexer.embeddings.EmbeddingStrategy.GloveWordE;
import mar.indexer.lucene.core.ITextSearcher;
import mar.indexer.lucene.core.Searcher;
import mar.model2graph.AbstractPathComputation;
import mar.model2graph.Model2GraphAllpaths;
import mar.paths.PathFactory;
import mar.restservice.scoring.JVectorScorer;
import mar.restservice.scoring.SqliteScorer;
import mar.restservice.services.API;
import mar.restservice.services.IConfigurationProvider;
import mar.restservice.services.InvalidMarRequest;
import mar.sqlite.SqliteIndexDatabase;
import mar.validation.AnalysisDB;
import mar.validation.AnalysisDB.Model;
import spark.Request;
import spark.Response;
import spark.Spark;

public class Main implements IConfigurationProvider {

	/** This is to reuse scorers and their connections */
	private static Map<String, MarConfiguration> configurationCache = new HashMap<String, MarConfiguration>();
	
	/** The system configuration in terms of model types and repositories */
	private IndexJobConfigurationData configuration;
	/** The list of all models by ID and its location in the filesystem */
	private final Map<String, String> modelFilesById = new HashMap<String, String>();

	private StorageKind storageKind;

	public Main(@Nonnull IndexJobConfigurationData configuration, StorageKind storageKind) {
		this.configuration = configuration;
		this.storageKind = storageKind;
		preLoad();
	}
	
	public void preLoad() {
		for (SingleIndexJob job : this.configuration.getRepositories()) {
			if (! job.hasModelDb())
				continue;
			
			if (! job.getModelDbFile().exists()) {
				System.err.println("File " + job.getModelDbFile() + " does not exist");
				continue;
			}
			
			try(AnalysisDB db = new AnalysisDB(job.getModelDbFile())) {
				List<Model> result = db.getValidModels(f -> Paths.get(job.getRootFolder(), f).toString());
				for (Model model : result) {
					modelFilesById.put(model.getId(), model.getFile().getAbsolutePath());					
				}
			} catch (SQLException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		};
	}

	private static Map<String, MarChatBotConfiguration> configurationCacheCB = new HashMap<String, MarChatBotConfiguration>();

	public static void main(String[] args) throws IOException, ParseException {
		Options options = new Options();

		Option input = new Option("p", "port", true, "port number");
		input.setType(Integer.class);
		input.setRequired(true);
		options.addOption(input);

		Option config = new Option("c", "config", true, "Configuration file");
		input.setType(String.class);
		input.setRequired(true);
		options.addOption(config);

		Option debugOpt = new Option("d", "debug", false, "debug mode");
		debugOpt.setRequired(false);
		options.addOption(debugOpt);

		Option storageOpt = new Option("s", "storage", true, "type of storage");
		storageOpt.setRequired(false);
		options.addOption(storageOpt);
		
		CommandLineParser parser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();
		int port = 1234;
		boolean debug = false;
		File configurationFile;
		StorageKind storageKind = StorageKind.JVECTOR;
		
		try {
			CommandLine cmd = parser.parse(options, args);
			port = Integer.parseInt(cmd.getOptionValue("port"));
			debug = cmd.hasOption("debug");
			if (cmd.hasOption(config.getOpt())) {
				configurationFile = new File(cmd.getOptionValue(config.getOpt()));				
			} else {
				formatter.printHelp("mar", options);
				return;
			}
			
			if (cmd.hasOption(storageOpt.getOpt())) {
				String str = cmd.getOptionValue("storage");
				storageKind = StorageKind.valueOf(str.toUpperCase());
				if (storageKind == null) {
					System.out.println("Invalid storage: " + str);
					System.exit(1);
				}
			}
		} catch (ParseException e) {
			System.out.println(e.getMessage());
			formatter.printHelp("mar", options);
			System.exit(1);
			return;
		}

		IndexJobConfigurationData configuration = CmdOptions.readConfiguration("file:/" + configurationFile.getAbsolutePath());
		
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
		EPackage.Registry.INSTANCE.put(UMLPackage.eINSTANCE.getNsURI(), UMLPackage.eINSTANCE);
		port(port);

		// Spark.staticFiles.location("/web/html");
		Spark.externalStaticFileLocation(Paths.get(EnvironmentVariables.getVariable(MAR.REPO_MAR), "mar-svelte/public").toString());
		Spark.staticFiles.expireTime(debug ? 0 : 60 * 10);

		
		if (debug) {
			System.out.println("Debug mode. Allowing CORS.");
			Spark.after((request, response) -> {
				response.header("Access-Control-Allow-Origin", "*");
				response.header("Access-Control-Request-Method", "*");
				response.header("Access-Control-Allow-Headers", "*");
			});
		}		

		Main main = new Main(configuration, storageKind);
		// Services
		API api = new API(main);
		
		// Disabled, it is not used
		// APIchatbot apiChatBot = new APIchatbot(main);

		if (debug) {
			post("/read-configuration", (req, res) -> {
				String configBody = req.body();
				IndexJobConfigurationData data = IndexJobConfigurationData.fromJSON(configBody);
				main.configuration = data;
				return "ok";				
			});
		}
		
		get("/getStopWords", main::getStopWords);

		Spark.exception(InvalidMarRequest.class, (exception, request, response) -> {
			response.body(exception.getMessage());
			response.status(400);
		});
		
		Spark.exception(Exception.class, (exception, request, response) -> {
			exception.printStackTrace();
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			exception.printStackTrace(new PrintStream(bos));			
			response.body("<html><body><h1>Internal error</h1><pre>" + exception.getMessage() + "</pre><h2>Trace</h2><pre>" + bos.toString() + "</pre></body></html>");
			response.status(500);
		});

		System.out.println("Started...");
	}

	public Object getStopWords(Request req, Response res) throws IOException {
		String modelType = req.queryParams("model");
		return getConfiguration(modelType).getScorer().getStopWords(modelType).toString();
	}

	@Override
	public MarConfiguration getConfiguration(@NonNull String modelType) {
		MarConfiguration conf = configurationCache.get(modelType);
		if (conf == null) {
			SingleIndexJob modelConf = configuration.getModelConfigurationByType(modelType);
			
			AbstractPathComputation pathComputation;
			if (modelConf != null) {
				PathFactory pathFactory;
				try {
					pathFactory = modelConf.getPathFactory();
				} catch (InvalidJobSpecification e) {
					pathFactory = new PathFactory.DefaultPathFactory();
				}
				pathComputation = new Model2GraphAllpaths(modelConf.getGraphLength()).
						withPathFactory(pathFactory).
						withFilter(modelConf.getTextMetaFilter());
			} else {
				pathComputation = new Model2GraphAllpaths(3).withPathFactory(new PathFactory.DefaultPathFactory());			
			}
						
			IScorer hsf = newScorer(pathComputation, modelType);
			
			conf = new MarConfiguration(pathComputation, hsf);
			configurationCache.put(modelType, conf);
		}
		return conf;
	}
	
	@Override
	public ITextSearcher newSearcher() {
		try {
			File dbFolder = new File(EnvironmentVariables.getVariable(MAR.INDEX_TARGET));
			return new Searcher(Paths.get(dbFolder.getAbsolutePath(), "lucene").toString());
		} catch (IndexNotFoundException e) {
			System.err.println("No index available: " + e.getMessage());
			return new ITextSearcher() {
				@Override
				public TopDocs topDocs(String searchQuery, PathFactory pf)
						throws org.apache.lucene.queryparser.classic.ParseException, IOException {
					return new TopDocs(new TotalHits(0, Relation.EQUAL_TO), new ScoreDoc[0]);
				}

				@Override
				public Document getDoc(int doc) throws IOException {
					throw new IllegalStateException();
				}
			};
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public IScorer newScorer(AbstractPathComputation pathComputation, String modelType) {
		if (storageKind == StorageKind.HBASE) {
			HBaseScorerFinal hsf = new HBaseScorerFinal(pathComputation, modelType);			
			return hsf;
		} else if (storageKind == StorageKind.SQLITE) {
			Path sqliteIndex    = getSqliteIndexDB(modelType);
			// TODO: How to close this??
			SqliteIndexDatabase db = new SqliteIndexDatabase(sqliteIndex.toFile());
			return new SqliteScorer(pathComputation, db);
		} else {
			Path sqliteIndex    = getJVectorIndexDB(modelType);
			Path jvectorVectors = getJVectorVectorDB(modelType);
			File vectorsFile = this.configuration.getEmbedding("glove");
			
			try {
				GloveWordE strategy = new EmbeddingStrategy.GloveWordE(vectorsFile);
				return new JVectorScorer(jvectorVectors, sqliteIndex, strategy);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	@Override
	public ModelDataAccessor getModelAccessor(String modelType) {
		if (storageKind == StorageKind.HBASE) {
			return new HBaseGetInfo();
		} else if (storageKind == StorageKind.SQLITE) {
			// We use here the jvector database because they are compatible
			// TODO: Generate a similar file but in the SQLite folder
			Path dbFile = getJVectorIndexDB(modelType);			
			return new SQLiteGetInfo(dbFile);
		} else {
			Path dbFile = getJVectorIndexDB(modelType);			
			return new SQLiteGetInfo(dbFile);
		}
	}
	
	@Override
	public IndexJobConfigurationData getIndexJobConfiguration() {
		return configuration;
	}

	@Override
	public String getModelFile(String id) {
		return modelFilesById.get(id);
	}
	
	public MarChatBotConfiguration getChatBotConfiguration(String modelType) {
		MarChatBotConfiguration conf = configurationCacheCB.get(modelType);
		if (conf == null) {
			conf = MarChatBotConfiguration.getHbaseConfiguration(modelType);
			configurationCacheCB.put(modelType, conf);
		}
		return conf;
	}

	private Path getJVectorIndexDB(String modelType) {
		File dbFolder = new File(EnvironmentVariables.getVariable(MAR.INDEX_TARGET));
		Path dbFile = Paths.get(dbFolder.getAbsolutePath(), "jvector", modelType + ".info");
		Preconditions.checkState(Files.exists(dbFile));
		return dbFile;
	}

	private Path getJVectorVectorDB(String modelType) {
		File dbFolder = new File(EnvironmentVariables.getVariable(MAR.INDEX_TARGET));
		Path dbFile = Paths.get(dbFolder.getAbsolutePath(), "jvector", modelType + ".jvector");
		Preconditions.checkState(Files.exists(dbFile));
		return dbFile;
	}

	private Path getSqliteIndexDB(String modelType) {
		File dbFolder = new File(EnvironmentVariables.getVariable(MAR.INDEX_TARGET));
		Path dbFile = Paths.get(dbFolder.getAbsolutePath(), "sqlite", modelType + ".db");
		Preconditions.checkState(Files.exists(dbFile));
		return dbFile;
	}
	
	private static enum StorageKind {
		HBASE,
		SQLITE, 
		JVECTOR
	}
	
}
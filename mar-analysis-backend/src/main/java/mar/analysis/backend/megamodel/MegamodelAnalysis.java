package mar.analysis.backend.megamodel;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Function;

import javax.annotation.Nonnull;

import mar.analysis.backend.RepositoryDB;
import mar.analysis.backend.RepositoryDB.RepoFile;
import mar.analysis.backend.megamodel.XtextAnalysisDB.XtextModel;
import mar.analysis.ecore.EcoreRepository;
import mar.analysis.ecore.EcoreRepository.EcoreDerivedModel;
import mar.analysis.ecore.EcoreRepository.EcoreModel;
import mar.analysis.ecore.SingleEcoreFileAnalyser;
import mar.analysis.megamodel.model.Artefact;
import mar.analysis.megamodel.model.Relationship;
import mar.analysis.megamodel.model.RelationshipsGraph;
import mar.analysis.megamodel.model.RelationshipsGraph.Node;
import mar.analysis.uml.UMLAnalyser;
import mar.artefacts.FileProgram;
import mar.artefacts.Metamodel;
import mar.artefacts.MetamodelReference;
import mar.artefacts.Transformation;
import mar.artefacts.epsilon.BuildFileInspector;
import mar.artefacts.graph.RecoveryGraph;
import mar.artefacts.qvto.QvtoInspector;
import mar.artefacts.qvto.QvtoProcessor;
import mar.ingestion.CrawlerDB;
import mar.ingestion.IngestedModel;
import mar.validation.AnalysisDB;
import mar.validation.AnalysisDB.Model;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "analyser", mixinStandardHelpOptions = true, description = "Generates a mega-model from different sources")
public class MegamodelAnalysis implements Callable<Integer> {

	@Parameters(index = "0", description = "Repository folder.")
	private File rootFolder;
	@Parameters(index = "1", description = "Output file.")
	private File output;
	
	private List<RecoveryGraph> computeMiniGraphs(Path repositoryDataFolder) {
		try(RepositoryDB db = new RepositoryDB(repositoryDataFolder, Paths.get(rootFolder.getAbsolutePath(), "analysis", "repo.db").toFile())) {		
			List<RecoveryGraph> result = new ArrayList<>();
			result.addAll( fromBuildFiles(db, repositoryDataFolder) );
			result.addAll(  fromQvtoFiles(db, repositoryDataFolder) );
			result.addAll(  fromXtextFiles(db, repositoryDataFolder) );
			
			return result;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private Collection<? extends RecoveryGraph> fromQvtoFiles(RepositoryDB db, Path repositoryDataFolder) throws SQLException {
		List<RecoveryGraph> result = new ArrayList<>();
		for (RepoFile model : db.getFilesByType("qvto")) {
			Path path = model.getRelativePath();
			Path projectPath = model.getProjectPath();
						
			System.out.println("Analysing QVTO: " + path);
			
			try {
				QvtoInspector inspector = new QvtoInspector(repositoryDataFolder, projectPath);
				RecoveryGraph minigraph = inspector.process(model.getFullPath().toFile());
				result.add(minigraph);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	@Nonnull
	private List<RecoveryGraph> fromBuildFiles(RepositoryDB db, Path repositoryDataFolder) throws SQLException {
		List<RecoveryGraph> result = new ArrayList<>();
		for (RepoFile model : db.getFilesByType("ant")) {
			Path path = model.getRelativePath();
			Path projectPath = model.getProjectPath();

			System.out.println("Analysing ANT: " + path);			
			
			try {
				BuildFileInspector inspector = new BuildFileInspector(repositoryDataFolder, projectPath);
				RecoveryGraph miniGraph = inspector.process(model.getFullPath().toFile());
				result.add(miniGraph);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	@Nonnull
	private List<RecoveryGraph> fromXtextFiles(RepositoryDB db, Path repositoryDataFolder) throws SQLException {
		List<RecoveryGraph> result = new ArrayList<>();
		for (RepoFile model : db.getFilesByType("xtext")) {
			Path path = model.getRelativePath();
			Path projectPath = model.getProjectPath();

			System.out.println("Analysing Xtext: " + path);			
			
			try {
				XtextInspector inspector = new XtextInspector(repositoryDataFolder, projectPath);
				RecoveryGraph miniGraph = inspector.process(model.getFullPath().toFile());
				result.add(miniGraph);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	private RelationshipsGraph mergeMiniGraphs(@Nonnull Collection<RecoveryGraph> graphs, File repositoryDataFolder, AnalysisDB metamodels) {
		RelationshipsGraph graph = new RelationshipsGraph();
		for (RecoveryGraph miniGraph : graphs) {		
			try {
				for (Metamodel metamodel : miniGraph.getMetamodels()) {
					String id = toId(metamodel, metamodels);
					String name = toName(metamodel);
					System.out.println("Adding id: " + id);
					Node node = new RelationshipsGraph.Node(id, new Artefact(id, "metamodel", name), metamodel);
					graph.addNode(node);							
				}
				
				for (Metamodel metamodel : miniGraph.getMetamodels()) {
					String id = toId(metamodel, metamodels);
					for (Metamodel dep : metamodel.getDependents()) {
						String depId = toId(dep, metamodels);
						graph.addEdge(id, depId, Relationship.IMPORT);
					}
				}
				
				for (FileProgram p : miniGraph.getPrograms()) {
					String id = toId(p);
					String name = toName(p);
					
					Node node = new RelationshipsGraph.Node(id, new Artefact(id, "transformation", name), p);
					graph.addNode(node);
					
					for (MetamodelReference ref : p.getMetamodels()) {
						Metamodel metamodel = ref.getMetamodel();
						String metamodelId = toId(metamodel, metamodels);
						System.out.println("Edge: " + id + ", " + metamodelId);
						
						// TODO: Analyse metamodel.getKind() to establish proper edge relationships
						graph.addEdge(id, metamodelId, Relationship.TYPED_BY);						
					}
					// p.getMetamodels()
				}
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return graph;
	}
	
	@Override
	public Integer call() throws Exception {
		new SingleEcoreFileAnalyser.Factory().configureEnvironment();
		new UMLAnalyser.Factory().configureEnvironment();
		
		File repositoryDataFolder = Paths.get(rootFolder.getAbsolutePath(), "repos").toFile();
		File ecoreAnalysisDbFile  = Paths.get(rootFolder.getAbsolutePath(), "analysis", "ecore" , "analysis.db").toFile();

		// Detailed information about all the meta-models in the repositories
		AnalysisDB analysisDb = new AnalysisDB(ecoreAnalysisDbFile);
		
		List<RecoveryGraph> miniGraphs = computeMiniGraphs(repositoryDataFolder.toPath());		
		RelationshipsGraph graph       = mergeMiniGraphs(miniGraphs, repositoryDataFolder, analysisDb);

		/*
		File repositoryDataFolder = Paths.get(rootFolder.getAbsolutePath(), "repos").toFile();
		File buildCrawlerDbFile  = Paths.get(rootFolder.getAbsolutePath(), "analysis", "build" , "crawler.db").toFile();
		File ecoreAnalysisDbFile  = Paths.get(rootFolder.getAbsolutePath(), "analysis", "ecore" , "analysis.db").toFile();
		File xtextAnalysisDbFile  = Paths.get(rootFolder.getAbsolutePath(), "analysis", "xtext" , "analysis.db").toFile();

		AnalysisDB analysisDb = new AnalysisDB(ecoreAnalysisDbFile);

		CrawlerDB buildFileCrawler = new CrawlerDB("build", "github", repositoryDataFolder.getAbsolutePath(), buildCrawlerDbFile);
		RelationshipsGraph graph = mergeMinigraphs(repositoryDataFolder, analysisDb, buildFileCrawler);
		*/
		
		if (true) {
			if (output.exists())
				output.delete();
			MegamodelDB megamodelDB = new MegamodelDB(output);
			megamodelDB.setAutocommit(false);
			megamodelDB.dump(graph);
			megamodelDB.close();
			return 0;
		}
		
		File qvtCrawlerDbFile  = Paths.get(rootFolder.getAbsolutePath(), "analysis", "qvto" , "crawler.db").toFile();
		File xtextAnalysisDbFile  = Paths.get(rootFolder.getAbsolutePath(), "analysis", "xtext" , "analysis.db").toFile();

		// AnalysisDB analysisDb = new AnalysisDB(new File("/home/jesus/projects/mde-ml/mar/.output/repo-github-ecore/analysis.db"));
		XtextAnalysisDB xtextAnalysisDb = new XtextAnalysisDB(xtextAnalysisDbFile);
		
		EcoreRepository repo  = new EcoreRepository(analysisDb, repositoryDataFolder);
		QvtoProcessor processor = new QvtoProcessor(repo);
				
		MegamodelDB megamodelDB = new MegamodelDB(output);
		megamodelDB.setAutocommit(false);
		
		String root = "/home/jesus/projects/mde-ml/mde-datasets/download/repo-github-qvto";

		for (Model model : repo.getModels()) {
			megamodelDB.addArtefact(model.getId(), "metamodel", model.getFile().getName());
		}		
		
		Function<String, String> xtextFileMapper = (relative) -> repositoryDataFolder.getAbsolutePath() + File.separator + relative;
		for (Model m : xtextAnalysisDb.getValidModels(xtextFileMapper)) {
			XtextModel xtext = (XtextModel) m;
			megamodelDB.addArtefact(xtext.getId(), "grammar", xtext.getFile().getName());
			
			if (xtext.getGeneratedMetamodels().isEmpty()) {
				XtextEcoreDerivedModel derived = new XtextEcoreDerivedModel(xtext.getId(), xtext.getFile().getName());
				xtext.getGeneratedMetamodels().forEach(uri -> {
					repo.addDerived(uri, derived);
					
					// TODO: Add atributes to the node, like "non-materialized"
					String generatedMetamodelId = xtext.getId() + "#" + uri;
					megamodelDB.addArtefact(generatedMetamodelId, "metamodel", xtext.getFile().getName() + "#generated-metamodel");
					
					megamodelDB.addRelationship(xtext.getId(), generatedMetamodelId, Relationship.TYPED_BY);
				});				
			}
			
			// This may need to go into another loop 
			xtext.getImportedMetamodels().forEach(uri -> {
				List<EcoreModel> pointedModels = repo.findEcoreByURI(uri);
				if (! pointedModels.isEmpty()) {
					// TODO: Get the proper model somehow
					EcoreModel pm = pointedModels.get(0);
					megamodelDB.addRelationship(xtext.getId(), pm.getId(), Relationship.TYPED_BY);
				}
			});			
		}
		
		CrawlerDB crawler = new CrawlerDB("qvto", "github", root, qvtCrawlerDbFile);
		for (IngestedModel m : crawler.getModels()) {
			Transformation transformation = processor.load(m.getAbsolutePath());
			// /home/jesus/projects/mde-ml/mde-datasets/download/repo-github-qvto/data/PhilippGruber/ScalingPolicyProfile/org.palladiosimulator.parallelcatalogue.catalog/completions/ScalingPolicy.qvto
			if (! transformation.hasErrors()) {
				System.out.println("Adding " + m.getAbsolutePath());
				String id = m.getModelId();
								
				megamodelDB.addArtefact(id, "transformation", m.getRelativeFile().getName());
				
				for (EcoreModel mm : transformation.getMetamodels()) {
					megamodelDB.addArtefact(mm.getId(), "metamodel", mm.getName());
					megamodelDB.addRelationship(id, mm.getId(), Relationship.TYPED_BY);
					System.out.println("  - " + id + " => " + mm.getId());
				}
			}			
		}
		
		xtextAnalysisDb.close();
		megamodelDB.close();
		
		return 0;
	}

	private RelationshipsGraph mergeMinigraphs(File repositoryDataFolder, AnalysisDB metamodels, CrawlerDB crawler) {
		RelationshipsGraph graph = new RelationshipsGraph();
		
		for (IngestedModel model : crawler.getModels()) {
			Path path = Paths.get(model.getRelativePath()).normalize();
			Path projectPath = path.subpath(0, 2);

			System.out.println("Doing: " + path);			
			
			BuildFileInspector inspector = new BuildFileInspector(repositoryDataFolder.toPath(), projectPath);
			
			try {
				RecoveryGraph miniGraph = inspector.process(model.getFullFile());
				for (Metamodel metamodel : miniGraph.getMetamodels()) {
					String id = toId(metamodel, metamodels);
					String name = toName(metamodel);
					System.out.println("Adding id: " + id);
					Node node = new RelationshipsGraph.Node(id, new Artefact(id, "metamodel", name), metamodel);
					graph.addNode(node);							
				}
				
				for (Metamodel metamodel : miniGraph.getMetamodels()) {
					String id = toId(metamodel, metamodels);
					for (Metamodel dep : metamodel.getDependents()) {
						String depId = toId(dep, metamodels);
						graph.addEdge(id, depId, Relationship.IMPORT);
					}
				}
				
				for (FileProgram p : miniGraph.getPrograms()) {
					String id = toId(p);
					String name = toName(p);
					
					Node node = new RelationshipsGraph.Node(id, new Artefact(id, "transformation", name), p);
					graph.addNode(node);
					
					for (MetamodelReference ref : p.getMetamodels()) {
						String metamodelId = toId(ref.getMetamodel(), metamodels);
						System.out.println("Edge: " + id + ", " + metamodelId);
						graph.addEdge(id, metamodelId, Relationship.TYPED_BY);						
					}
					// p.getMetamodels()
				}
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
		
		return graph;
	}

	private String toName(FileProgram p) {
		return p.getFilePath().getPath().getFileName().toString();		
	}

	private String toName(Metamodel metamodel) {
		if (metamodel.getPath() != null)
			return metamodel.getPath().getPath().getFileName().toString();
		return metamodel.getUri();
	}

	private String toId(FileProgram p) {
		return p.getFilePath().getPath().toString();
	}

	private String toId(Metamodel metamodel, AnalysisDB metamodels) {
		if (metamodel.getPath() != null) {
			Path relativePath = metamodel.getPath().getPath();
			Model model = metamodels.getModelByPath(relativePath.toString(), "nsURI", (s) -> s /* TODO: Do this properly */);
			if (model != null) {
				return model.getKeyValueMetadata("nsURI");
			}			
			return metamodel.getPath().getPath().toString();
		}
		return metamodel.getUri();
	}

	public static void main(String[] args) {
		int exitCode = new CommandLine(new MegamodelAnalysis()).execute(args);
		System.exit(exitCode);
	}
	
	public static class XtextEcoreDerivedModel implements EcoreDerivedModel {

		private String id;
		private String name;

		public XtextEcoreDerivedModel(@Nonnull String id, @Nonnull String name) {
			this.id = id;
			this.name = name;
		}

		@Override
		public String getId() {
			return id;
		}

		@Override
		public String getName() {
			return name;
		}
		
	}
}

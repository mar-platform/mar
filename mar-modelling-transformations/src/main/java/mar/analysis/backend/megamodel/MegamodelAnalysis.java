package mar.analysis.backend.megamodel;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.emf.ecore.resource.Resource;

import mar.analysis.backend.megamodel.inspectors.InspectorLauncher;
import mar.analysis.duplicates.DuplicateComputation;
import mar.analysis.duplicates.DuplicateComputation.DuplicateFinderConfiguration;
import mar.analysis.duplicates.DuplicateConfiguration;
import mar.analysis.duplicates.DuplicateFinder;
import mar.analysis.duplicates.EcoreDuplicateFinder;
import mar.analysis.ecore.SingleEcoreFileAnalyser;
import mar.analysis.megamodel.model.Artefact;
import mar.analysis.megamodel.model.Relationship;
import mar.analysis.megamodel.model.RelationshipsGraph;
import mar.analysis.megamodel.model.RelationshipsGraph.Node;
import mar.analysis.uml.UMLAnalyser;
import mar.artefacts.FileProgram;
import mar.artefacts.Metamodel;
import mar.artefacts.MetamodelReference;
import mar.artefacts.db.RepositoryDB;
import mar.artefacts.graph.RecoveryGraph;
import mar.artefacts.graph.RecoveryStats;
import mar.artefacts.graph.RecoveryStats.Composite;
import mar.indexer.common.configuration.ModelLoader;
import mar.validation.AnalysisDB;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "analyser", mixinStandardHelpOptions = true, description = "Generates a mega-model from different sources")
public class MegamodelAnalysis implements Callable<Integer> {

	@Parameters(index = "0", description = "Repository folder.")
	private File rootFolder;
	@Parameters(index = "1", description = "Output file.")
	private File output;
	
	private Map<ArtefactType, Collection<RecoveryGraph>> computeMiniGraphs(Path repositoryDataFolder) {
		try(RepositoryDB db = new RepositoryDB(repositoryDataFolder, Paths.get(rootFolder.getAbsolutePath(), "analysis", "repo.db").toFile())) {
			InspectorLauncher inspector = new InspectorLauncher(db, repositoryDataFolder);
			
			Map<ArtefactType, Collection<RecoveryGraph>> result = new HashMap<>();
			result.put(ArtefactType.ANT, inspector.fromBuildFiles() );
			result.put(ArtefactType.LAUNCH, inspector.fromLaunchFiles() );
			result.put(ArtefactType.QVTO, inspector.fromQvtoFiles() );
			result.put(ArtefactType.XTEXT, inspector.fromXtextFiles() );
			result.put(ArtefactType.EMFATIC, inspector.fromEmfaticFiles() );
			result.put(ArtefactType.ACCELEO, inspector.fromAcceleoFiles() );
			result.put(ArtefactType.ATL, inspector.fromATLFiles());
			result.put(ArtefactType.SIRIUS, inspector.fromSirius());
			
			return result;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void computeDuplicates(@Nonnull Map<ArtefactType, Collection<RecoveryGraph>> miniGraphs, @Nonnull RelationshipsGraph graph, @Nonnull Path repositoryDataFolder) {
		DuplicateConfiguration configuration = new DuplicateConfiguration(repositoryDataFolder, MegamodelAnalysis.this::toId, MegamodelAnalysis.this::toName);		
		DuplicateComputation computation = configuration.newComputation(miniGraphs, graph);
		computation.setMetamodelConfiguration(new DuplicateFinderConfiguration<Metamodel, Resource>() {
			@Override
			public Resource toResource(Metamodel p) throws Exception {
				if (p.getPath() == null)
					throw new UnsupportedOperationException("Ecore duplication for URIs not supported: " + p.getUri());

				return ModelLoader.DEFAULT.load(p.getPath().getCompletePath(repositoryDataFolder).toFile());
			}

			@Override
			public DuplicateFinder<Metamodel, Resource> toFinder() {
				return new EcoreDuplicateFinder();
			}

			@Override
			public String toId(Metamodel p) {
				return MegamodelAnalysis.this.toId(p);
			}			
			
			@Override
			public String toName(Metamodel p) {
				return MegamodelAnalysis.this.toName(p);
			}
		});				
		
		computation.updateGraph();
	}

	private Pair<RelationshipsGraph, RecoveryStats.Composite> mergeMiniGraphs(@Nonnull Map<ArtefactType, Collection<RecoveryGraph>> miniGraphs, File repositoryDataFolder, AnalysisDB metamodels) {
		RelationshipsGraph graph = new RelationshipsGraph();
		RecoveryStats.Composite stats = new RecoveryStats.Composite();
		
		for (ArtefactType type : miniGraphs.keySet()) {
			for (RecoveryGraph miniGraph : miniGraphs.get(type)) {				
				graph.addProject(miniGraph.getProject());
				try {
					for (Metamodel metamodel : miniGraph.getMetamodels()) {
						String id = toId(metamodel); /* , metamodels); */
						String name = toName(metamodel);
						System.out.println("Adding id: " + id);
						Node node = new RelationshipsGraph.ArtefactNode(id, new Artefact(miniGraph.getProject(), id, "ecore", "metamodel", name));
						graph.addNode(node);							
					}
					
					for (Metamodel metamodel : miniGraph.getMetamodels()) {
						String id = toId(metamodel); /* , metamodels); */
						for (Metamodel dep : metamodel.getDependents()) {
							String depId = toId(dep); /* , metamodels); */
							graph.addEdge(id, depId, Relationship.IMPORT);
						}
					}
					
					for (FileProgram p : miniGraph.getPrograms()) {
						String id = toId(p);
						String name = toName(p);
						
						Node node = new RelationshipsGraph.ArtefactNode(id, new Artefact(miniGraph.getProject(), id, p.getKind(), p.getCategory(), name));
						graph.addNode(node);
						
						for (MetamodelReference ref : p.getMetamodels()) {
							Metamodel metamodel = ref.getMetamodel();
							String metamodelId = toId(metamodel); /* , metamodels); */
							System.out.println("Edge: " + id + " -> " + metamodelId);
							
							// TODO: Analyse metamodel.getKind() to establish proper edge relationships
							graph.addEdge(id, metamodelId, Relationship.TYPED_BY);						
						}
					}
					
					if (miniGraph.getStats() != null)
						stats.addStats(miniGraph.getStats());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
				
		return Pair.of(graph, stats);
	}
	
	@Override
	public Integer call() throws Exception {
		new SingleEcoreFileAnalyser.Factory().configureEnvironment();
		new UMLAnalyser.Factory().configureEnvironment();
		
		File repositoryDataFolder = Paths.get(rootFolder.getAbsolutePath(), "repos").toFile();
		File ecoreAnalysisDbFile  = Paths.get(rootFolder.getAbsolutePath(), "analysis", "ecore" , "analysis.db").toFile();

		// Detailed information about all the meta-models in the repositories
		AnalysisDB analysisDb = new AnalysisDB(ecoreAnalysisDbFile);
		analysisDb.setReadOnly(true);
		
		Map<ArtefactType, Collection<RecoveryGraph>> miniGraphs  = computeMiniGraphs(repositoryDataFolder.toPath());		
		Pair<RelationshipsGraph, RecoveryStats.Composite> result = mergeMiniGraphs(miniGraphs, repositoryDataFolder, analysisDb);
		
		Composite stats = result.getRight();
		RelationshipsGraph graph = result.getLeft();
		
		computeDuplicates(miniGraphs, graph, repositoryDataFolder.toPath());
		
		stats.detailedReport();
		
		if (output.exists())
			output.delete();
		
		MegamodelDB megamodelDB = new MegamodelDB(output);
		megamodelDB.setAutocommit(false);
		megamodelDB.dump(graph, stats);
		megamodelDB.close();
		
		return 0;
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

	private String toId(Metamodel metamodel) {  /*, AnalysisDB metamodels) */
		if (metamodel.getPath() != null) {
			Path relativePath = metamodel.getPath().getPath();
			return relativePath.toString();
			
// This is an attempt to merge similar meta-models by URI, but there are downsides (different meta-models with the same URI; e.g., MyDSL).
// The current approach is to use duplication groups
//			Model model = metamodels.getModelByPath(relativePath.toString(), (s) -> s /* TODO: Do this properly */);
//			if (model != null) {
//				return model.getKeyValueMetadata("nsURI");
//			}			
//			return metamodel.getPath().getPath().toString();
		}
		return metamodel.getUri();
	}

	public static void main(String[] args) {
		int exitCode = new CommandLine(new MegamodelAnalysis()).execute(args);
		System.exit(exitCode);
	}
	
}

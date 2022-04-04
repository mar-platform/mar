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

import anatlyzer.atl.model.ATLModel;
import anatlyzer.atl.tests.api.AtlLoader;
import anatlyzer.atl.tests.api.AtlLoader.LoadException;
import mar.analysis.backend.RepositoryDB;
import mar.analysis.backend.megamodel.inspectors.InspectorLauncher;
import mar.analysis.duplicates.ATLDuplicateFinder;
import mar.analysis.duplicates.DuplicateFinder.DuplicationGroup;
import mar.analysis.ecore.SingleEcoreFileAnalyser;
import mar.analysis.megamodel.model.Artefact;
import mar.analysis.megamodel.model.Relationship;
import mar.analysis.megamodel.model.RelationshipsGraph;
import mar.analysis.megamodel.model.RelationshipsGraph.Node;
import mar.analysis.uml.UMLAnalyser;
import mar.artefacts.FileProgram;
import mar.artefacts.Metamodel;
import mar.artefacts.MetamodelReference;
import mar.artefacts.graph.RecoveryGraph;
import mar.artefacts.graph.RecoveryStats;
import mar.artefacts.graph.RecoveryStats.Composite;
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
	
	private Map<String, Collection<RecoveryGraph>> computeMiniGraphs(Path repositoryDataFolder) {
		try(RepositoryDB db = new RepositoryDB(repositoryDataFolder, Paths.get(rootFolder.getAbsolutePath(), "analysis", "repo.db").toFile())) {
			InspectorLauncher inspector = new InspectorLauncher(db, repositoryDataFolder);
			
			Map<String, Collection<RecoveryGraph>> result = new HashMap<>();
			result.put("ant", inspector.fromBuildFiles() );
			result.put("launch", inspector.fromLaunchFiles() );
			result.put("qvto", inspector.fromQvtoFiles() );
			result.put("xtext", inspector.fromXtextFiles() );
			result.put("emfatic", inspector.fromEmfaticFiles() );
			result.put("acceleo", inspector.fromAcceleoFiles() );
			result.put("atl", inspector.fromATLFiles());
			
			return result;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private void computeDuplicates(Path repositoryDataFolder, Collection<? extends RecoveryGraph> graphs, RelationshipsGraph completeGraph) {
		ATLDuplicateFinder finder = new ATLDuplicateFinder();
		Map<ATLModel, FileProgram> programs = new HashMap<>();
		for (RecoveryGraph graph : graphs) {			
			for (FileProgram p : graph.getPrograms()) {
				try {
					Resource r = AtlLoader.load(p.getFilePath().getCompletePath(repositoryDataFolder).toString());
					ATLModel model = new ATLModel(r, p.getFilePath().getPath().toString());
					finder.addResource(model);
					programs.put(model, p);
				} catch (LoadException e) {
					e.printStackTrace();
				}
			}
		}
				
		Collection<DuplicationGroup<ATLModel>> duplicates = finder.getDuplicates(0.8, 0.7);
		for (DuplicationGroup<ATLModel> group : duplicates) {
			for (ATLModel model1 : group) {
				FileProgram p1 = programs.get(model1);
				for (ATLModel model2 : group) {
					FileProgram p2 = programs.get(model2);
					if (p1 != p2) {
						String id1 = toId(p1);
						String id2 = toId(p2);
						completeGraph.addEdge(id1, id2, Relationship.DUPLICATE);
					}					
				}	
			}
		}
		
		// This could introduce a special node "Duplication node" which act as the group that links everything, instead of all-to-all edges
	}

	private Pair<RelationshipsGraph, RecoveryStats.Composite> mergeMiniGraphs(@Nonnull Map<String, Collection<RecoveryGraph>> miniGraphs, File repositoryDataFolder, AnalysisDB metamodels) {
		RelationshipsGraph graph = new RelationshipsGraph();
		RecoveryStats.Composite stats = new RecoveryStats.Composite();
		
		for (String type : miniGraphs.keySet()) {
			for (RecoveryGraph miniGraph : miniGraphs.get(type)) {				
				try {
					for (Metamodel metamodel : miniGraph.getMetamodels()) {
						String id = toId(metamodel, metamodels);
						String name = toName(metamodel);
						System.out.println("Adding id: " + id);
						Node node = new RelationshipsGraph.Node(id, new Artefact(id, "ecore", "metamodel", name), metamodel);
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
						
						Node node = new RelationshipsGraph.Node(id, new Artefact(id, p.getKind(), p.getCategory(), name), p);
						graph.addNode(node);
						
						for (MetamodelReference ref : p.getMetamodels()) {
							Metamodel metamodel = ref.getMetamodel();
							String metamodelId = toId(metamodel, metamodels);
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
			
			if (type.equals("atl")) {			
				computeDuplicates(repositoryDataFolder.toPath(), miniGraphs.get(type), graph);
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
		
		Map<String, Collection<RecoveryGraph>> miniGraphs = computeMiniGraphs(repositoryDataFolder.toPath());		
		Pair<RelationshipsGraph, RecoveryStats.Composite> result = mergeMiniGraphs(miniGraphs, repositoryDataFolder, analysisDb);
		Composite stats = result.getRight();
		
		stats.detailedReport();
		
		if (output.exists())
			output.delete();
		
		MegamodelDB megamodelDB = new MegamodelDB(output);
		megamodelDB.setAutocommit(false);
		megamodelDB.dump(result.getLeft(), stats);
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

	private String toId(Metamodel metamodel, AnalysisDB metamodels) {
		if (metamodel.getPath() != null) {
			Path relativePath = metamodel.getPath().getPath();
			Model model = metamodels.getModelByPath(relativePath.toString(), (s) -> s /* TODO: Do this properly */);
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
	
}

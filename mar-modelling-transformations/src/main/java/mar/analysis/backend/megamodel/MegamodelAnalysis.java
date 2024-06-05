package mar.analysis.backend.megamodel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.emf.ecore.resource.Resource;

import com.google.common.io.Files;

import mar.analysis.backend.megamodel.AnalyserConfiguration.ContentFilter;
import mar.analysis.backend.megamodel.inspectors.InspectorLauncher;
import mar.analysis.backend.megamodel.inspectors.InspectorLauncher.InspectorResult;
import mar.analysis.backend.megamodel.stats.ResultAnalyser;
import mar.analysis.duplicates.DuplicateComputation;
import mar.analysis.duplicates.DuplicateComputation.DuplicateFinderConfiguration;
import mar.analysis.duplicates.DuplicateConfiguration;
import mar.analysis.duplicates.DuplicateFinder;
import mar.analysis.duplicates.DuplicateFinder.DuplicationGroup;
import mar.analysis.duplicates.DuplicationAnalysisResult;
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
import mar.artefacts.RecoveredPath;
import mar.artefacts.db.RepositoryDB;
import mar.artefacts.graph.RecoveryGraph;
import mar.artefacts.graph.RecoveryStats;
import mar.artefacts.graph.RecoveryStats.Composite;
import mar.indexer.common.configuration.ModelLoader;
import mar.validation.AnalyserRegistry;
import mar.validation.AnalysisDB;
import mar.validation.IFileInfo;
import mar.validation.IFileProvider;
import mar.validation.ISingleFileAnalyser;
import mar.validation.ISingleFileAnalyser.Remote;
import mar.validation.ResourceAnalyser;
import mar.validation.ResourceAnalyser.Factory;
import mar.validation.ResourceAnalyser.OptionMap;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "analyser", mixinStandardHelpOptions = true, description = "Generates a mega-model from different sources")
public class MegamodelAnalysis implements Callable<Integer> {

	@Parameters(index = "0", description = "Repository folder.")
	private File rootFolder;
	@Parameters(index = "1", description = "Output file.")
	private File output;
	@Option(required = false, names = { "--configuration" }, description = "Configuration files")
	private File configurationFile;
	@Option(required = false, names = { "--analysis-ecore" }, description = "Force analysis of Ecore")
	private boolean analysisEcore;
	@Option(required = false, names = { "--project" }, description = "Project to be analysis")
	private String project;
	@Option(required = false, names = { "--git-version" }, description = "Show information about the version")
	private boolean showGitInfo;
	@Option(required = false, names = { "--apply-preprocessing" }, description = "Apply pre-processing filters.")
	private boolean applyFilters;
	@Option(required = false, names = { "--parallel" }, description = "Execute in parallel with X threads")
	private int parallel = -1;
	@CheckForNull
	private AnalyserConfiguration configuration;
	
	private Map<ArtefactType, InspectorResult> computeMiniGraphs(Path repositoryDataFolder, AnalysisDB analysisDb) {
		try(RepositoryDB db = openRepositoryDB(repositoryDataFolder)) {
			InspectorLauncher inspector = new InspectorLauncher(db, repositoryDataFolder, analysisDb);			
			if (project != null) {
				// This is typically for debugging only
				inspector.withFilter(f -> {
					return f.getProjectPath().toString().contains(project);
				});
			} else if (applyFilters){
				inspector.withFilter(f -> {
					AnalyserConfiguration conf = getConfiguration();
					String extension = f.getExtension();
					List<ContentFilter> filters = conf.getFilters(extension);
					if (filters.size() > 0) {
						try (FileReader fr = new FileReader(f.getFullPath().toFile())) {
							BufferedReader reader = new BufferedReader(fr);
							boolean matched = reader.lines().anyMatch(l -> {
								for (ContentFilter contentFilter : filters) {
									if (l.contains(contentFilter.getContains()))
										return true;
								}
								return false;
							});
							if (matched)
								return false;
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					}
					return !conf.isIgnored(f.getRelativePath());
				});
			}
			
			Map<ArtefactType, InspectorResult> result = inspector.execute(parallel);
			return result;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private DuplicationAnalysisResult computeDuplicates(@Nonnull Map<ArtefactType, Collection<RecoveryGraph>> miniGraphs, @Nonnull Path repositoryDataFolder) {
		DuplicateConfiguration configuration = new DuplicateConfiguration(repositoryDataFolder, MegamodelAnalysis.this::toId, MegamodelAnalysis.this::toName);		
		DuplicateComputation computation = configuration.newComputation(miniGraphs);
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
		
		return computation.run();
	}

	private Pair<RelationshipsGraph, RecoveryStats.Composite> mergeMiniGraphs(@Nonnull Map<ArtefactType, Collection<RecoveryGraph>> miniGraphs, DuplicationAnalysisResult duplicates, File repositoryDataFolder, AnalysisDB metamodels) {
		RelationshipsGraph graph = new RelationshipsGraph();
		RecoveryStats.Composite stats = new RecoveryStats.Composite();
		
		List<FileProgram> withImports = new ArrayList<>();
		
		for (ArtefactType type : miniGraphs.keySet()) {
			for (RecoveryGraph miniGraph : miniGraphs.get(type)) {				
				graph.addProject(miniGraph.getProject());
				try {
					for (Metamodel metamodel : miniGraph.getMetamodels()) {
						String id = toId(metamodel); /* , metamodels); */
						String name = toName(metamodel);
						String status = toRecoveryStatus(metamodel.getPath());
						
						System.out.println("Adding id: " + id);
						Node node = new RelationshipsGraph.ArtefactNode(id, new Artefact(miniGraph.getProject(), id, "ecore", "metamodel", name, status));
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
						String status = toRecoveryStatus(p.getFilePath());
						
						// It may happen that the same node is recovered with two different methods (e.g., EpsilonInspector and LaunchInspector)
						// This perform the merge.
						Node node;
						if (graph.hasNode(id)) {
							node = graph.getNode(id);
						} else {
							node = new RelationshipsGraph.ArtefactNode(id, new Artefact(miniGraph.getProject(), id, p.getKind(), p.getCategory(), name, status));
							graph.addNode(node);
						}
						
						for (MetamodelReference ref : p.getMetamodels()) {
							Metamodel metamodel = ref.getMetamodel();
							String metamodelId = toId(metamodel); /* , metamodels); */
							System.out.println("Edge: " + id + " -> " + metamodelId);
							
							addEdge(graph, id, metamodelId, ref);					
						}
						
						if (! p.getImportedPrograms().isEmpty()) {
							withImports.add(p);
						}
						
						extendLocalInformation(p, type, duplicates, graph);
					}
					
					if (miniGraph.getStats() != null)
						stats.addStats(miniGraph.getStats());
										
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		for (FileProgram fileProgram : withImports) {
			for (Path path : fileProgram.getImportedPrograms()) {
				String tgt = toId(path);
				if ( ! graph.hasNode(tgt) ) {
					// This happens for instance with:
					//  - src: adilinam/QVTo-QVTd-OCL/org.eclipse.m2m.tests.qvt.oml/parserTestData/sources/bug468303/bug468303.qvto
					//  - tgt: adilinam/QVTo-QVTd-OCL/org.eclipse.m2m.tests.qvt.oml/parserTestData/sources/bug468303/incompatible.qvto
					// Because the QVT inspector creates an imported program "incompatible.qvto" but at the same
					// time the QVT inspector (for some reason) fails to load "incompatible.qvto" and thus it is
					// never added to the graph.
					
					System.err.println("Target node not found: " + tgt);
					continue;
				}
				graph.addEdge(toId(fileProgram), toId(path), Relationship.IMPORT);
			}
		}
		
		return Pair.of(graph, stats);
	}
	
	private void extendLocalInformation(FileProgram fileProgram, ArtefactType type, DuplicationAnalysisResult duplicates, RelationshipsGraph graph) {
		if (! fileProgram.hasAttribute(RelationshipsGraph.IsInBuildFolderAttribute.INSTANCE))
			return;
		
		DuplicationGroup<FileProgram> dups = duplicates.getDuplicatesOf(type, fileProgram);
		if (dups == null)
			return;
		
		for (FileProgram other : dups) {
			if (other != fileProgram && other.getProjectPath().equals(fileProgram.getProjectPath()) && 
				!other.hasAttribute(RelationshipsGraph.IsInBuildFolderAttribute.INSTANCE)) {
				// We know that there is a duplicate of this file in the project so we assume that this is the original and this is just a copy
				graph.addEdge(toId(fileProgram), toId(other), Relationship.DUPLICATE);
			}
		}

	}

	private void addEdge(RelationshipsGraph graph, String id, String metamodelId, MetamodelReference ref) {
		if (ref.is(MetamodelReference.Kind.TYPED_BY))
			graph.addEdge(id, metamodelId, Relationship.TYPED_BY);						
		if (ref.is(MetamodelReference.Kind.INPUT_OF))
			graph.addEdge(metamodelId, id, Relationship.INPUT_TYPE);
		if (ref.is(MetamodelReference.Kind.OUTPUT_OF))
			graph.addEdge(id, metamodelId, Relationship.OUTPUT_TYPE);
	}

	@Override
	public Integer call() throws Exception {
		if (showGitInfo) {
			new ResultAnalyser(getConfiguration()).showProjectInformation();
			return 0;
		}
		
		new SingleEcoreFileAnalyser.Factory().configureEnvironment();
		new UMLAnalyser.Factory().configureEnvironment();
		
		File repositoryDataFolder = Paths.get(rootFolder.getAbsolutePath(), "repos").toFile();
		File ecoreAnalysisDbFile  = Paths.get(rootFolder.getAbsolutePath(), "analysis", "ecore" , "analysis.db").toFile();
		
		if (!analysisEcore && !ecoreAnalysisDbFile.exists()) {
			System.out.println("No analysis file. Run with --analysis-ecore");
			return -1;
		}
		
		if (analysisEcore) {
			Factory factory = AnalyserRegistry.INSTANCE.getFactory(mar.analysis.ecore.SingleEcoreFileAnalyser.ID);
			OptionMap options = new OptionMap();
			options.put(SingleEcoreFileAnalyser.EXTRACT_CLASSIFIER_FOOTPRINT_OPTION, "true");
			try (ISingleFileAnalyser.Remote singleAnalyser = (Remote) factory.newRemoteAnalyser(options);
				 RepositoryDB repoDB = openRepositoryDB(repositoryDataFolder.toPath())) {
				try (ResourceAnalyser analyser = new ResourceAnalyser(singleAnalyser, new RepositoryDBProvider(repoDB), ecoreAnalysisDbFile)) {
					analyser.withParallelThreads(4);
					analyser.check();
				}
			}
		}
		
		// Detailed information about all the meta-models in the repositories
		AnalysisDB analysisDb = new AnalysisDB(ecoreAnalysisDbFile);
		analysisDb.setReadOnly(true);

		// 1. Recover local information about the projects
		System.out.println("1. Recover local information. Mini-graphs.");
		Map<ArtefactType, InspectorResult> inspectionResults  = computeMiniGraphs(repositoryDataFolder.toPath(), analysisDb);
		Map<ArtefactType, Collection<RecoveryGraph>> miniGraphs = inspectionResults.keySet().stream().collect(Collectors.toMap(k -> k, k -> inspectionResults.get(k).getGraphs()));
		//Map<ArtefactType, Collection<RecoveryGraph>> miniGraphs  = computeMiniGraphs(repositoryDataFolder.toPath(), analysisDb);
		
		// 2. Perform global analysis (for the moment duplicate computation)
		System.out.println("2. Global analysis. Duplicate computation.");
		DuplicationAnalysisResult duplicates = computeDuplicates(miniGraphs, repositoryDataFolder.toPath());
		
		// 3. Perform local analysis (maybe using the global information)
		System.out.println("3. Local analysis.");
		localAnalysis(miniGraphs);
		
		// 4. Merge local information + local information
		System.out.println("4. Merge.");
		Pair<RelationshipsGraph, RecoveryStats.Composite> result = mergeMiniGraphs(miniGraphs, duplicates, repositoryDataFolder, analysisDb);
		
		// 5. Organize errors for dumping (this is just for statistics purposes)
		List<Error> allErrors = inspectionResults.values().stream().flatMap(i -> i.getErrors().stream()).
				map(e -> new Error(toId(e.getProgram()), "syntax", "-")).
				collect(Collectors.toList());
		
		List<Ignored> allIgnored = inspectionResults.values().stream().flatMap(i -> i.getIgnored().stream()).
			map(i -> {
				String id = toId(i.getPath());
				i.setId(id); // TODO: I hate this
				return i;
			}).collect(Collectors.toList());
			
		
		Composite stats = result.getRight();
		RelationshipsGraph graph = result.getLeft();
				
		if (output.exists())
			output.delete();
		
		MegamodelDB megamodelDB = new MegamodelDB(output);
		megamodelDB.setAutocommit(false);
		megamodelDB.dump(graph, stats, allErrors, allIgnored);
		duplicates.updateGraph(megamodelDB);
		megamodelDB.close();
		analysisDb.close();
		
		String extension = Files.getFileExtension(output.getPath());		
		File resultAnalysisFile = new File(output.getPath().replace("." + extension, "") + ".analysis.txt");
		File resultStatsFile = new File(output.getPath().replace("." + extension, "") + ".stats.json");
		
		new ResultAnalyser(getConfiguration()).
			withOutput(new PrintStream(resultAnalysisFile), resultStatsFile).
			run(getRepositoryDbFile(), output);

		return 0;
	}

	private AnalyserConfiguration getConfiguration() {
		if (configuration != null)
			return configuration;
		
		if (configurationFile == null) {
			configuration = new AnalyserConfiguration();
		} else {
			try {
				configuration = AnalyserConfiguration.read(configurationFile);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		
		return configuration;
	}

	// TODO: If there are more analysis, we can have them in a factory or something, for the moment they are hard-coded here
	private void localAnalysis(Map<ArtefactType, Collection<RecoveryGraph>> miniGraphs) {
		miniGraphs.forEach((type, graphs) -> {
			for (RecoveryGraph recoveryGraph : graphs) {
				for (FileProgram fileProgram : recoveryGraph.getPrograms()) {
					analysisBuildFolderPlacement(fileProgram, type);
				}
			}
		});
	}

	private void analysisBuildFolderPlacement(FileProgram fileProgram, ArtefactType type) {
		Predicate<FileProgram> isInBinaryFolder = f -> {
			String path = fileProgram.getFilePath().getPath().toString();
			return path.contains("/bin/") || path.contains("/target/classes/");
		};
		
		if (isInBinaryFolder.test(fileProgram)) {
			fileProgram.addAttribute(RelationshipsGraph.IsInBuildFolderAttribute.INSTANCE);
		}		
	}

	private String toName(FileProgram p) {
		return p.getFilePath().getPath().getFileName().toString();		
	}
	
	private String toRecoveryStatus(RecoveredPath p) {
		if (p == null)
			return "no-path";
		return p.toPathStatusId();
	}

	private String toName(Metamodel metamodel) {
		if (metamodel.getPath() != null)
			return metamodel.getPath().getPath().getFileName().toString();
		return metamodel.getUri();
	}

	private String toId(FileProgram p) {
		return toId(p.getFilePath().getPath());
	}

	private String toId(Path p) {
		return p.toString();
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

	private RepositoryDB openRepositoryDB(Path repositoryDataFolder) throws SQLException {
		return new RepositoryDB(repositoryDataFolder, getRepositoryDbFile());
	}

	private File getRepositoryDbFile() {
		return Paths.get(rootFolder.getAbsolutePath(), "analysis", "repo.db").toFile();
	}

	public static class RepositoryDBProvider implements IFileProvider {

		private RepositoryDB repoDB;

		public RepositoryDBProvider(RepositoryDB repoDB) {
			this.repoDB = repoDB;
		}
		
		@Override
		public List<? extends IFileInfo> getLocalFiles() {			
			try {
				return repoDB.getFilesByType("ecore").stream()
						.map(f -> new IFileInfo.FileInfo(f.getRootFolder().toFile(), f.getFullPath().toFile()))
						.collect(Collectors.toList());
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}
		
	}
	
	public static void main(String[] args) {
		int exitCode = new CommandLine(new MegamodelAnalysis()).execute(args);
		System.exit(exitCode);
	}
	
}

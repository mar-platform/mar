package mar.analysis.backend.megamodel.stats;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import org.jgrapht.Graph;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import mar.analysis.backend.megamodel.AnalyserConfiguration;
import mar.analysis.backend.megamodel.ArtefactType;
import mar.analysis.backend.megamodel.Error;
import mar.analysis.backend.megamodel.MegamodelDB;
import mar.analysis.backend.megamodel.RawRepositoryDB;
import mar.analysis.backend.megamodel.TransformationRelationshipsAnalysis;
import mar.analysis.backend.megamodel.RawRepositoryDB.RawFile;
import mar.analysis.megamodel.model.Artefact;
import mar.analysis.megamodel.model.RelationshipsGraph;
import mar.analysis.megamodel.model.RelationshipsGraph.ArtefactNode;
import mar.analysis.megamodel.model.RelationshipsGraph.Edge;
import mar.analysis.megamodel.model.RelationshipsGraph.Node;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * This program analysis the results of the Megamodel Analysis step by comparing
 * the original files (in the raw database) and the actual files that has been
 * recovered (in the mega-model database).
 * 
 * This is 
 * - Compute stats
 * - Identify concrete files which has not been recovered
 * 
 * @author jesus
 *
 */
@Command(name = "result-analyser", mixinStandardHelpOptions = true, description = "Analyses the results of the mega-model analysis.")
public class ResultAnalyser implements Callable<Integer> {

	@Parameters(index = "0", description = "Megamodel database")
	private File megamodelDbFile;
	@Parameters(index = "1", description = "Raw database")
	private File rawDbFile;
	@Option(required = false, names = { "--types" }, description = "File types to be analysis")
	private List<String> types;
	@Option(required = false, names = { "--configuration" }, description = "Configuration files")
	private File configurationFile;
	private AnalyserConfiguration configuration;

	private PrintStream out = System.out;
	
	public ResultAnalyser() {
		// For piccocli
	}
	
	public ResultAnalyser(AnalyserConfiguration configuration) {
		this.configuration = configuration;
	}

	public ResultAnalyser withOutput(PrintStream stream) {
		this.out = stream;
		return this;
	}
	
	@Override
	public Integer call() throws Exception {
		Set<String> artefactTypes;
		if (types == null) {
			artefactTypes = toAllTypes();
		} else {
			artefactTypes = new HashSet<>(types);
		}
		
		run(artefactTypes, rawDbFile, megamodelDbFile);
		
		return null;
	}

	private Set<String> toAllTypes() {
		Set<String> artefactTypes;
		artefactTypes = new HashSet<>();
		for (ArtefactType artefactType : ArtefactType.values()) {
			if (artefactType.isArtefactFile)
				artefactTypes.add(artefactType.id);
		}
		return artefactTypes;
	}

	public void run(File rawDbFile, File megamodelDbFile) throws SQLException, IOException {
		run(toAllTypes(), rawDbFile, megamodelDbFile);
	}
	
	public void run(Set<String> artefactTypes, File rawDbFile, File megamodelDbFile) throws SQLException, IOException {
		try (RawRepositoryDB rawDb = new RawRepositoryDB(rawDbFile);
			MegamodelDB megamodelDb = new MegamodelDB(megamodelDbFile)) {
			
			computeFileLevelStats(artefactTypes, rawDb, megamodelDb);
			computeGraphLevelStats(artefactTypes, megamodelDb);
			
			showProjectInformation();
		};
	}

	private void computeGraphLevelStats(Set<String> artefactTypes, MegamodelDB megamodelDb) {
		long totalArtefactNodes = 0;
		long totalIsolatedArtefacts = 0;
		long totalOutDegree = 0;
		long totalInDegree = 0;
		

		Multimap<String, Artefact> byType = MultimapBuilder.hashKeys().arrayListValues().build();		
		
		
		TransformationRelationshipsAnalysis analysis = new TransformationRelationshipsAnalysis(megamodelDb);
		RelationshipsGraph graph = analysis.getRelationships();
		for (Node node : graph.getNodes()) {
			if (node instanceof ArtefactNode) {
				Artefact artefact = ((ArtefactNode) node).getArtefact();
				if (! artefactTypes.contains(artefact.getType()))
					continue;
		
				totalArtefactNodes++;
				
				Graph<Node, Edge> impl = graph.getGraph();
				int outDegree = impl.outDegreeOf(node);
				int inDegree = impl.inDegreeOf(node);

				totalOutDegree += outDegree;
				totalInDegree += inDegree;
				if (inDegree == 0 && outDegree == 0) {
					totalIsolatedArtefacts++;
					byType.put(artefact.getType(), artefact);
				}
			}
		}
		
		out.println();
		out.println("Graph-level stats:");
		out.println("  " + String.format("%-8s", "# Artefacts") + " " + String.format("%d", totalArtefactNodes));
		out.println("  " + String.format("%-8s", "# Isolated") + " " + String.format("%d", totalIsolatedArtefacts));
		out.println("  " + String.format("%-8s", "% Isolated") + " " + String.format("%.2f%s", 100.0 * totalIsolatedArtefacts / totalArtefactNodes, "%"));
		out.println("  " + String.format("%-8s", "Avg. out-degree") + " " + String.format("%.2f", 1.0 * totalOutDegree / totalIsolatedArtefacts));
		out.println("  " + String.format("%-8s", "Avg. in-degree") + " " + String.format("%.2f", 1.0 * totalInDegree / totalIsolatedArtefacts));
	
		out.println();
		out.println("Isolated nodes:");
		byType.asMap().forEach((type, artefacts) -> {
			out.println("- Type: " + type + "  " + artefacts.size() + " isolated artefacts");
			artefacts.forEach(a -> {
				out.println("   " + a.getId());
			});
		});
	}

	private void computeFileLevelStats(Set<String> artefactTypes, RawRepositoryDB rawDb, MegamodelDB megamodelDb)
			throws SQLException {
		Multimap<String, RawFile> byType = compare(rawDb, megamodelDb, artefactTypes);

		out.println("Number of mismatches: " + byType.size());
		byType.asMap().forEach((type, values) -> {
			if (! values.isEmpty()) {
				out.println("Missing " + type);
				values.forEach(v -> {
					out.println("  - " + v.getFilepath());
				});
			}
		});
		
		out.println("\nStats:");
		RawRepositoryStats rawStats = rawDb.getStats();
		for(String type : artefactTypes) {
			Collection<RawFile> values = byType.get(type);
			
			long artefactsInRaw = rawStats.getCount(type);
			long artefactsInRawMatched = artefactsInRaw - values.size();
			// This is not totally precise because we are counting as matched syntax errors and elements ignored by configuration
			// We have to think if this is fully correct
			double v = 1.0 * artefactsInRawMatched / artefactsInRaw;
			out.println("  " + String.format("%-8s", type) + " " + String.format("%.2f", v));
		}
		
		/*
		CombinedStats stats = new CombinedStats(rawDb.getStats(), megamodelDb.getStats());
		stats.getArtefactRecoveryCompletion().forEach((k, v) -> {
			out.println("  " + String.format("%-8s", k) + " " + String.format("%.2f", v));
		});
		*/
	}
	
	/**
	 * For each type of artefact, it returns which files in the raw repository have no correspondence with an
	 * artefact processed in the mega-model.
	 */
	private Multimap<String, RawFile> compare(RawRepositoryDB rawDb, MegamodelDB megamodelDb, Set<String> artefactTypes) throws SQLException {
		Set<String> ignoredFiles = megamodelDb.getIgnoredFileIds();
		List<RawFile> files = rawDb.getFiles().stream().
				filter(p -> ! getConfiguration().isIgnored(Paths.get(p.getFilepath()))).
				filter(p -> ! ignoredFiles.contains(p.getFilepath())).
				collect(Collectors.toList());

		Map<? extends String, ? extends Artefact> artefacts = megamodelDb.getAllArtefacts();
		
		Map<String, Error> allErrorsById = megamodelDb.getErrors();
		
		Multimap<String, RawFile> byType = MultimapBuilder.hashKeys().arrayListValues().build();
		for (RawFile rawFile : files) {
			if (! artefactTypes.contains(rawFile.getType()))
				continue;
				
			String artefactId = rawFile.getFilepath();
			
			
			Artefact artefact = artefacts.get(artefactId);
			// If it is a missing artefact, let's make sure that it is not an erroring artefact
			if (artefact == null && !allErrorsById.containsKey(artefactId)) {
				//out.println("File " + rawFile.getFilepath() + " not found in MegamodelDB");
				byType.put(rawFile.getType(), rawFile);
				continue;
			} 			
		}

		return byType;
	}

	public void showProjectInformation() throws IOException {
		Properties props = new Properties();
	    try (InputStream stream = this.getClass().getResourceAsStream("/git.properties")) {
	    	if(stream == null)
	    		throw new IllegalStateException("No git.properties file found");
	      props.load(stream);
	    }

	    String commitId = props.getProperty("git.commit.id.abbrev");
	    String commitMessage = props.getProperty("git.commit.message.full");
	    String commitTime = props.getProperty("git.build.time");
	    String branch = props.getProperty("git.branch");
	    
	    
	    out.println("ProjectInfo: ");
	    out.println("  - Branch: " + branch);
	    out.println("  - Commit: " + commitId);
	    out.println("  - Message: " + commitMessage);
	    out.println("  - Time: " + commitTime);
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

	
	public static void main(String[] args) {
		int exitCode = new CommandLine(new ResultAnalyser()).execute(args);
		System.exit(exitCode);
	}
	
}

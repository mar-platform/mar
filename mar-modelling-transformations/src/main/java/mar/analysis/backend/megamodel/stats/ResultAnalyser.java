package mar.analysis.backend.megamodel.stats;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import mar.analysis.backend.megamodel.ArtefactType;
import mar.analysis.backend.megamodel.MegamodelDB;
import mar.analysis.backend.megamodel.RawRepositoryDB;
import mar.analysis.backend.megamodel.RawRepositoryDB.RawFile;
import mar.analysis.megamodel.model.Artefact;
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
			
			Multimap<String, RawFile> byType = compare(rawDb, megamodelDb, artefactTypes);

			System.out.println("Number of mismatches: " + byType.size());
			byType.asMap().forEach((type, values) -> {
				if (! values.isEmpty()) {
					System.out.println("Missing " + type);
					values.forEach(v -> {
						System.out.println("  - " + v.getFilepath());
					});
				}
			});
			
			System.out.println("\nStats:");
			CombinedStats stats = new CombinedStats(rawDb.getStats(), megamodelDb.getStats());
			stats.getArtefactRecoveryCompletion().forEach((k, v) -> {
				System.out.println("  " + String.format("%-8s", k) + " " + String.format("%.2f", v));
			});
		};
	}
	
	private Multimap<String, RawFile> compare(RawRepositoryDB rawDb, MegamodelDB megamodelDb, Set<String> artefactTypes) throws SQLException {
		Multimap<String, RawFile> byType = MultimapBuilder.hashKeys().arrayListValues().build();
		List<RawFile> files = rawDb.getFiles();
		Map<? extends String, ? extends Artefact> artefacts = megamodelDb.getAllArtefacts();
		
		for (RawFile rawFile : files) {
			if (! artefactTypes.contains(rawFile.getType()))
				continue;
				
			Artefact artefact = artefacts.get(rawFile.getFilepath());
			if (artefact == null) {
				//System.out.println("File " + rawFile.getFilepath() + " not found in MegamodelDB");
				byType.put(rawFile.getType(), rawFile);
				continue;
			} 			
		}

		return byType;
	}

	public static void main(String[] args) {
		int exitCode = new CommandLine(new ResultAnalyser()).execute(args);
		System.exit(exitCode);
	}
	
}

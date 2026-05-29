package mar.analysis.backend.megamodel.stats;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import mar.analysis.backend.megamodel.MegamodelDB;
import mar.analysis.backend.megamodel.RawRepositoryDB;
import mar.analysis.megamodel.model.Artefact;
import mar.analysis.megamodel.model.DuplicationRelationships;

public class DuplicationStats {

	// Duplication organized by type
	private Map<String, Integer> individualFilesTotal = new HashMap<String, Integer>();
	private Map<String, Double> individualFilesPercentage_Raw = new HashMap<String, Double>();
	private Map<String, Double> individualFilesPercentage_Mega = new HashMap<String, Double>();
		
	public DuplicationStats(MegamodelDB megamodel, RawRepositoryDB raw) {
		duplicationInfo(megamodel, raw);
	}
	
	private void duplicationInfo(MegamodelDB megamodelDb, RawRepositoryDB raw) {
		System.out.println("Duplication information");
		DuplicationRelationships duplicates = megamodelDb.getDuplicates();
		Map<String, Integer> totalCount = new HashMap<String, Integer>();
		Map<String, Integer> duplicatedCount = new HashMap<String, Integer>();
		Map<String, Integer> nonDuplicatedCount = new HashMap<String, Integer>();
		Map<String, Integer> duplicationGroupCount = new HashMap<String, Integer>();
		Set<String> seenGroups = new HashSet<String>();
		
		Set<String> types = new TreeSet<String>();
		for (Artefact artefact : megamodelDb.getAllArtefacts().values()) {
			totalCount.putIfAbsent(artefact.getType(), 0);
			totalCount.compute(artefact.getType(), (k, v) -> (v == null ? 0 : v) + 1);
			String group = duplicates.getGroupOf(artefact.getId());				
			if (group != null) {
				duplicatedCount.compute(artefact.getType(), (k, v) -> (v == null ? 0 : v) + 1);
				if (seenGroups.add(group)) {
					duplicationGroupCount.compute(artefact.getType(), (k, v) -> (v == null ? 0 : v) + 1);					
				}
			} else {
				nonDuplicatedCount.compute(artefact.getType(), (k, v) -> (v == null ? 0 : v) + 1);
			}
			types.add(artefact.getType());
		}

		RawRepositoryStats rawStats = raw.getStats();
		
		System.out.println("  " + String.format("%-8s", "Type") + " " + String.format("%-5s - %-5s", "Unique files", "Percentage"));
		for (String type : types) {
			int total = totalCount.get(type);
			int dups = duplicatedCount.getOrDefault(type, 0);
			int notDuplicated = nonDuplicatedCount.getOrDefault(type, 0);
			int totalDuplicationGroups = duplicationGroupCount.getOrDefault(type, 0);
			
			double duplicationPercentage = 100.0 * (dups / (double) total);
			int totalUnique = notDuplicated + totalDuplicationGroups;
			double uniquePercentage =  100.0 * (totalUnique / (double) total);
			double uniquePercentage_raw =  100.0 * (totalUnique / (double) rawStats.getCount(type));
			
			System.out.println("  " + String.format("%-8s", type) + " " + String.format("%5d", totalUnique) + " - " + String.format("%.2f", uniquePercentage) + " - " + totalUnique + " / " + total);

			individualFilesTotal.put(type, totalUnique);
			individualFilesPercentage_Mega.put(type, uniquePercentage);
			individualFilesPercentage_Raw.put(type, uniquePercentage_raw);
			
			
			// System.out.println("  " + String.format("%-8s", type) + " " + String.format("%.2f", duplicationPercentage) + " - " + dups + " / " + total);
		}
		
	}

	public int getUnique(String type) {
		return individualFilesTotal.get(type);
	}

	public double getUniquePercentageRaw(String type) {
		return individualFilesPercentage_Raw.get(type);
	}

	public double getUniquePercentageMega(String type) {
		return individualFilesPercentage_Mega.get(type);
	}
}

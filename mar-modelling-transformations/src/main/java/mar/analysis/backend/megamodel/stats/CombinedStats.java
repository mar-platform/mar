package mar.analysis.backend.megamodel.stats;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import mar.analysis.backend.megamodel.ArtefactType;
import mar.analysis.backend.megamodel.stats.InMemoryResultAnalyser.MegamodelAnalysisStats;

public class CombinedStats {

	@JsonProperty
	private final RawRepositoryStats raw;
	@JsonProperty
	private final MegamodelStats mega;
	@JsonProperty	
	private long totalRaw;
	@JsonProperty
	private long totalMega;
	@JsonProperty
	private double totalCompletion;
	@JsonProperty
	private final Map<String, Double> artefactRecoveryCompletion;
	@JsonProperty
	MegamodelAnalysisStats megamodelAnalysisStats;

	public CombinedStats(RawRepositoryStats raw, MegamodelStats mega, MegamodelAnalysisStats megamodelAnalysisStats) {
		this.raw = raw;
		this.mega = mega;
		this.megamodelAnalysisStats = megamodelAnalysisStats;
		this.artefactRecoveryCompletion = new HashMap<>();
		computeStats();
	}
	
	public RawRepositoryStats getRaw() {
		return raw;
	}
	
	public MegamodelStats getMega() {
		return mega;
	}
	
	public Map<? extends String, Double> getArtefactRecoveryCompletion() {
		return artefactRecoveryCompletion;
	}
	
	private void computeStats() {
		long totalRaw = 0;
		long totalMega = 0;
		
		for (ArtefactType type : ArtefactType.values()) {
			if (! type.isArtefactFile)
				continue;
			
			long rawCount  = raw.getCount(type.id);
			long megaCount = mega.getCount(type.id);
			
			totalRaw  += rawCount;
			totalMega += megaCount;
			
			double completion = (megaCount * 100.0) / rawCount; 
			
			artefactRecoveryCompletion.put(type.id, completion);
		}
		
		this.totalRaw = totalRaw;
		this.totalMega = totalMega;
		this.totalCompletion = totalMega * 100.0 / totalRaw;
	}
	
}

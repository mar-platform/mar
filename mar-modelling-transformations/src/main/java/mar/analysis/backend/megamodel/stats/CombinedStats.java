package mar.analysis.backend.megamodel.stats;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CombinedStats {

	@JsonProperty
	private final RawRepositoryStats raw;
	@JsonProperty
	private final MegamodelStats mega;

	public CombinedStats(RawRepositoryStats raw, MegamodelStats mega) {
		this.raw = raw;
		this.mega = mega;
	}
	
	public RawRepositoryStats getRaw() {
		return raw;
	}
	
	public MegamodelStats getMega() {
		return mega;
	}
	
}

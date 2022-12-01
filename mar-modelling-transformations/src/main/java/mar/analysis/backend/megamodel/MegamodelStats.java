package mar.analysis.backend.megamodel;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MegamodelStats {

	@JsonProperty
	private Map<String, Long> artefactTypeCount = new HashMap<>();
	
	public void addArtefactTypeCount(String type, long count) {
		artefactTypeCount.put(type, count);
	}

}

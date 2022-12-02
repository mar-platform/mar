package mar.analysis.backend.megamodel.stats;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RawRepositoryStats {

	@JsonProperty
	private Map<String, Long> artefactTypeCount = new HashMap<>();
	
	public void addArtefactTypeCount(String type, long count) {
		artefactTypeCount.put(type, count);
	}

	public long getCount(String type) {
		if (! artefactTypeCount.containsKey(type))
			return 0;
		return artefactTypeCount.get(type);
	}
}

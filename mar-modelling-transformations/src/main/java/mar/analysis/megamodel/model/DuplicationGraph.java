package mar.analysis.megamodel.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DuplicationGraph extends RelationshipsGraph {
	// extends RelationshipsGraph
	
	public static class ArtefactGroup extends RelationshipsGraph.VirtualNode {

		@JsonProperty
		private List<String> artefacts;
		
		public ArtefactGroup(String id, String type) {
			super(id, type);
			this.artefacts = new ArrayList<>();
			// Perhaps the type?
		}

		public void addArtefacts(Collection<? extends String> group) {
			artefacts.addAll(group);
		}
		
	}
	
	
}

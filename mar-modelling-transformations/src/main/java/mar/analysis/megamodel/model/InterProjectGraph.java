package mar.analysis.megamodel.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class InterProjectGraph extends RelationshipsGraph {
	
	public static class ProjectGroup extends RelationshipsGraph.VirtualNode {

		@JsonProperty
		private List<String> artefacts;
		
		public ProjectGroup(String id, String type) {
			super(id, type);
			this.artefacts = new ArrayList<>();
		}

		public void addArtefacts(Collection<? extends String> group) {
			artefacts.addAll(group);
		}
		
	}
	
	
}

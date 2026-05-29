package mar.analysis.megamodel.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

public class DuplicationGraph extends RelationshipsGraph {
	private List<Artefact> artefacts = new ArrayList<Artefact>();
	
	public void addArtefact(Artefact artefact) {
		artefacts.add(artefact);
	}
	
	public static class ArtefactGroup extends RelationshipsGraph.VirtualNode {

		@JsonProperty
		private List<String> artefacts;
		@JsonProperty
		private final String artefactType;
		
		public ArtefactGroup(String id, String type, String artefactType) {
			super(id, type);
			this.artefacts = new ArrayList<>();
			this.artefactType = artefactType;
			// Perhaps the type?
		}

		public void addArtefacts(Collection<? extends String> group) {
			artefacts.addAll(group);
		}
		
		public List<? extends String> getArtefacts() {
			return artefacts;
		}
		
		public String getArtefactType() {
			return artefactType;
		}
		
	}
	
	
}

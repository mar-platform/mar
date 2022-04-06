package mar.analysis.duplicates;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import mar.analysis.duplicates.DuplicateFinder.DuplicationGroup;
import mar.analysis.megamodel.model.Artefact;
import mar.analysis.megamodel.model.Relationship;
import mar.analysis.megamodel.model.RelationshipsGraph;
import mar.analysis.megamodel.model.RelationshipsGraph.Node;
import mar.artefacts.FileProgram;
import mar.artefacts.graph.RecoveryGraph;

public class DuplicateComputation {

	private Map<String, Collection<RecoveryGraph>> miniGraphs;
	private final RelationshipsGraph completeGraph;

	private final Map<String, DuplicateFinderConfiguration<?>> typeToConfiguration = new HashMap<>();
	
	public DuplicateComputation(Map<String, Collection<RecoveryGraph>> miniGraphs, RelationshipsGraph completeGraph) {
		this.miniGraphs = miniGraphs;
		this.completeGraph = completeGraph;
	}
	
	public void addType(String type, DuplicateFinderConfiguration<?> configuration) {
		typeToConfiguration.put(type, configuration);
	}
	

	public void updateGraph() {
		for (String type : typeToConfiguration.keySet()) {
			computeDuplicates(type);
		}
	}
	
	@SuppressWarnings("unchecked")
	private <T> void computeDuplicates(String type) {
		System.out.println("Finding duplicates: " + type);
		
		Collection<RecoveryGraph> graphs = miniGraphs.get(type);
		DuplicateFinderConfiguration<?> conf = typeToConfiguration.get(type);
		
		DuplicateFinder<T> finder = (DuplicateFinder<T>) conf.toFinder();
		
		Map<T, FileProgram> programs = new HashMap<>();
		for (RecoveryGraph graph : graphs) {			
			for (FileProgram p : graph.getPrograms()) {
				try {
					T model = (T) conf.toResource(p);
					finder.addResource(model);
					programs.put(model, p);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}		
		
		Collection<DuplicationGroup<T>> duplicates = finder.getDuplicates(0.8, 0.7);
		System.out.println("Adding " + duplicates.size() + " duplication groups");
		for (DuplicationGroup<T> duplicationGroup : duplicates) {
			T representative = duplicationGroup.getRepresentative();
			FileProgram p = programs.get(representative);
			
			String id = conf.toId(p) + "#duplicate-group"; 
			Node node = new RelationshipsGraph.Node(id, new Artefact(id, "duplication-group", "duplicates", conf.toName(p)), duplicationGroup);
			completeGraph.addNode(node);
			
			for (T m  : duplicationGroup) {
				FileProgram p1 = programs.get(m);
				completeGraph.addEdge(id, conf.toId(p1), Relationship.DUPLICATE);
			}
		}

	}
	
	public static interface DuplicateFinderConfiguration<T> {
		public T toResource(FileProgram p) throws Exception; 
		public String toName(FileProgram p);
		public DuplicateFinder<T> toFinder();
		public String toId(FileProgram p);
	}
	
}

package mar.analysis.duplicates;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.ecore.resource.Resource;

import mar.analysis.backend.megamodel.ArtefactType;
import mar.analysis.duplicates.DuplicateFinder.DuplicationGroup;
import mar.analysis.megamodel.model.Relationship;
import mar.analysis.megamodel.model.RelationshipsGraph;
import mar.analysis.megamodel.model.RelationshipsGraph.Node;
import mar.artefacts.FileProgram;
import mar.artefacts.Metamodel;
import mar.artefacts.graph.RecoveryGraph;

public class DuplicateComputation {

	private Map<ArtefactType, Collection<RecoveryGraph>> miniGraphs;
	private final RelationshipsGraph completeGraph;

	private final Map<ArtefactType, DuplicateFinderConfiguration<FileProgram, ?>> typeToConfiguration = new HashMap<>();
	private DuplicateFinderConfiguration<Metamodel, Resource> metamodelConfiguration;
	
	public DuplicateComputation(Map<ArtefactType, Collection<RecoveryGraph>> miniGraphs, RelationshipsGraph completeGraph) {
		this.miniGraphs = miniGraphs;
		this.completeGraph = completeGraph;
	}
	
	public void addType(ArtefactType type, DuplicateFinderConfiguration<FileProgram, ?> configuration) {
		typeToConfiguration.put(type, configuration);
	}
	
	public void setMetamodelConfiguration(DuplicateFinderConfiguration<Metamodel, Resource> metamodelConfiguration) {
		this.metamodelConfiguration = metamodelConfiguration;		
	}

	public void updateGraph() {
		DuplicateFinder<Metamodel, Resource> metamodelDuplicateFinder = metamodelConfiguration.toFinder();		
		for (ArtefactType type : typeToConfiguration.keySet()) {
			computeDuplicates(type, metamodelDuplicateFinder);
		}
		
		Collection<DuplicationGroup<Metamodel>> groups = metamodelDuplicateFinder.getDuplicates(0.7, 0.8);
		updateGraph(metamodelConfiguration, groups);
	}
	
	@SuppressWarnings("unchecked")
	private <T> void computeDuplicates(ArtefactType type, DuplicateFinder<Metamodel, Resource> metamodelDuplicateFinder) {
		System.out.println("Finding duplicates: " + type);
		
		Collection<RecoveryGraph> graphs = miniGraphs.get(type);
		if (graphs == null) {
			System.out.println("No graphs of type: " + type);
			return;
		}
		
		DuplicateFinderConfiguration<FileProgram, ?> conf = typeToConfiguration.get(type);
		DuplicateFinder<FileProgram, T> finder = (DuplicateFinder<FileProgram, T>) conf.toFinder();
		
		for (RecoveryGraph graph : graphs) {			
			for (FileProgram p : graph.getPrograms()) {
				try {
					T model = (T) conf.toResource(p);
					finder.addResource(p, model);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			for (Metamodel metamodel : graph.getMetamodels()) {
				try {
					Resource r = metamodelConfiguration.toResource(metamodel);
					metamodelDuplicateFinder.addResource(metamodel, r);
					r.unload();
				} catch (Exception e) {
					e.printStackTrace();
				}
			
			}
		}		
		
		Collection<DuplicationGroup<FileProgram>> duplicates = finder.getDuplicates(0.8, 0.7);
		updateGraph(conf, duplicates);

	}

	private <T> void updateGraph(DuplicateFinderConfiguration<T, ?> conf, Collection<DuplicationGroup<T>> duplicates) {
		System.out.println("Adding " + duplicates.size() + " duplication groups");
		for (DuplicationGroup<T> duplicationGroup : duplicates) {
			T representative = duplicationGroup.getRepresentative();
			
			String id = conf.toId(representative) + "#duplicate-group"; 
			Node node = new RelationshipsGraph.VirtualNode(id, "duplication");
			completeGraph.addNode(node);
			
			for (T p1  : duplicationGroup) {
				completeGraph.addEdge(id, conf.toId(p1), Relationship.DUPLICATE);
			}
		}
	}
	
	public static interface DuplicateFinderConfiguration<I, T> {
		public T toResource(I p) throws Exception; 
		public String toName(I p);
		public DuplicateFinder<I, T> toFinder();
		public String toId(I p);
	}
	
}

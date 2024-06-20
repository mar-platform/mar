package mar.analysis.duplicates;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.CheckForNull;

import org.eclipse.emf.ecore.resource.Resource;

import mar.analysis.backend.megamodel.ArtefactType;
import mar.analysis.duplicates.DuplicateFinder.DuplicationGroup;
import mar.artefacts.FileProgram;
import mar.artefacts.Metamodel;
import mar.artefacts.graph.RecoveryGraph;

public class DuplicateComputation {

	private final Map<ArtefactType, Collection<RecoveryGraph>> miniGraphs;

	private final Map<ArtefactType, DuplicateFinderConfiguration<FileProgram, ?>> typeToConfiguration = new HashMap<>();
	private DuplicateFinderConfiguration<Metamodel, Resource> metamodelConfiguration;
	
	public DuplicateComputation(Map<ArtefactType, Collection<RecoveryGraph>> miniGraphs) {
		this.miniGraphs = miniGraphs;
	}
	
	public void addType(ArtefactType type, DuplicateFinderConfiguration<FileProgram, ?> configuration) {
		typeToConfiguration.put(type, configuration);
	}
	
	public void setMetamodelConfiguration(DuplicateFinderConfiguration<Metamodel, Resource> metamodelConfiguration) {
		this.metamodelConfiguration = metamodelConfiguration;		
	}

	public DuplicationAnalysisResult run() {
		DuplicationAnalysisResult result = new DuplicationAnalysisResult(typeToConfiguration, metamodelConfiguration);
		
		DuplicateFinder<Metamodel, Resource> metamodelDuplicateFinder = metamodelConfiguration.toFinder();		
		for (ArtefactType type : typeToConfiguration.keySet()) {
			Collection<DuplicationGroup<FileProgram>> groups = computeDuplicates(type, metamodelDuplicateFinder);
			result.add(type, groups);
		}
		
		Collection<DuplicationGroup<Metamodel>> groups = metamodelDuplicateFinder.getDuplicates(0.7, 0.8);
		result.add(groups);
		
		return result;
	}
	
	@CheckForNull
	@SuppressWarnings("unchecked")
	private <T> Collection<DuplicationGroup<FileProgram>> computeDuplicates(ArtefactType type, DuplicateFinder<Metamodel, Resource> metamodelDuplicateFinder) {
		System.out.println("Finding duplicates: " + type);
		
		Collection<RecoveryGraph> graphs = miniGraphs.get(type);
		if (graphs == null) {
			System.out.println("No graphs of type: " + type);
			return null;
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
		return duplicates;
	}

	public static interface DuplicateFinderConfiguration<I, T> {
		public T toResource(I p) throws Exception; 
		public String toName(I p);
		public DuplicateFinder<I, T> toFinder();
		public String toId(I p);
	}
	
}

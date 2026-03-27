package mar.analysis.duplicates;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import javax.annotation.CheckForNull;

import org.eclipse.emf.ecore.resource.Resource;

import mar.analysis.backend.megamodel.ArtefactType;
import mar.analysis.duplicates.IDuplicateFinder.DuplicationGroup;
import mar.artefacts.FileProgram;
import mar.artefacts.Metamodel;
import mar.artefacts.graph.RecoveryGraph;

public class DuplicateComputation {

	private final Map<ArtefactType, Collection<RecoveryGraph>> miniGraphs;

	private final Map<ArtefactType, DuplicateFinderConfiguration<FileProgram, ?>> typeToConfiguration = new HashMap<>();
	private DuplicateFinderConfiguration<Metamodel, Resource> metamodelConfiguration;
	private HashDuplicates hashDuplicates;
	
	public DuplicateComputation(Map<ArtefactType, Collection<RecoveryGraph>> miniGraphs, HashDuplicates hashDuplicate) {
		this.miniGraphs = miniGraphs;
		this.hashDuplicates = hashDuplicate;
	}

	public void addType(ArtefactType type, DuplicateFinderConfiguration<FileProgram, ?> configuration) {
		typeToConfiguration.put(type, configuration);
	}
	
	public void setMetamodelConfiguration(DuplicateFinderConfiguration<Metamodel, Resource> metamodelConfiguration) {
		this.metamodelConfiguration = metamodelConfiguration;		
	}

	public DuplicationAnalysisResult run() {
		DuplicationAnalysisResult result = new DuplicationAnalysisResult(typeToConfiguration, metamodelConfiguration);
		
		IDuplicateFinder<Metamodel, Resource> metamodelDuplicateFinder = metamodelConfiguration.toFinder();		
		for (ArtefactType type : typeToConfiguration.keySet()) {
			Collection<DuplicationGroup<FileProgram>> groups = computeDuplicates(type, metamodelDuplicateFinder);
			result.add(type, groups);
		}
		
		
		IDuplicateFinder<Metamodel, Resource> finder = metamodelConfiguration.toFinder();
		Collection<RecoveryGraph> metamodelGraphs = miniGraphs.get(metamodelConfiguration.getType());
		Set<String> consideredHashDuplicates = new HashSet<String>();
		for (RecoveryGraph recoveryGraph : metamodelGraphs) {
			for (Metamodel metamodel : recoveryGraph.getMetamodels()) {
				String id = metamodelConfiguration.toId(metamodel);
				if (consideredHashDuplicates.contains(id))
					continue;
				
				try {
					Resource r = metamodelConfiguration.toResource(metamodel);

					Set<? extends String> hashDuplicationGroup = hashDuplicates.getDuplicationGroup("ecore", metamodelConfiguration.toId(metamodel));
					if (hashDuplicationGroup != null) {
						consideredHashDuplicates.addAll(hashDuplicationGroup);
						finder.addHashResource(metamodel, r, hashDuplicationGroup);
					} else {
						finder.addResource(metamodel, r);
					}
					
					r.unload();
				} catch (Exception e) {
					e.printStackTrace();
				}
			
			}
		}
		
		Collection<DuplicationGroup<Metamodel>> groups = finder.getDuplicates(0.8, 0.7);
		result.add(groups);
		
		
		//Collection<DuplicationGroup<Metamodel>> groups = metamodelDuplicateFinder.getDuplicates(0.8, 0.7);
		//result.add(groups);
		
		return result;
	}
	
	@CheckForNull
	@SuppressWarnings("unchecked")
	private <T> Collection<DuplicationGroup<FileProgram>> computeDuplicates(ArtefactType type, IDuplicateFinder<Metamodel, Resource> metamodelDuplicateFinder) {
		System.out.println("Finding duplicates: " + type);
		
		Collection<RecoveryGraph> graphs = miniGraphs.get(type);
		if (graphs == null) {
			System.out.println("No graphs of type: " + type);
			return null;
		}
		
		DuplicateFinderConfiguration<FileProgram, ?> conf = typeToConfiguration.get(type);
		IDuplicateFinder<FileProgram, T> finder = (IDuplicateFinder<FileProgram, T>) conf.toFinder();
		
		Set<String> consideredHashDuplicates = new HashSet<String>();
		for (RecoveryGraph graph : graphs) {			
			for (FileProgram p : graph.getPrograms()) {
				String id = conf.toId(p);
				if (consideredHashDuplicates.contains(id))
					continue;
				
				try {
					T model = (T) conf.toResource(p);

					Set<? extends String> hashDuplicationGroup = hashDuplicates.getDuplicationGroup(p.getKind(), conf.toId(p));
					if (hashDuplicationGroup != null) {
						consideredHashDuplicates.addAll(hashDuplicationGroup);
						finder.addHashResource(p, model, hashDuplicationGroup);
					} else {
						finder.addResource(p, model);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			// Not anymore because it is done globally (see above)
			/*
			for (Metamodel metamodel : graph.getMetamodels()) {
				try {
					Resource r = metamodelConfiguration.toResource(metamodel);
					metamodelDuplicateFinder.addResource(metamodel, r);
					r.unload();
				} catch (Exception e) {
					e.printStackTrace();
				}
			
			}
			*/
		}		

		Collection<DuplicationGroup<FileProgram>> duplicates = finder.getDuplicates(conf.default_t0(), conf.default_t1());
		return duplicates;
	}

	public static interface DuplicateFinderConfiguration<I, T> {
		public T toResource(I p) throws Exception; 
		public String toName(I p);
		public IDuplicateFinder<I, T> toFinder();
		public String toId(I p);
		public default double default_t0() {
			return 0.8;
		}
		public default double default_t1() {
			return 0.7;
		}
		
		public ArtefactType getType();	
	}
	
	public static record HashFinderConfiguration(ArtefactType type, Function<FileProgram, String> toId, Function<FileProgram, String> toName) implements DuplicateFinderConfiguration<FileProgram, Void> {

		@Override
		public Void toResource(FileProgram p) throws Exception {
			return null;
		}

		@Override
		public String toName(FileProgram p) {
			return toName(p);
		}

		@Override
		public IDuplicateFinder<FileProgram, Void> toFinder() {
			return new HashSimpleDuplicateFinder<FileProgram, Void>();
		}

		@Override
		public String toId(FileProgram p) {
			return toId(p);
		}

		@Override
		public ArtefactType getType() {
			return type;
		}
		
	}
	
}

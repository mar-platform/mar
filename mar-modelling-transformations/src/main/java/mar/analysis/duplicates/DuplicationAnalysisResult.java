package mar.analysis.duplicates;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.CheckForNull;

import org.eclipse.emf.ecore.resource.Resource;

import mar.analysis.backend.megamodel.ArtefactType;
import mar.analysis.backend.megamodel.MegamodelDB;
import mar.analysis.duplicates.DuplicateComputation.DuplicateFinderConfiguration;
import mar.analysis.duplicates.DuplicateFinder.DuplicationGroup;
import mar.artefacts.FileProgram;
import mar.artefacts.Metamodel;

/**
 * Gathers the results of the duplication of each type of file.
 * 
 * @author jesus
 */
public class DuplicationAnalysisResult {
	private final Map<ArtefactType, DuplicateFinderConfiguration<FileProgram, ?>> typeToConfiguration;
	private final DuplicateFinderConfiguration<Metamodel, Resource> metamodelConfiguration;

	private final Map<ArtefactType, Collection<DuplicationGroup<FileProgram>>> artefactGroups = new HashMap<>();
	private Collection<DuplicationGroup<Metamodel>> metamodelGroups;
	
	public DuplicationAnalysisResult(
			Map<ArtefactType, DuplicateFinderConfiguration<FileProgram, ?>> typeToConfiguration,
			DuplicateFinderConfiguration<Metamodel, Resource> metamodelConfiguration) {
		this.typeToConfiguration = typeToConfiguration;
		this.metamodelConfiguration = metamodelConfiguration;
	}

	public void add(ArtefactType type, Collection<DuplicationGroup<FileProgram>> groups) {
		artefactGroups.put(type, groups);
	}

	public void add(Collection<DuplicationGroup<Metamodel>> groups) {
		this.metamodelGroups = groups;
	}

	public void updateGraph(MegamodelDB db) {
		artefactGroups.forEach((type, groups) -> {
			DuplicateFinderConfiguration<FileProgram, ?> conf = typeToConfiguration.get(type);
			updateGraph(db, conf, groups);
		});
		
		updateGraph(db, metamodelConfiguration, metamodelGroups);
	}
	
	private <T> void updateGraph(MegamodelDB db, DuplicateFinderConfiguration<T, ?> conf, Collection<DuplicationGroup<T>> duplicates) {
		System.out.println("Adding " + duplicates.size() + " duplication groups");
		Set<String> alreadyAdded = new HashSet<>();
		for (DuplicationGroup<T> duplicationGroup : duplicates) {
			T representative = duplicationGroup.getRepresentative();
			
			String groupId = conf.toId(representative) + "#duplicate-group"; 
			
			for (T p1  : duplicationGroup) {
				String id = conf.toId(p1);
				if (alreadyAdded.add(id)) {
					db.addDuplicate(groupId, id);
				} else {
					System.out.println("Duplication: attempt to add an existing duplicate: " + id);
				}
			}
		}
	}

	@CheckForNull
	public <T> DuplicationGroup<FileProgram> getDuplicatesOf(ArtefactType type, FileProgram r) {
		Collection<DuplicationGroup<FileProgram>> groups = artefactGroups.get(type);
		if (groups == null) {
			return null;			
		}
		
		for (DuplicationGroup<FileProgram> g : groups) {
			if (g.contains(r)) {
				return g;
			}
		}
		
		return null;
	}
	

}

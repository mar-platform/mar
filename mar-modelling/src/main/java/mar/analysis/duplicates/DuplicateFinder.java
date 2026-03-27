package mar.analysis.duplicates;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A Java implementation of the duplicate detection algorithm of page 3 of:
 * 
 * "The Adverse Effects of Code Duplication in Machine Learning Models of Code"
 * 
 * @author jesus
 */
public abstract class DuplicateFinder<I, T> implements IDuplicateFinder<I, T> {
	
	// Indexed by it representative, which must be added through addHashResource
	protected final Map<I, Set<? extends String>> hashGroups = new HashMap<>();
	

	@Override
	public void addHashResource(I artefact, T resource, Set<? extends String> hashDuplicationGroup) {
		addResource(artefact, resource);
	}

	
}

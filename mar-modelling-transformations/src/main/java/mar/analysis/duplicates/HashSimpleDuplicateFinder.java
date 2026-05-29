package mar.analysis.duplicates;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.base.Preconditions;

public class HashSimpleDuplicateFinder<I, T> extends DuplicateFinder<I, T> {

	private Set<I> groupsByRepresentative = new HashSet<>();
	
	@Override
	public void addResource(I artefact, T resource) {
		groupsByRepresentative.add(artefact);
	}

	@Override
	public Collection<DuplicationGroup<I>> getDuplicates(double t0, double t1) {
		List<DuplicationGroup<I>> result = new ArrayList<>();
		for (I i : groupsByRepresentative) {
			DuplicationGroup<I> group = new DuplicationGroup<I>(i);
			Set<? extends String> groupElements = hashGroups.get(i);
			if (groupElements != null && groupElements.size() > 1) {
				group.addHashDuplicates(Preconditions.checkNotNull(hashGroups.get(i)));
				result.add(group);
			}
		}
		return result;
	}

}

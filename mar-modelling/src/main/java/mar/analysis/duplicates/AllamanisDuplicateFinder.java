package mar.analysis.duplicates;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.agrona.collections.Object2IntHashMap;

import com.google.common.base.Preconditions;

/**
 * A Java implementation of the duplicate detection algorithm of page 3 of:
 * 
 * "The Adverse Effects of Code Duplication in Machine Learning Models of Code"
 * 
 * @author jesus
 */
public abstract class AllamanisDuplicateFinder<I, T> extends DuplicateFinder<I, T> {

	private final ITokenExtractor<T> extractor;
	private final Map<I, Object2IntHashMap<String>> fingerprints = new HashMap<>();

	private int minTokens = 2;	
	
	public AllamanisDuplicateFinder(ITokenExtractor<T> extractor) {
		this.extractor = extractor;
	}

	
	@Override
	public void addResource(I artefact, T resource) {
		List<String> elements = extractor.extract(resource);
		//Not sure if it is ok or not considering empty or very simple artefacts
		//if (elements.size() <= minTokens) {
		//	return; 
		//}
		
		Object2IntHashMap<String> identifierMultiset = new Object2IntHashMap<String>(0);
		for (String token : elements) {
			Preconditions.checkNotNull(token);
			identifierMultiset.put(token, identifierMultiset.getValue(token) + 1);
		}		
		fingerprints.put(artefact, identifierMultiset);
	}
	
	/**
	 * This algorithm detects duplicate greedly. This means that once an elements is considered
	 * duplicate of one element, in its never reconsidered to be duplicate of another element if even
	 * if they are similar.
	 * 
	 * @param t0 Typically 0.8
	 * @param t1 Typically 0.7
	 * @return
	 */
	@Override
	public Collection<DuplicationGroup<I>> getDuplicates(double t0, double t1) {
		System.out.println("Fingerprints: " + fingerprints.size());
		Map<I, DuplicationGroup<I>> groups = new HashMap<>();
		fingerprints.forEach((k1, v1) -> {
			if (! groups.containsKey(k1)) {
				
				DuplicationGroup<I> k1Group = new DuplicationGroup<I>(k1);
				// If the resource comes from a hash
				if (hashGroups.containsKey(k1)) {
					k1Group.addHashDuplicates(hashGroups.get(k1));
				}
				
				fingerprints.forEach((k2, v2) -> {
					if (k1 != k2 && ! groups.containsKey(k2)) {
						if (areDuplicates(k1, k2, t0, t1)) {							
							k1Group.addDuplicate(k2);
							if (hashGroups.containsKey(k2)) {
								k1Group.addHashDuplicates(hashGroups.get(k1));
							}							
						}					
					}
				});
				
				// Everything in the group is associated with the group for fast lookup in the next iteration
				for (I resource : k1Group) {
					groups.put(resource, k1Group);
				}			
			}
		});
		
		Set<DuplicationGroup<I>> groupsWithTwoOrMore = groups.values().stream().filter(g -> g.size() > 1).collect(Collectors.toSet());
		return groupsWithTwoOrMore;
		
		// We need to compute the HashSet because the same DuplicationGroup appears several times
		// i.e., several resources points to the same groups (because they belong to it).
		// return new HashSet<>(groups.values());
	}
	
	private boolean areDuplicates(I a, I b, double t0, double t1) {
		Object2IntHashMap<String> t1_a = fingerprints.get(a);
		Object2IntHashMap<String> t1_b = fingerprints.get(b);
				
		// Multiset intersection is: for each pair of common keys, take the minimum value
		// Multiset union is:        for each pair of common keys, take the maximum value
		int cardinality_multiset_intersection = 0;
		int cardinality_multiset_union = 0;		
		int cardinarlity_intersection = 0;
		int cardinarlity_union = 0;
		
		for (String v1 : t1_a.keySet()) {
			int v1_count = t1_a.getValue(v1);
			int v2_count = t1_b.getValue(v1);
			cardinality_multiset_intersection += Math.min(v1_count, v2_count);
			
			// If we find the element in the other set, we count it for the intersection, 
			// but we don't count it for the union because it will be counted by just adding up all the elements of the other set (see code after this loop)
			if (v2_count != 0) {
				cardinarlity_intersection++;
				cardinality_multiset_union += Math.max(v1_count, v2_count);
			} else {
				cardinarlity_union++;
			}
		}
		
		cardinarlity_union += t1_b.size();
		
		for (String v2 : t1_b.keySet()) {
			int v1_count = t1_a.getValue(v2);
			// We count only those that doesn't match, because those that match has been counted in the previous loop 
			if (v1_count == 0) {
				int v2_count = t1_b.getValue(v2);
				cardinality_multiset_union += v2_count;
			}
		}
		
		double jaccard_t0 = ((double) cardinarlity_intersection) / cardinarlity_union;
		double jaccard_t1 = ((double) cardinality_multiset_intersection) / cardinality_multiset_union;
		
		return jaccard_t0 > t0 && jaccard_t1 > t1;
	}

	
	
}

package mar.analysis.duplicates;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;

public interface IDuplicateFinder<I, T> {

	void addResource(I artefact, T resource);

	void addHashResource(I artefact, T resource, Set<? extends String> hashDuplicationGroup);

	/**
	 * This algorithm detects duplicate greedly. This means that once an elements is considered
	 * duplicate of one element, in its never reconsidered to be duplicate of another element if even
	 * if they are similar.
	 * 
	 * @param t0 Typically 0.8
	 * @param t1 Typically 0.7
	 * @return
	 */
	Collection<DuplicationGroup<I>> getDuplicates(double t0, double t1);
	

	public static class DuplicationGroup<T> extends HashSet<T> {
		private static final long serialVersionUID = 1L;
		private T representative;
		private Set<String> hashArtefacts = new HashSet<String>();

		public DuplicationGroup(T representative) {
			this.representative = representative;
			this.add(representative);
		}

		public void addHashDuplicates(Set<? extends String> set) {
			hashArtefacts.addAll(set);
		}

		public void addDuplicate(T artefact) {
			this.add(artefact);
		}

		@Nonnull
		public T getRepresentative() {
			return this.representative;
		}
		
		public Set<? extends String> getHashArtefacts() {
			return hashArtefacts;
		}
		
	}


}

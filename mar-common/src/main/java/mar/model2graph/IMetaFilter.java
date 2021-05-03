package mar.model2graph;

import java.util.HashSet;

import org.eclipse.emf.ecore.EObject;

public interface IMetaFilter {
	
	public boolean passFilterObject(EObject o);
	
	public boolean passFilerStructural(EObject o);
	
	public static class UniversalSet<T> extends HashSet<T> {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public boolean contains (Object object) {
			return true;
			
		}
	}
	
}

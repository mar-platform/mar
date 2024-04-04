package mar.indexer.embeddings;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;

public interface WordExtractor {
	static final WordExtractor NAME_EXTRACTOR = new NameExtractor();
	
	List<String> toWords(Resource r);

	public static class NameExtractor implements WordExtractor {
		
		@Override
		public List<String> toWords(Resource r) {
			List<String> words = new ArrayList<>(64);
			TreeIterator<EObject> it = r.getAllContents();
			while (it.hasNext()) {
				EObject obj = it.next();
				EStructuralFeature f = obj.eClass().getEStructuralFeature("name");
				if (f != null) {
					Object value = obj.eGet(f);
					if (value instanceof String) {
						words.add((String) value);
					}
				}
			}
			return words;
		}
		
	}
}

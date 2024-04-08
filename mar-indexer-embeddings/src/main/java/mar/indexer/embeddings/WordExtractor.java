package mar.indexer.embeddings;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;

import mar.paths.stemming.CamelCaseTokenizer;

public interface WordExtractor {
	static final WordExtractor NAME_EXTRACTOR = new NameExtractor();
	static final WordExtractor ECLASS_EXTRACTOR = new EClassExtractor();
	
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
		
		@Override
		public String[] split(String original) {
			return CamelCaseTokenizer.INSTANCE.tokenize(original);
		}
		
	}

	public static class EClassExtractor implements WordExtractor {
		
		@Override
		public List<String> toWords(Resource r) {
			List<String> words = new ArrayList<>(64);
			TreeIterator<EObject> it = r.getAllContents();
			while (it.hasNext()) {
				EObject obj = it.next();
				if (obj instanceof EClass) {
					String value = ((EClass) obj).getName();
					if (value != null) {
						words.add((String) value);
					}
					
				}
			}
			return words;
		}
		
		@Override
		public String[] split(String original) {
			return CamelCaseTokenizer.INSTANCE.tokenize(original);
		}
		
	}

	
	String[] split(String original);
}

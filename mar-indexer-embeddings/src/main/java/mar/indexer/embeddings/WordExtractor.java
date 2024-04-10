package mar.indexer.embeddings;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;

import mar.paths.stemming.CamelCaseTokenizer;

public interface WordExtractor {
	static final WordExtractor NAME_EXTRACTOR = new NameExtractor();
	static final WordExtractor ECLASS_EXTRACTOR = new EClassExtractor();
	static final WordExtractor ECLASS_FEATURE_EXTRACTOR = new EClassAndEStructuralFeatureExtractor();

	public static final String CLASS_CATEGORY = "class";
	public static final String FEATURE_CATEGORY = "feature";
	
	// The index is the category of words
	WordList toWords(Resource r);

	public static class NameExtractor implements WordExtractor {
		
		@Override
		public WordList toWords(Resource r) {
			WordList words = new WordList();
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
		public WordList toWords(Resource r) {
			WordList words = new WordList();
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

	public static class EClassAndEStructuralFeatureExtractor implements WordExtractor {
				
		@Override
		public WordList toWords(Resource r) {
			WordList words = new WordList(CLASS_CATEGORY, FEATURE_CATEGORY);
			TreeIterator<EObject> it = r.getAllContents();
			while (it.hasNext()) {
				EObject obj = it.next();
				if (obj instanceof EClass) {
					String value = ((EClass) obj).getName();
					if (value != null) {
						words.add(value);
						words.add(CLASS_CATEGORY, value);
					}					
				} else if (obj instanceof EStructuralFeature) {
					String value = ((EStructuralFeature) obj).getName();
					if (value != null) {
						words.add(value);
						words.add(FEATURE_CATEGORY, value);
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

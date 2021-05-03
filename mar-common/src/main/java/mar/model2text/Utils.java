package mar.model2text;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;

import mar.model2graph.IMetaFilter;
import mar.paths.PathFactory;

public class Utils {

	public static String model2document(Resource r,  IMetaFilter mf, PathFactory pf) {
		StringBuffer buffer = new StringBuffer();
		String separator = "";
		TreeIterator<EObject> it = r.getAllContents();
		//iterate
		while (it.hasNext()) {
			EObject obj = it.next();
			//ignore poxies
			if (obj.eIsProxy()) {
				continue;
			}
			if (mf.passFilterObject(obj)) {
				for (EStructuralFeature f : obj.eClass().getEAllStructuralFeatures()) {
					if (f.isDerived())
						continue;
					if (!mf.passFilerStructural(f))
						continue;
					if (f instanceof EAttribute && f.isMany()) {
						@SuppressWarnings("unchecked")
						List<Object> attributes = (List<Object>) obj.eGet(f);
						for (Object object : attributes) {
							if ((object != null) && (object instanceof String)) {
								List<String> parts = applyTokSwStem((String) object, pf);
								for (String s: parts) {
									buffer.append(s).append(separator);
									separator = " ";
								}
							}
						}
					}
					if (f instanceof EAttribute && !f.isMany()) {
						Object attribute = obj.eGet(f);
						if ((attribute != null) && (attribute instanceof String)) {
							List<String> parts = applyTokSwStem((String) attribute, pf);
							for (String s : parts) {
								buffer.append(s).append(separator);
								separator = " ";
							}
						}
					}
				}
			}	
		}
						
		return buffer.toString();
	}
	
	@Nonnull
	public static List<String> applyTokSwStem(@Nonnull String input, @Nonnull PathFactory pf){
		List<String> result = new ArrayList<>();
		for (String subtok : pf.getTokenizer().tokenize(input)) {
			if (!pf.getStopWords().isStopWord(subtok)) {
				result.add(pf.getStemmer().stem(subtok));
			}
		}
		return result;
	}
}

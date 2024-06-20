package mar.analysis.duplicates;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.m2m.internal.qvt.oml.cst.UnitCS;

public class QVToDuplicateFinder<T> extends DuplicateFinder<T, UnitCS> {

	public QVToDuplicateFinder() {
		super(new QVToTokenExtractor());
	}

	private static class QVToTokenExtractor implements ITokenExtractor<UnitCS> {

		@Override
		public List<String> extract(UnitCS resource) {
			List<String> tokens = new ArrayList<>();
			TreeIterator<EObject> it = resource.eAllContents();
			while (it.hasNext()) {
				EObject obj = it.next();
				EClass c = obj.eClass();
				for (EAttribute eAttribute : c.getEAllAttributes()) {
					if (eAttribute.getEType().getName().contains("String")) {
						if (eAttribute.isMany()) {
							Collection<String> strs = (Collection<String>) obj.eGet(eAttribute);
							for (String string : strs) {
								addToken(tokens, string);
							}
						} else {
							String str = (String) obj.eGet(eAttribute);
							addToken(tokens, str);
						}
					}
				}
			}
			
			return tokens;
		}
		
	}
	
	private static void addToken(List<String> tokens, String string) {
		if (string != null)
			tokens.add(string);
	}	
}

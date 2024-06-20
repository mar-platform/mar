package mar.analysis.duplicates;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.uml2.uml.NamedElement;

// For the moment it uses the TokenExtractor from Ecore which essentially looks for Name
public class UMLDuplicateFinder<T> extends DuplicateFinder<T, Resource> {

	public UMLDuplicateFinder() {
		super(new UMLTokenExtractor());
	}
	
	public static class UMLTokenExtractor implements ITokenExtractor<Resource> {

		@Override
		public List<String> extract(Resource resource) {
			List<String> tokens = new ArrayList<>();
			TreeIterator<EObject> it = resource.getAllContents();
			while (it.hasNext()) {
				EObject obj = it.next();		
				if (obj instanceof NamedElement) {
					NamedElement ne = (NamedElement) obj;
					String name = ne.getName();
					addToken(tokens, name);
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

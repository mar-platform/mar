package mar.analysis.duplicates;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;

import mar.artefacts.Metamodel;

public class EcoreDuplicateFinder extends DuplicateFinder<Metamodel, Resource> {

	public EcoreDuplicateFinder() {
		super(new EcoreTokenExtractor());
	}

	private static class EcoreTokenExtractor implements ITokenExtractor<Resource> {

		@Override
		public List<String> extract(Resource resource) {
			List<String> tokens = new ArrayList<>();
			TreeIterator<EObject> it = resource.getAllContents();
			while (it.hasNext()) {
				EObject obj = it.next();				
				String value = (String) obj.eGet(EcorePackage.Literals.ENAMED_ELEMENT__NAME);
				addToken(tokens, value);				
			}
			
			return tokens;
		}		
	}
	
	private static void addToken(List<String> tokens, String string) {
		if (string != null)
			tokens.add(string);
	}	

}


package mar.analysis.smells.ecore;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcorePackage;

import mar.analysis.smells.Model;
import mar.analysis.smells.Smell;
import mar.analysis.smells.SmellDetector;

/**
 * From "Assessing the Quality of Meta-models, JJ Lopez-Fernandez, E. Guerra, J. de Lara"
 *  
 * D02 - There are no isolated classes (i.e., not involved in any association or hierarchy)
 * 
 * @author jesus
 */
public class IsolatedClassSmellDetector extends EcoreSmellDetector {

	@Override
	public List<Smell> detect(Model m) {
		List<Smell> results = new ArrayList<Smell>();
		
		List<? extends EClass> classes = m.allObjectsOf(EcorePackage.Literals.ECLASS, EClass.class);
		for (EClass eClass : classes) {
			if (eClass.getEAllReferences().size() > 0)
				continue;
			if (eClass.getESuperTypes().size() > 0)
				continue;
			
			boolean isReferenced = false;
			List<? extends EReference> allReferences = m.allObjectsOf(EcorePackage.Literals.EREFERENCE, EReference.class);
			for (EReference ref : allReferences) {
				if (ref.getEType() == eClass) {
					isReferenced = true;
					break;
				}
			}
			
			if (! isReferenced) {
				results.add(new IsolatedClassSmell(this, eClass));
			}		
		}
		
		return results;
	}
	
	public static class IsolatedClassSmell extends Smell {

		public IsolatedClassSmell(SmellDetector detector, EClass eClass) {
			super(detector, eClass);
		}
		
		@Nonnull
		public EClass getSmellyEClass() {
			return getSmellyObject(0, EClass.class);
		}	
	}
	
}

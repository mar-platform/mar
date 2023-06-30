package mar.analysis.smells.ecore;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.ENamedElement;
import org.eclipse.emf.ecore.EcorePackage;

import mar.analysis.smells.Model;
import mar.analysis.smells.Smell;
import mar.analysis.smells.SmellDetector;

/**
 * From "Assessing the Quality of Meta-models, JJ Lopez-Fernandez, E. Guerra, J. de Lara"
 * 
 * N05 - Element names are too complex to process.
 * 
 * @author ja
 */
public class TooLongNamesSmellDetector extends EcoreSmellDetector{
	private int thresh = 50;
	@Override
	public List<Smell> detect(Model m) {
		List<Smell> results = new ArrayList<Smell>();
		List<? extends ENamedElement> namelements = m.allObjectsOf(EcorePackage.Literals.ENAMED_ELEMENT, ENamedElement.class);
		for (ENamedElement eNamedElement : namelements) {
			if (eNamedElement.getName() != null && eNamedElement.getName().length() >= thresh)
				results.add(new TooLongNamesSmell(this, eNamedElement));
		}
		return results;
	}
	
	public static class TooLongNamesSmell extends Smell {

		public TooLongNamesSmell(SmellDetector detector, ENamedElement named) {
			super(detector, named);
		}
		
		@Nonnull
		public ENamedElement getSmellyElement() {
			return getSmellyObject(0, ENamedElement.class);
		}	
	}

}

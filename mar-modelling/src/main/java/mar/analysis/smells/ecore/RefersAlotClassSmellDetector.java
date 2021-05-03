package mar.analysis.smells.ecore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.annotation.Nonnull;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcorePackage;

import mar.analysis.smells.Model;
import mar.analysis.smells.Smell;
import mar.analysis.smells.SmellDetector;

/**
 * From "Assessing the Quality of Meta-models, JJ Lopez-Fernandez, E. Guerra, J. de Lara"
 * 
 * M02 - No class refers too many others.
 * 
 * @author ja
 */

public class RefersAlotClassSmellDetector extends EcoreSmellDetector{
	private int thresh = 5;
	@Override
	public List<Smell> detect(Model m) {
		
		List<Smell> results = new ArrayList<Smell>();
		List<? extends EClass> classes = m.allObjectsOf(EcorePackage.Literals.ECLASS, EClass.class);
		for (EClass eClass : classes) {
			if (eClass.getEReferences().size() > 0) {
				HashSet<EClassifier> references = new HashSet<EClassifier>();
				for (EReference eref : eClass.getEReferences()) {
					references.add(eref.getEType());
				}
				if (references.size()> thresh)
					results.add(new RefersAlotClassSmell(this, eClass));
			}
				
		}
		
		
		return results;
	}
	public int getThresh() {
		return thresh;
	}
	public void setThresh(int thresh) {
		this.thresh = thresh;
	}
	
	public static class RefersAlotClassSmell extends Smell {
		
		public RefersAlotClassSmell(SmellDetector detector, EClass eClass) {
			super(detector, eClass);
		}
		
		@Nonnull
		public EClass getSmellyEClass() {
			return getSmellyObject(0, EClass.class);
		}
		
		
	}

}

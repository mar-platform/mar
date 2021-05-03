package mar.analysis.smells.ecore;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EcorePackage;

import mar.analysis.smells.Model;
import mar.analysis.smells.Smell;
import mar.analysis.smells.SmellDetector;

/**
 * From "Assessing the Quality of Meta-models, JJ Lopez-Fernandez, E. Guerra, J. de Lara"
 * 
 * M01 - No class is overloaded with attributes.
 * 
 * @author ja
 */
public class OverLoadedClassSmellDetector extends EcoreSmellDetector{
	private int thresh = 5;
	
	@Override
	public List<Smell> detect(Model m) {
		List<Smell> results = new ArrayList<Smell>();
		List<? extends EClass> classes = m.allObjectsOf(EcorePackage.Literals.ECLASS, EClass.class);
		for (EClass eClass : classes) {
			if (eClass.getEAttributes().size() > thresh)
				results.add(new OverLoadedClassSmell(this, eClass));
		}
		
		
		return results;
	}

	public int getThresh() {
		return thresh;
	}

	public void setThresh(int thresh) {
		this.thresh = thresh;
	}
	
	public static class OverLoadedClassSmell extends Smell {
		
		public OverLoadedClassSmell(SmellDetector detector, EClass eClass) {
			super(detector, eClass);
		}
		
		@Nonnull
		public EClass getSmellyEClass() {
			return getSmellyObject(0, EClass.class);
		}
		
		
	}

}

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
 * M01 - No hierarchy is too deep.
 * 
 * @author ja
 */

public class DepthHierarchySmellDetector extends EcoreSmellDetector {
	private int thresh = 5;

	@Override
	public List<Smell> detect(Model m) {
		List<Smell> results = new ArrayList<Smell>();
		List<? extends EClass> classes = m.allObjectsOf(EcorePackage.Literals.ECLASS, EClass.class);
		for (EClass eClass : classes) {
			int depth = countDepth(eClass);
			if (depth >= thresh)
				results.add(new DepthHierarchySmell(this, eClass));
		}
		return results;
	}
	
	private static int countDepth(EClass klass) {
		if (klass.getESuperTypes().isEmpty())
			return 0;
		else {
			int max = -1;
			for (EClass sup : klass.getESuperTypes()) {
				max = Math.max(max, countDepth(sup));
			}
			max = max + 1;
			return max;
		}	
	}
	
	public int getThresh() {
		return thresh;
	}

	public void setThresh(int thresh) {
		this.thresh = thresh;
	}

	public class DepthHierarchySmell extends Smell {
		
		public DepthHierarchySmell(SmellDetector detector, EClass eClass) {
			super(detector, eClass);
		}
		
		@Nonnull
		public EClass getSmellyEClass() {
			return getSmellyObject(0, EClass.class);
		}
				
		
	}

}

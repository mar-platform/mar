package mar.analysis.smells.ecore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.annotation.Nonnull;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EcorePackage;

import mar.analysis.smells.Model;
import mar.analysis.smells.Smell;
import mar.analysis.smells.SmellDetector;

/**
 * From "Assessing the Quality of Meta-models, JJ Lopez-Fernandez, E. Guerra, J. de Lara"
 * 
 * M05 - No class has too many children.
 * 
 * @author ja
 */

public class TooManyChildrenSmellDetector extends EcoreSmellDetector{
	
	private int thresh = 10;
	@Override
	public List<Smell> detect(Model m) {
		HashMap<EClass, Integer> aux = new HashMap<EClass, Integer>();
		List<Smell> results = new ArrayList<Smell>();
		List<? extends EClass> classes = m.allObjectsOf(EcorePackage.Literals.ECLASS, EClass.class);
		for (EClass eClass : classes) {
			for (EClass eClass2 : eClass.getESuperTypes()) {
				if (aux.containsKey(eClass2))
					aux.put(eClass2, aux.get(eClass2) + 1);
				else
					aux.put(eClass2, 1);
			}
		}
		for (Entry<EClass,Integer> entry : aux.entrySet()) {
			if (entry.getValue() >= thresh) {
				results.add(new TooManyChildrenSmell(this, entry.getKey()));
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
	
	public static class TooManyChildrenSmell extends Smell {
		
		public TooManyChildrenSmell(SmellDetector detector, EClass eClass) {
			super(detector, eClass);
		}
		
		@Nonnull
		public EClass getSmellyEClass() {
			return getSmellyObject(0, EClass.class);
		}
		
	}

}

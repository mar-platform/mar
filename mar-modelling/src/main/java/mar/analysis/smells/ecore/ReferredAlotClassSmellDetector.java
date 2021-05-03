package mar.analysis.smells.ecore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

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
 * M03 - No class is referred from too many others.
 * 
 * @author ja
 */

public class ReferredAlotClassSmellDetector extends EcoreSmellDetector{
	private int thresh = 5;
	
	@Override
	public List<Smell> detect(Model m) {
		List<Smell> results = new ArrayList<Smell>();
		List<? extends EReference> erefs = m.allObjectsOf(EcorePackage.Literals.EREFERENCE, EReference.class);
		HashMap<EClass,Integer> map = new HashMap<EClass,Integer>();
		
		for (EReference eReference : erefs) {
			if (map.containsKey(eReference.getEType()))
				map.put((EClass) eReference.getEType(), map.get(eReference.getEType()) + 1);
			else
				map.put((EClass) eReference.getEType(), 1);
		}
		
		for(Entry<EClass, Integer> entry : map.entrySet()) {
			if (entry.getValue() > thresh)
				results.add(new ReferredAlotClassSmell(this, entry.getKey()));
				
		}
		return results;
	}
	
	
	public int getThresh() {
		return thresh;
	}


	public void setThresh(int thresh) {
		this.thresh = thresh;
	}


	public static class ReferredAlotClassSmell extends Smell {

		public ReferredAlotClassSmell(SmellDetector detector, EClass eClass) {
			super(detector, eClass);
		}
		
		@Nonnull
		public EClass getSmellyEClass() {
			return getSmellyObject(0, EClass.class);
		}	
	}

}

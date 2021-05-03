package mar.analysis.smells.ecore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EcorePackage;

import mar.analysis.smells.Model;
import mar.analysis.smells.Smell;
import mar.analysis.smells.SmellDetector;

/**
 * From "Assessing the Quality of Meta-models, JJ Lopez-Fernandez, E. Guerra, J. de Lara"
 * 
 * BP02 - There are no uninstantiable classes (i.e., abstract without concrete children).
 * 
 * @author jesus
 */
public class UninstantiableClassSmellDetector extends EcoreSmellDetector {

	@Override
	public List<Smell> detect(Model m) {
		List<Smell> results = new ArrayList<Smell>();
		
		List<? extends EClass> classes = m.allObjectsOf(EcorePackage.Literals.ECLASS, EClass.class);
		
		Map<EClass, List<EClass>> subtypes = new HashMap<>();		
		for (EClass sub : classes) {
			for (EClass sup: sub.getESuperTypes()) {
				List<EClass> list = subtypes.computeIfAbsent(sup, (k) -> new ArrayList<>());
				list.add(sub);
			}
		}

		for (EClass c : classes) {
			if (! c.isAbstract())
				continue;
			
			List<EClass> sub = subtypes.get(c);
			if (sub == null || sub.isEmpty()) {
				results.add(new UninstantiableClassSmell(this, c));
			}
		}
		
		return results;
	}

	public static class UninstantiableClassSmell extends Smell {

		public UninstantiableClassSmell(SmellDetector detector, EClass eClass) {
			super(detector, eClass);
		}
		
		@Nonnull
		public EClass getSmellyEClass() {
			return getSmellyObject(0, EClass.class);
		}	
	}
}

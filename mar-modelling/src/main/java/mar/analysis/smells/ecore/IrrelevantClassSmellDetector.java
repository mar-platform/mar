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
 * D05 - There are no irrelevant classes (i.e., abstract and subclass of concrete class).
 * 
 * @author jesus
 */
public class IrrelevantClassSmellDetector extends EcoreSmellDetector{

	@Override
	public List<Smell> detect(Model m) {
		List<Smell> results = new ArrayList<Smell>();
		List<? extends EClass> classes = m.allObjectsOf(EcorePackage.Literals.ECLASS, EClass.class);
		for (EClass eClass : classes) {
			if (eClass.isAbstract() && eClass.getESuperTypes().stream().anyMatch(c -> !c.isAbstract())) {
				results.add(new IrrelevantClassSmell(this, eClass));
			}
		}
		return results;
	}
	public static class IrrelevantClassSmell extends Smell {

		public IrrelevantClassSmell(SmellDetector detector, EClass eClass) {
			super(detector, eClass);
		}
		
		@Nonnull
		public EClass getSmellyEClass() {
			return getSmellyObject(0, EClass.class);
		}	
	}
}

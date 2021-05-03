package mar.chatbot.elements;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EcorePackage;

public class EcoreElementId extends ElementId {

	public EcoreElementId(String elementId) {
		super(elementId);
	}

	@Override
	public ElementsSet toSet() {
		Set<IElement> set = new HashSet<IElement>();
		for (EClassifier eClassifier : EcorePackage.eINSTANCE.getEClassifiers()) {
			if (eClassifier instanceof EClass) {
				EClass ec = (EClass) eClassifier;
				if (!ec.isAbstract() && ec.getEAllSuperTypes().contains(EcorePackage.Literals.ENAMED_ELEMENT)) {
					//TO DO: only lower case
					set.add(new SingleElement("name", this.elementId.toLowerCase(), ec.getName()));
				}
			}
		}
		ElementsSet eset = new ElementsSet(set, SetType.OR);
		return eset;
	}
	
}

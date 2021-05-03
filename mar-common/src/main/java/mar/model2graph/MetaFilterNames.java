package mar.model2graph;

import java.util.HashSet;
import java.util.List;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;


public class MetaFilterNames implements IMetaFilter{
	
	private HashSet<String> references = new HashSet<>();
	private HashSet<String> attributes = new HashSet<>();
	private HashSet<String> classes = new HashSet<>();
	
	private MetaFilterNames() {

	}
	
	public static MetaFilterNames getEcoreFilter() {
		
		MetaFilterNames mf = new MetaFilterNames();
		
		mf.attributes.add("ENamedElement:name");
		mf.attributes.add("EReference:containment");
		mf.attributes.add("ETypedElement:upperBound");
		mf.attributes.add("ETypedElement:lowerBound");
		mf.attributes.add("EClass:abstract");
		
		mf.classes.add("EReference");
		mf.classes.add("EAttribute");
		mf.classes.add("EClass");
		mf.classes.add("EDataType");
		mf.classes.add("EOperation");
		mf.classes.add("ENamedElement");
		mf.classes.add("EPackage");

		mf.references = new UniversalSet<String>();
		
		return mf;
	}
	
	public static MetaFilterNames getEcoreFilterJustNames() {
		
		MetaFilterNames mf = new MetaFilterNames();
		
		mf.attributes.add("ENamedElement:name");
		
		mf.classes.add("ENamedElement");
		
		mf.references = new UniversalSet<String>();
		
		return mf;
	}
	
	public static MetaFilterNames getBpmnMetaFilter() {
		MetaFilterNames mf = new MetaFilterNames();
		
		mf.classes.add("FlowNode");
		mf.classes.add("SequenceFlow");
		mf.classes.add("FlowElementsContainer");
		
		mf.attributes.add("FlowElement:name");
		mf.attributes.add("CallableElement:name");
		
		
		mf.references = new UniversalSet<String>();
		
		return mf;
	}
	

	@Override
	public boolean passFilterObject(EObject o) {
		EClass c = o.eClass();
		
		if (classes.contains(c.getName()))
			return true;
		
		List<EClass> supertypes = c.getEAllSuperTypes();
		for (EClass eClass : supertypes) {
			if (classes.contains(eClass.getName()))
				return true;
		}
		
		return false;
	}

	@Override
	public boolean passFilerStructural(EObject o) {
		
		if (o instanceof EReference) {
			EReference eo = (EReference) o;
			String search = eo.getEContainingClass().getName() + ":" + eo.getName();
			if (references.contains(search))
				return true;
		} else if (o instanceof EAttribute) {
			EAttribute ea = (EAttribute) o;
			String search = ea.getEContainingClass().getName() + ":" + ea.getName();
			if (attributes.contains(search))
				return true;
		}
		
		return false;
	}

}

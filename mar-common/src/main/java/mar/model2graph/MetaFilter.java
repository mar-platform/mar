package mar.model2graph;

import java.util.HashSet;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.uml2.uml.UMLPackage;

public class MetaFilter implements IMetaFilter {
	
	public static class UniversalSet<T> extends HashSet<T> {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public boolean contains (Object object) {
			return true;
			
		}
	}
	
	public static class NoFilter extends MetaFilter {
		@Override
		public boolean passFilterObject(EObject o) {
			return true;
		}		
		
		@Override
		public boolean passFilerStructural(EObject o) {
			return true;
		}	
	}
	
	private HashSet<EReference> ereferences = new HashSet<EReference>();
	private HashSet<EAttribute> eattribute = new HashSet<EAttribute>();
	private HashSet<EClass> eclasses = new HashSet<EClass>();
	
	private MetaFilter() {
		
	}
	
	public static MetaFilter getEcoreFilter() {
		MetaFilter mf = new MetaFilter();
		mf.eattribute.add(EcorePackage.Literals.ENAMED_ELEMENT__NAME);
		mf.eclasses.add(EcorePackage.Literals.ENAMED_ELEMENT);
		
		mf.eattribute.add(EcorePackage.Literals.EREFERENCE__CONTAINMENT);
		mf.eattribute.add(EcorePackage.Literals.ETYPED_ELEMENT__UPPER_BOUND);
		mf.eattribute.add(EcorePackage.Literals.ETYPED_ELEMENT__LOWER_BOUND);
		mf.eattribute.add(EcorePackage.Literals.ECLASS__ABSTRACT);
		//mf.eattribute.add(EcorePackage.Literals.ETYPED_ELEMENT__UPPER_BOUND); 
		//mf.eattribute.add(EcorePackage.Literals.ETYPED_ELEMENT__LOWER_BOUND);
		// the user have to add references
		mf.ereferences = new UniversalSet<EReference>();
		
		return mf;
	}
	
	public static MetaFilter getEcoreFilterNames() {
		MetaFilter mf = new MetaFilter();
		mf.eattribute.add(EcorePackage.Literals.ENAMED_ELEMENT__NAME);
		mf.eclasses.add(EcorePackage.Literals.ENAMED_ELEMENT);
		mf.ereferences = new UniversalSet<EReference>();
		return mf;
	}
	
	public static MetaFilter getUMLStateMachineFilter() {
		MetaFilter mf = new MetaFilter();
		mf.eclasses.add(UMLPackage.Literals.STATE_MACHINE);
		mf.eclasses.add(UMLPackage.Literals.STATE);
		mf.eclasses.add(UMLPackage.Literals.PSEUDOSTATE);
		mf.eclasses.add(UMLPackage.Literals.TRANSITION);
		mf.eclasses.add(UMLPackage.Literals.REGION);
//		mf.eclasses.add(UMLPackage.Literals.TRIGGER);
//		mf.eclasses.add(UMLPackage.Literals.CONSTRAINT);
//		mf.eclasses.add(UMLPackage.Literals.EVENT);
//		mf.eclasses.add(UMLPackage.Literals.OPERATION);
//		mf.eclasses.add(UMLPackage.Literals.CLASS);
//		mf.eclasses.add(UMLPackage.Literals.PROPERTY);
		// Put common superclass ValueSpecification?
//		mf.eclasses.add(UMLPackage.Literals.EXPRESSION);
//		mf.eclasses.add(UMLPackage.Literals.OPAQUE_EXPRESSION);
		mf.eclasses.add(UMLPackage.Literals.STATE_MACHINE);
		mf.eattribute.add(UMLPackage.Literals.NAMED_ELEMENT__NAME);
		mf.eattribute.add(UMLPackage.Literals.PSEUDOSTATE__KIND);
		mf.eattribute.add(UMLPackage.Literals.TRANSITION__KIND);
//		mf.eattribute.add(UMLPackage.Literals.EXPRESSION__SYMBOL);
//		mf.eattribute.add(UMLPackage.Literals.STATE__IS_SIMPLE);

		//mf.eattribute.add(UMLPackage.Literals.OPAQUE_EXPRESSION__BODY);
		mf.ereferences = new UniversalSet<EReference>();
		return mf;

	}
	
	public static MetaFilter getUMLEasyFilter() {
		MetaFilter mf = new MetaFilter();
		mf.eclasses.add(UMLPackage.Literals.NAMED_ELEMENT);
		mf.eattribute.add(UMLPackage.Literals.NAMED_ELEMENT__NAME);
		mf.eattribute.add(UMLPackage.Literals.MULTIPLICITY_ELEMENT__UPPER);
		mf.eattribute.add(UMLPackage.Literals.MULTIPLICITY_ELEMENT__LOWER);
		mf.eattribute.add(UMLPackage.Literals.CLASSIFIER__IS_ABSTRACT);
		//state machine
		mf.eattribute.add(UMLPackage.Literals.PSEUDOSTATE__KIND);
		mf.eattribute.add(UMLPackage.Literals.TRANSITION__KIND);
		
		mf.ereferences = new UniversalSet<EReference>();
		return mf;
	}
	
	public static MetaFilter getEcoreReduced() {
		MetaFilter mf = new MetaFilter();
		mf.eattribute.add(EcorePackage.Literals.ENAMED_ELEMENT__NAME);
		
		mf.eclasses.add(EcorePackage.Literals.EREFERENCE);
		mf.eclasses.add(EcorePackage.Literals.EATTRIBUTE);
		mf.eclasses.add(EcorePackage.Literals.ECLASS);
		mf.eclasses.add(EcorePackage.Literals.EOPERATION);
		mf.eclasses.add(EcorePackage.Literals.EPARAMETER);
		mf.eclasses.add(EcorePackage.Literals.EPACKAGE);
		mf.eclasses.add(EcorePackage.Literals.EENUM_LITERAL);
		mf.eclasses.add(EcorePackage.Literals.EENUM);
		mf.eclasses.add(EcorePackage.Literals.EDATA_TYPE);
		
		mf.eattribute.add(EcorePackage.Literals.EREFERENCE__CONTAINMENT);
		mf.eattribute.add(EcorePackage.Literals.ETYPED_ELEMENT__UPPER_BOUND);
		mf.eattribute.add(EcorePackage.Literals.ETYPED_ELEMENT__LOWER_BOUND);
		mf.eattribute.add(EcorePackage.Literals.ECLASS__ABSTRACT);
		
		//EReferences
		//mf.ereferences.add(EcorePackage.Literals.)
		
		return mf;
	}
	
	/**
	 * @return A filter that allows everything to be considered 
	 */
	public static MetaFilter getNoFilter() {
		return new NoFilter();
	}
	
	@Override
	public boolean passFilterObject(EObject o) {
		for (EClass eclass : eclasses) 
			if (eclass.isInstance(o))
				return true;
		return false;
	}
	
	@Override
	public boolean passFilerStructural(EObject o) {
		if (o instanceof EReference) {
			return ereferences.contains(o);
			//return ereferences.contains(o); TO DO: CHANGEE!
		} else if ( o instanceof EAttribute) {
			return eattribute.contains(o);
		}
			return false;
	}
	

}

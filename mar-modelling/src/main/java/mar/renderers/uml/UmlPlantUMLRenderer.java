package mar.renderers.uml;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Property;

import mar.renderers.PlantUMLRenderer;
import mar.renderers.PlantUmlCollection;
import mar.renderers.PlantUmlText;

public class UmlPlantUMLRenderer extends PlantUMLRenderer {

	public static UmlPlantUMLRenderer INSTANCE = new UmlPlantUMLRenderer();

	@Override
	protected void render(PlantUmlCollection diagrams, Resource r) {
		TreeIterator<EObject> it = r.getAllContents();
		while (it.hasNext()) {
			EObject obj = it.next();
			if (obj instanceof org.eclipse.uml2.uml.Model) {				
				Model pkg = (org.eclipse.uml2.uml.Model) obj;
				PlantUmlText diagram = new PlantUmlText();
				diagram.start();
				if (renderPkg(diagram, pkg)) {					
					diagram.end();
					diagrams.add(diagram, pkg, pkg.getName());
				}			
			}
		}		
	}

	private boolean renderPkg(PlantUmlText text, Package pkg) {
		boolean somethingDone = false;
		for (Element element : pkg.getOwnedElements()) {
			if (element instanceof Package) { 
				somethingDone |= renderPkg(text, (Package) element);
			} if (element instanceof org.eclipse.uml2.uml.Class) {
				org.eclipse.uml2.uml.Class c = (org.eclipse.uml2.uml.Class) element;
				somethingDone = true;
				
				for (Property ref : c.getAllAttributes()) {
					if (!(ref.getType() instanceof Class)) 
						continue;
					text.append(toName(c));
					text.append(" --> ");
					text.append(toName(ref)).append(" ");
					// TODO: Add details
					text.append(toName((Class) ref.getType()));
					text.line("");					
				}
				
				for (Class sup : c.getSuperClasses()) {
					text.append(toName(sup));
					text.append(" ^-- ");
					text.append(toName(c));
					text.line("");					
				}
			}
		}
		return somethingDone;
	}
	
	private String toName(Class c) {
		return '"' + c.getName() + '"';				 
	}

	private String toName(Property c) {
		return '"' + c.getName() + '"';				 
	}

}

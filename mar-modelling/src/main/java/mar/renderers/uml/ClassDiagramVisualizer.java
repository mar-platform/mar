package mar.renderers.uml;

import javax.annotation.CheckForNull;

import org.eclipse.uml2.uml.Association;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Property;

import mar.renderers.PlantUmlText;

/**
 * Docs: https://plantuml.com/en/state-diagram
 * 
 */
public class ClassDiagramVisualizer {
	
	public static final ClassDiagramVisualizer INSTANCE = new ClassDiagramVisualizer();
	
	@CheckForNull
	public PlantUmlText render(Package pkg) {	
		PlantUmlText text = new PlantUmlText();
		text.start();
		if (renderPkg(text, pkg)) {					
			text.end();
		} else {
			// There are no classes here
			return null;
		}
		return text;
	}

	private boolean renderPkg(PlantUmlText text, Package pkg) {
		boolean somethingDone = false;
		for (Element element : pkg.getOwnedElements()) {
			if (element instanceof Package) { 
				somethingDone |= renderPkg(text, (Package) element);
			} else if (element instanceof org.eclipse.uml2.uml.Class) {
				org.eclipse.uml2.uml.Class c = (org.eclipse.uml2.uml.Class) element;
				somethingDone = true;
				
				text.append("class ").line(toName(c));
				
				for (Property ref : c.getAllAttributes()) {
					if (!(ref.getType() instanceof org.eclipse.uml2.uml.Class)) 
						continue;
					text.append(toName(c));
					text.append(" --> ");
					text.append(toName(ref)).append(" ");
					// TODO: Add details
					text.append(toName((org.eclipse.uml2.uml.Class) ref.getType()));
					text.line("");					
				}
				
				for (Class sup : c.getSuperClasses()) {
					text.append(toName(sup));
					text.append(" ^-- ");
					text.append(toName(c));
					text.line("");					
				}
			} else if (element instanceof org.eclipse.uml2.uml.Association) {
				Association assoc = (Association) element;
				if (assoc.getOwnedEnds().size() == 2) {
					Property p1 = assoc.getOwnedEnds().get(0);
					Property p2 = assoc.getOwnedEnds().get(1);
					if (! (p1.getType() instanceof Class && p2.getType() instanceof Class))
						continue;
					
					text.append(toName((org.eclipse.uml2.uml.Class) p1.getType()));
					text.append(" --> ");
					text.append(toName(assoc)).append(" ");
					// TODO: Add details
					text.append(toName((org.eclipse.uml2.uml.Class) p2.getType()));
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
	
	private String toName(Association assoc) {
		return '"' + assoc.getName() + '"';				 
	}	

}

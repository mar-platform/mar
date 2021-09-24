package mar.renderers.uml;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.CheckForNull;

import org.eclipse.uml2.uml.Actor;
import org.eclipse.uml2.uml.Association;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.UseCase;

import mar.renderers.PlantUmlText;

/**
 * Docs: https://plantuml.com/en/state-diagram
 * 
 */
public class UseCaseVisualizer {
	
	public static final UseCaseVisualizer INSTANCE = new UseCaseVisualizer();
	
	@CheckForNull
	public PlantUmlText render(Package pkg) {	
		PlantUmlText text = new PlantUmlText();
		text.start();
		Map<Object, String> elementsToIds = new HashMap<>();
		if (renderPkg(text, pkg, elementsToIds)) {					
			text.end();
		} else {
			// There are no classes here
			return null;
		}
		return text;
	}

	private boolean renderPkg(PlantUmlText text, Package pkg, Map<Object, String> elementsToIds) {
		boolean somethingDone = false;
		for (Element element : pkg.getOwnedElements()) {
			if (element instanceof Package) { 
				somethingDone |= renderPkg(text, (Package) element, elementsToIds);
			} else if (element instanceof org.eclipse.uml2.uml.Actor) {
				Actor actor = (Actor) element;
				text.append("actor :").append(actor.getName()).append(": ").append(" as ").append(toId(element, elementsToIds)).append("\n");				
				somethingDone = true;
			} else if (element instanceof UseCase) {
				UseCase usecase = (UseCase) element;
				text.append("usecase (").append(usecase.getName()).append(") ").append(" as ").append(toId(element, elementsToIds)).append("\n");				
				somethingDone = true;
			} else if (element instanceof Association) {
				Association assoc = (Association) element;
				if (assoc.getOwnedEnds().size() == 2) {
					Property p1 = assoc.getOwnedEnds().get(0);
					Property p2 = assoc.getOwnedEnds().get(1);

					if (p1.getType() instanceof UseCase && p2.getType() instanceof Actor) { 
						Property tmp = p2;
						p2 = p1;
						p1 = tmp;
					} else if (p2.getType() instanceof UseCase && p1.getType() instanceof Actor) {
						// That's ok, from the actor to the use case
					} else if (p1.getType() instanceof UseCase && p2.getType() instanceof UseCase) {
						// That should also be ok
					} else {
						continue;
					}
					
					renderAssocProperty(text, elementsToIds, p1);					
					text.append("-->");
					renderAssocProperty(text, elementsToIds, p2);	
					text.line("");
				}				
			}
		}
		return somethingDone;
	}

	private void renderAssocProperty(PlantUmlText text, Map<Object, String> elementsToIds, Property p1) {
		if (p1.getType() instanceof UseCase) {
			text.append("(").append(toId(p1.getType(), elementsToIds)).append(")");
		} else if (p1.getType() instanceof Actor) {
			text.append(":").append(toId(p1.getType(), elementsToIds)).append(":");
		}
	}
	
	private String toId(Element element, Map<Object, String> elementsToIds) {
		if (! elementsToIds.containsKey(element))
			elementsToIds.put(element, element.eClass().getName() + elementsToIds.size() + 1);
			
		return elementsToIds.get(element);		
	}


}

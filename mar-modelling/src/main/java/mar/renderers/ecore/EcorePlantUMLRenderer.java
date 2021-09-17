package mar.renderers.ecore;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.ENamedElement;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;

import mar.renderers.PlantUMLRenderer;
import mar.renderers.PlantUmlCollection;
import mar.renderers.PlantUmlText;

public class EcorePlantUMLRenderer extends PlantUMLRenderer {

	public static EcorePlantUMLRenderer INSTANCE = new EcorePlantUMLRenderer();
	
	@Override
	protected void render(PlantUmlCollection diagrams, Resource r) {
		EObject obj = r.getContents().get(0);
		if (! (obj instanceof EPackage) ) {
			return;
		}
		PlantUmlText text = diagrams.newDiagram(obj, ((ENamedElement) obj).getName());
		text.start();
		render(text, r);
		text.end();	
	}
	
	protected void render(PlantUmlText text, Resource r) {	
		r.getAllContents().forEachRemaining(obj -> {
			if (obj instanceof EClass) {
				EClass c = (EClass) obj;
				
				for (EReference ref : c.getEReferences()) {
					text.append(toName(c));
					text.append(" --> ");
					text.append(toName(ref)).append(" ");
					// TODO: Add details
					text.append(toName(ref.getEReferenceType()));
					text.line("");
				}
				
				for (EClass sup : c.getESuperTypes()) {
					text.append(toName(sup));
					text.append(" ^-- ");
					text.append(toName(c));
					text.line("");					
				}
				
			}			
		});

		text.append("@enduml");
	}

	private String toName(EReference ref) {
		return '"' + ref.getName() + '"';				 
	}

	private String toName(EClass c) {
		return '"' + c.getName() + '"';				 
	}

}

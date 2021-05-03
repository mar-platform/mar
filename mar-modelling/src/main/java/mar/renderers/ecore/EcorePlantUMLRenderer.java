package mar.renderers.ecore;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;

import mar.renderers.PlantUMLRenderer;
import mar.renderers.PlantUmlText;

public class EcorePlantUMLRenderer extends PlantUMLRenderer {

	public static EcorePlantUMLRenderer INSTANCE = new EcorePlantUMLRenderer();
	
	@Override
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

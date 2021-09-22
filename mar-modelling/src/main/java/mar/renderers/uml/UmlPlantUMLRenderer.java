package mar.renderers.uml;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.uml2.uml.Activity;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.StateMachine;

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
				PlantUmlText diagram = ClassDiagramVisualizer.INSTANCE.render(pkg);
				if (diagram != null) {
					diagrams.add(diagram, pkg, pkg.getName());
				}
			} else if (obj instanceof StateMachine) {
				PlantUmlText diagram = StateMachineVisualizer.INSTANCE.render((StateMachine) obj);
				diagrams.add(diagram, obj, ((StateMachine) obj).getName());
			} else if (obj instanceof Activity) {
				PlantUmlText diagram = ActivityDiagramVisualizer.INSTANCE.render((Activity) obj);
				diagrams.add(diagram, obj, ((Activity) obj).getName());				
			}  else if (obj instanceof Interaction) {
				PlantUmlText diagram = InteractionDiagramVisualizer.INSTANCE.render((Interaction) obj);
				diagrams.add(diagram, obj, ((Interaction) obj).getName());				
			}
		}		
	}
	

}

package mar.renderers.uml;

import java.io.File;
import java.io.IOException;

import org.eclipse.emf.ecore.resource.Resource;
import org.junit.BeforeClass;
import org.junit.Test;

import mar.analysis.uml.UMLAnalyser;
import mar.common.test.TestUtils;
import mar.indexer.common.configuration.ModelLoader;
import mar.renderers.PlantUmlCollection;
import mar.renderers.PlantUmlCollection.PlantUmlImage;

public class UmlPlantUMLRendererTest {

	@BeforeClass
	public static void register() {
		new UMLAnalyser.Factory().configureEnvironment();
	}
	
	@Test
	public void testSimple() throws IOException {		
		Resource r = ModelLoader.UML.load(TestUtils.getExternalResource("uml", "bank-1.xmi"));
		PlantUmlCollection collection = UmlPlantUMLRenderer.INSTANCE.render(r);
		for (PlantUmlImage diagram : collection) {
			File output = TestUtils.getOutputFile("bank-" + diagram.getIndex() + ".png");
			System.out.println("Output to: " + output);
			diagram.toImage(output);
		}
		
	}
	
}

package mar.renderers.ecore;

import java.io.File;
import java.io.IOException;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.junit.BeforeClass;
import org.junit.Test;

import mar.common.test.TestUtils;
import mar.indexer.common.configuration.ModelLoader;

public class EcorePlantUMLRendererTest {

	@BeforeClass
	public static void register() {
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap( ).put("*", new XMIResourceFactoryImpl());
	}
	
	@Test
	public void testSimple() throws IOException {
		Resource r = ModelLoader.DEFAULT.load(TestUtils.getExternalResource("ecore", "JavaM.ecore"));
		File output = TestUtils.getOutputFile("JavaM.png");
		System.out.println("Output to: " + output);
		EcorePlantUMLRenderer.INSTANCE.renderTo(r, output);
	}
	
}

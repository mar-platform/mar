package mar.loader;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.junit.Test;

import mar.model2graph.AbstractPathComputation;
import mar.model2graph.MetaFilter;
import mar.model2graph.Model2GraphAllpaths;
import mar.models.bpmn.BPMNLoader;
import mar.paths.ListofPaths;
import mar.paths.PathFactory;

public class BPMNLoaderTest {

	static {
    	Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap( ).put("*", new XMIResourceFactoryImpl());
	}
	
	@Test
	public void test() throws IOException {
		BPMNLoader loader = new BPMNLoader();
		String xmi = IOUtils.toString(new FileInputStream("src/test/resources/bpmn-examples/bpmn-1.xmi"), "utf-8");
		Resource r = loader.load(xmi);
		
		assertNotNull(r);
		
		List<EObject> objects = new ArrayList<>();
		TreeIterator<EObject> it = r.getAllContents();
		while (it.hasNext()) {
			objects.add(it.next());
		}
		
		assertTrue(objects.size() > 0);
		assertTrue(objects.size() > 5);	
		
		AbstractPathComputation pathComputation = new Model2GraphAllpaths(4)
    			.withPathFactory(new PathFactory.DefaultPathFactory());
		pathComputation.withFilter(MetaFilter.getNoFilter());
		
		ListofPaths paths = pathComputation.getListOfPaths(r);
		Map<String, Map<String, Integer>> tokens = paths.toMapParticionedPaths();
		
		assertTrue(tokens.size() == 107);		
		
		System.out.println(tokens.size());
		tokens.forEach((k, v) -> {
			System.out.println(k);
			
		});
		
	}

}

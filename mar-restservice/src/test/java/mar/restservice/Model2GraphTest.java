package mar.restservice;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.junit.Test;

import mar.model2graph.Model2GraphAllpaths;
import mar.model2graph.PathComputation;

public class Model2GraphTest extends MarTest {

	@Test
	public void testName() throws Exception {
		ResourceSet rs = new ResourceSetImpl();
		File f = new File("src/test/resources/statemachine-query.ecore");
		Resource r = rs.getResource(URI.createFileURI(f.getAbsolutePath()), true);
		
		PathComputation pathComputation = new Model2GraphAllpaths(4);
		
		Map<String, Integer> map = pathComputation.getListOfPaths(r).toMapPaths();
		assertTrue(map.size() > 0);
		for (Entry<String, Integer> entry : map.entrySet()) {
			System.out.println(entry.getKey() + " - " + entry.getValue());
		}
		
		
		Map<String, Map<String, Integer>> parts = pathComputation.getListOfPaths(r).toMapParticionedPaths();
		assertTrue(parts.size() > 0);
		parts.forEach((k, v) -> {
			System.out.println(k);
			for (Entry<String, Integer> entry : v.entrySet()) {
				System.out.println("  " + entry.getKey() + " - " + entry.getValue());
			}
			System.out.println("--");			
		});
	}
	
}

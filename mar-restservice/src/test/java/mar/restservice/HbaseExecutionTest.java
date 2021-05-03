package mar.restservice;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.lang.time.StopWatch;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.junit.Test;

import mar.MarConfiguration;

public class HbaseExecutionTest extends MarTest {

	private static final MarConfiguration conf = MarConfiguration.getHbaseConfiguration("ecore");
	
	@Test
	public void testStateMachine() throws IOException {
		ResourceSet rs = new ResourceSetImpl();
		File f = new File("src/test/resources/statemachine-query.ecore");
		//File f = new File("src/test/resources/relational-query.ecore");
		Resource r = rs.getResource(URI.createFileURI(f.getAbsolutePath()), true);

		IScorer scorer = conf.getScorer();
		
		// TODO: Check the results
		StopWatch watch = new StopWatch();
		watch.start();
		{
			Map<String, Double> result = scorer.sortedScore(r, 10);
			//Map<String, Double> result = new HbaseScorer_JA().score(r);
			firtsTenElements(result).forEach((k, v) -> {
				System.out.println(k + " - " + String.format("%.2f", v));
			});
		}
		watch.stop();
		System.out.println("Total: " + String.format("%.2f", watch.getTime() / 1000.0));		
	}

	 private static <K, V extends Comparable<? super V>> Map<K, V> firtsTenElements(Map<K, V> map) {
	        List<Entry<K, V>> list = new ArrayList<>(map.entrySet());
	        //list.sort(Entry.comparingByValue());
	        Collections.sort(list, Collections.reverseOrder(Entry.comparingByValue()));
	        list = list.stream().limit(10).collect(Collectors.toList());
	        Map<K, V> result = new LinkedHashMap<>();
	        for (Entry<K, V> entry : list) {
	            result.put(entry.getKey(), entry.getValue());
	        }

	        return result;
	    }
}

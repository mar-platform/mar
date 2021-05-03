package mar.indexer.common.configuration;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Test;

import mar.indexer.common.configuration.IndexJobConfigurationData;
import mar.indexer.common.configuration.SingleIndexJob;

public class IndexConfigurationDataTest {

	@Test
	public void testParseJson() {
		String str = "{ repositories: {\"repo-ecore\" : {\n" + 
				"        \"type\" : \"ecore\",\n" +
				"        \"origin\" : \"test\",\n" +
				"        \"repo_root\" : \"/home/jesus/repo\",\n" + 
				"        \"file_list\" : \"/home/jesus/repo/ecore/files.txt\",\n" + 
				"        \"graph_length\" : 3,\n" + 
				"        \"graph_filter\": \"EcoreFilter\",\n" + 
				"        \"graph_factory\": \"EcoreFactory\"\n" + 
				"}}}";        
		System.out.println(str);
		IndexJobConfigurationData data = IndexJobConfigurationData.fromJSON(str);
		
		SingleIndexJob repo = data.getRepo("repo-ecore");

		assertEquals("ecore", repo.getType());
		assertEquals("/home/jesus/repo", repo.getRootFolder());
		assertEquals("/home/jesus/repo/ecore/files.txt", repo.getFileList());
		
		assertEquals(3, repo.getGraphLength());
		assertEquals("EcoreFilter", repo.getGraphFilter());
		assertEquals("EcoreFactory", repo.getGraphFactory());		
	}
	
	@Test
	public void testVariableReplacement() {
		SingleIndexJob job = new SingleIndexJob();
		String newPath = job.replaceEnv("$(ROOT)/a/path", Map.of("ROOT", "/home/jesus/root"));
		assertEquals("/home/jesus/root/a/path", newPath);
	}
}

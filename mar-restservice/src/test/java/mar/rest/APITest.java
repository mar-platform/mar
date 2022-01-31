package mar.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import edu.emory.mathcs.backport.java.util.Arrays;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import mar.common.test.HBaseTest;
import mar.rest.testing.MarHttpServer;

@Category(HBaseTest.class)
public class APITest {

	@ClassRule
	public static MarHttpServer server = new MarHttpServer();

	@Test
	public void testInvalidParameter() {
		String search = "whatever"; 
		HttpResponse<String> result = Unirest.post(server.getURL("search-full"))	
				.body(search)
				.asString();
		
		assertEquals(result.getBody(), 400, result.getStatus());
	}

	@Test
	public void testNotExistMetadata() {
		HttpResponse<String> result = Unirest.get(server.getURL("search/metadata"))
				.queryString("id", "no_exists/JavaM.ecore")
				.asString();
		
		assertEquals(404, result.getStatus());
	}
	
	@Test
	public void testSearchKeyword() {
		HttpResponse<String> result = Unirest.post(server.getURL("search/keyword"))
				.body("SQLScript")
				.asString();
		
		assertEquals(200, result.getStatus());
		assertNotNull(result.getBody());
		assertNotEquals("{}", result.getBody());
	}
	
	@Test
	public void testMetadata() {
		HttpResponse<String> result = Unirest.get(server.getURL("search/metadata"))
				.queryString("id", "test-data:ecore:/ecore/sql-with-smells.ecore")
				.asString();
		
		assertEquals(200, result.getStatus());
		assertNotNull(result.getBody());
		// Make sure that smells are captured
		for(String smell: new String[] { "IrrelevantClassSmell", "ReferredAlotClassSmell", "IsolatedClassSmell", "OnlyOneClassSuperSmell" }) {
			assertTrue(result.getBody().contains(smell));			
		}
		assertTrue(result.getBody().contains("[\"test\",\"model\"]"));
		assertTrue(result.getBody().contains("\"numElements\":314"));		
		assertNotEquals("{}", result.getBody());
	}
	
}

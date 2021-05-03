package mar.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

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
		HttpResponse<String> result = Unirest.get(server.getURL("v1/search/metadata"))
				.queryString("id", "no_exists/JavaM.ecore")
				.asString();
		
		assertEquals(404, result.getStatus());
	}
	
	@Test
	public void testMetadata() {
		HttpResponse<String> result = Unirest.get(server.getURL("v1/search/metadata"))
				.queryString("id", "test-data:ecore:/ecore/JavaM.ecore")
				.asString();
		
		assertEquals(200, result.getStatus());
		assertNotNull(result.getBody());
		assertNotEquals("{}", result.getBody());
	}
}

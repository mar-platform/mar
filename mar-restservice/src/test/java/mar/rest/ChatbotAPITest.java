package mar.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import mar.common.test.HBaseTest;
import mar.rest.testing.MarHttpServer;

@Category(HBaseTest.class)
public class ChatbotAPITest {

	@ClassRule
	public static MarHttpServer server = new MarHttpServer();
	
	@Test
	public void testSearchAnd() {
		String search = "[\n" + 
				"	    {\n" + 
				"	         \"attribute\": \"name\",\n" + 
				"	         \"value\": \"class\",\n" + 
				"             \"metaclass\": \"EClass\"\n" + 
				"	    },\n" + 
				"        {\n" + 
				"	         \"attribute\": \"name\",\n" + 
				"	         \"value\": \"student\",\n" + 
				"             \"metaclass\": \"EClass\"\n" + 
				"	    }\n" + 
				"	]"; 
		HttpResponse<String> result = Unirest.post(server.getURL("v1/search/path?mode=and&type=ecore"))
				.body(search)
				.asString();
		
		assertEquals(200, result.getStatus());
		assertNotNull(result.getBody());
		// No results because we don't have models that satisfy all these conditions
		assertEquals("{\"key\":0,\"results\":[]}", result.getBody());
	}

	@Test
	public void testSearchOr() {
		// This should match ecore/JavaM.ecore
		String search = "[\n" + 
				"	    {\n" + 
				"	         \"attribute\": \"name\",\n" + 
				"	         \"value\": \"Class\",\n" + 
				"             \"metaclass\": \"EClass\"\n" + 
				"	    },\n" + 
				"        {\n" + 
				"	         \"attribute\": \"name\",\n" + 
				"	         \"value\": \"member\",\n" + 
				"             \"metaclass\": \"EClass\"\n" + 
				"	    }\n" + 
				"	]"; 
		HttpResponse<String> result = Unirest.post(server.getURL("v1/search/path?mode=or&type=ecore"))
				.body(search)
				.asString();
		
		// System.out.println(result.getBody());
		assertEquals(200, result.getStatus());
		assertNotNull(result.getBody());	
	}

	@Test
	public void testSearchId() {
		String search = "{\n" + 
				"	         \"elementId\": 'class'\n" + 
				"	    }"; 
		HttpResponse<String> result = Unirest.post(server.getURL("v1/search/path?mode=id&type=ecore"))
				.body(search)
				.asString();
		
		assertEquals(200, result.getStatus());
		assertNotNull(result.getBody());
		assertTrue(result.getBody().contains("test-data:ecore:/ecore/JavaM.ecore"));
	}

	@Test
	public void testSearchId_InvalidRequest() {
		String search = "{ element: 'elementId is required' }"; 
		HttpResponse<String> result = Unirest.post(server.getURL("v1/search/path?mode=id&type=ecore"))
				.body(search)
				.asString();
		
		assertEquals(400, result.getStatus());
	}
}

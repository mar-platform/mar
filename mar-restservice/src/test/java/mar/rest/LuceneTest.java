package mar.rest;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import mar.common.test.HBaseTest;
import mar.rest.testing.MarHttpServer;
import mar.restservice.services.ResultItem;

@Category(HBaseTest.class)
public class LuceneTest {

	@ClassRule
	public static MarHttpServer server = new MarHttpServer();

	@Test
	public void testBasicSearch() throws IOException {		
		HttpResponse<String> result = Unirest.post(server.getURL("search-text"))	
				.body("Schema")
				.asString();
		
		assertEquals(200, result.getStatus());
		
		ObjectMapper mapperObj = new ObjectMapper();
		ResultItem[] items = mapperObj.readValue(result.getBody(), ResultItem[].class);
		for (ResultItem resultItem : items) {
			System.out.println(resultItem.getId());
		}
	}
}

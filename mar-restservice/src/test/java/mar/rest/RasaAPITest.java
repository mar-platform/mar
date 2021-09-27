package mar.rest;

import io.github.arturkorb.rasa.RasaClient;
import io.github.arturkorb.rasa.model.Context;
import io.github.arturkorb.utils.ApiException;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

public class RasaAPITest {

	public static void main(String[] args) throws ApiException {
		RasaClient rasaClient = new RasaClient().withBasePath("http://localhost:5005");
		Context context = rasaClient.sendMessageWithContextRetrieval("hi", "jesus");
		System.out.println(context);
		System.out.println(context.getSlots());
		System.out.println(context.getLatestAction());
		System.out.println(context.getParseResult().getIntentRanking());
	//	interact("Hi!");
	//	interact("I want to search ecore models");
	}
	
	
	
	public static void interact(String s) {
		String json = "{\n"
				+ "\n"
				+ "    \"text\": \"" + s + "\",\n"
				+ "    \"message_id\": \"b2831e73-1407-4ba0-a861-0f30a42a2a5a\"\n"
				+ "\n"
				+ "}";
		HttpResponse<String> res = Unirest.post("http://localhost:5005/model/parse")
			.body(json)
			.asString();
		
		System.out.println(res.getBody());
	}
}

package mar.chatbot;

import com.google.api.gax.rpc.ApiException;
import com.google.cloud.dialogflow.v2.DetectIntentResponse;
import com.google.cloud.dialogflow.v2.QueryInput;
import com.google.cloud.dialogflow.v2.QueryResult;
import com.google.cloud.dialogflow.v2.SessionName;
import com.google.cloud.dialogflow.v2.SessionsClient;
import com.google.cloud.dialogflow.v2.TextInput;
import com.google.common.collect.Maps;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class DetectIntentTexts {

	public static void main(String[] args) throws ApiException, IOException {
		detectIntentTexts("mar-nwuc", Arrays.asList("hi"), "123456789", "en-US");
	}

	// DialogFlow API Detect Intent sample with text inputs.
	public static Map<String, QueryResult> detectIntentTexts(String projectId, List<String> texts, String sessionId,
			String languageCode) throws IOException, ApiException {
		Map<String, QueryResult> queryResults = Maps.newHashMap();
		// Instantiates a client
		try (SessionsClient sessionsClient = SessionsClient.create()) {
			// Set the session name using the sessionId (UUID) and projectID (my-project-id)
			SessionName session = SessionName.of(projectId, sessionId);
			System.out.println("Session Path: " + session.toString());

			// Detect intents for each text input
			for (String text : texts) {
				// Set the text (hello) and language code (en-US) for the query
				TextInput.Builder textInput = TextInput.newBuilder().setText(text).setLanguageCode(languageCode);

				// Build the query with the TextInput
				QueryInput queryInput = QueryInput.newBuilder().setText(textInput).build();

				// Performs the detect intent request
				DetectIntentResponse response = sessionsClient.detectIntent(session, queryInput);

				// Display the query result
				QueryResult queryResult = response.getQueryResult();

				System.out.println("====================");
				System.out.format("Query Text: '%s'\n", queryResult.getQueryText());
				System.out.format("Detected Intent: %s (confidence: %f)\n", queryResult.getIntent().getDisplayName(),
						queryResult.getIntentDetectionConfidence());
				System.out.format("Fulfillment Text: '%s'\n", queryResult.getFulfillmentText());

				queryResults.put(text, queryResult);
			}
		}
		return queryResults;
	}
}
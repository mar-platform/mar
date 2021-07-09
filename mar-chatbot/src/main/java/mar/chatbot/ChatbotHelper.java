package mar.chatbot;

import java.io.IOException;

import javax.annotation.Nonnull;

import com.google.cloud.dialogflow.v2.DetectIntentResponse;
import com.google.cloud.dialogflow.v2.QueryInput;
import com.google.cloud.dialogflow.v2.SessionName;
import com.google.cloud.dialogflow.v2.SessionsClient;
import com.google.cloud.dialogflow.v2.TextInput;

public class ChatbotHelper {
	private static final String projectId = "mar-nwuc";
	private static final String language  = "en-US";
	
	public static ChatbotResponse doConversation(String text, String sessionId) {	
		try (SessionsClient sessionsClient = SessionsClient.create()) {
			SessionName session = SessionName.of(projectId, sessionId);
			DetectIntentResponse response = sendText(sessionsClient, session, "hi", language);
			
			return new ChatbotResponse(response);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	@Nonnull
	private static DetectIntentResponse sendText(SessionsClient sessionClient, SessionName session, String text, String languageCode) {
		// Set the text (hello) and language code (en-US) for the query
		TextInput.Builder textInput = TextInput.newBuilder().setText(text).setLanguageCode(languageCode);

		// Build the query with the TextInput
		QueryInput queryInput = QueryInput.newBuilder().setText(textInput).build();

		// Performs the detect intent request
		DetectIntentResponse response = sessionClient.detectIntent(session, queryInput);
		
		return response;
	}

}

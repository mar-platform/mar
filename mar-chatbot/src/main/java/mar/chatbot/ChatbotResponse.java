package mar.chatbot;

import com.google.cloud.dialogflow.v2.DetectIntentResponse;

public class ChatbotResponse {

	private DetectIntentResponse response;

	public ChatbotResponse(DetectIntentResponse response) {
		this.response = response;
	}

	public String getMessage() {
		return response.getQueryResult().getFulfillmentText();
	}

}

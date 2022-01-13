package mar.chatbot.actions;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class ActionResponse {

	@JsonProperty
	public abstract String getType();
}

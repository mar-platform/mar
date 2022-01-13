package mar.chatbot.actions;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A simple message to be shown in the UI.
 * 
 * @author jesus
 */
public class ActionMessage extends ActionResponse {

	@Nonnull
	private final String message;

	public ActionMessage(@Nonnull String message) {
		this.message = message;
	}
	
	@Override
	public String getType() {
		return "message";
	}
	
	@JsonProperty
	@Nonnull
	public String getMessage() {
		return message;
	}

}

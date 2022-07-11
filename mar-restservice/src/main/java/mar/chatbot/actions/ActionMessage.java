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
	private final int key;

	public ActionMessage(@Nonnull String message, int key) {
		this.message = message;
		this.key = key;
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
	
	@JsonProperty
	@Nonnull
	public int getKey() {
		return key;
	}

}

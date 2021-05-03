package mar.chatbot.elements;

import com.google.gson.JsonSyntaxException;

public abstract class ElementId implements IElement{
	
	protected final String elementId;

	public ElementId(String elementId) {
		this.elementId = elementId;
	}
	
	// This is a hack, we should rely on jackson annotations
	public void check() throws JsonSyntaxException {
		if (elementId == null)
			throw new JsonSyntaxException("elementId is a required property");
	}
}

package mar.chatbot.elements;

import java.util.List;

import javax.annotation.Nonnull;

import mar.restservice.services.ResultItem;

public class ChatBotResults {
	
	private final int key;
	private final List<ResultItem> results;
	
	public ChatBotResults(int key, @Nonnull List<ResultItem> items) {
		this.key = key;
		this.results = items;
	}

	public int getKey() {
		return key;
	}

	@Nonnull
	public List<ResultItem> getResults() {
		return results;
	}

}

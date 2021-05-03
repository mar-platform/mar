package mar;

import edu.umd.cs.findbugs.annotations.NonNull;
import mar.restservice.chatbot.HBaseChatBotScorer;
import mar.restservice.chatbot.IChatBotScorer;

public class MarChatBotConfiguration {
	@NonNull
	private IChatBotScorer scorer;

	public MarChatBotConfiguration(IChatBotScorer scorer) {
		super();
		this.scorer = scorer;
	}
	
	@NonNull
	public static MarChatBotConfiguration getHbaseConfiguration(String model)  {
		return new MarChatBotConfiguration(new HBaseChatBotScorer(model));
	}

	public IChatBotScorer getScorer() {
		return scorer;
	}

	public void setScorer(IChatBotScorer scorer) {
		this.scorer = scorer;
	}
	
}

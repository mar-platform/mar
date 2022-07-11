package mar.chatbot.actions;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import mar.restservice.services.ResultItem;

public class ActionResultList extends ActionMessage {

	@JsonProperty
	private List<ResultItem> items;

	public ActionResultList(String message,int key, List<ResultItem> items) {
		super(message,key);
		this.items = items;
	}

	@Override
	public String getType() {
		return "result_list";
	}
	
}

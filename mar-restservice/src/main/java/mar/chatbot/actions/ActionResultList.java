package mar.chatbot.actions;

import java.util.HashMap;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import mar.restservice.services.ResultItem;

public class ActionResultList extends ActionMessage {

	@JsonProperty
	private List<ResultItem> items;
	
	@JsonProperty
	private List<String> modeltype;
	
	@JsonProperty
	private List<String> origins;
	
	@JsonProperty
	private List<String> category;
	
	@JsonProperty
	private List<String> topics;

	public ActionResultList(String message,int key, List<ResultItem> items, List<String> modeltype, List<String> origins, List<String> category, List<String> topics){
		super(message,key);
		this.items = items;
		this.origins = origins;
		this.modeltype = modeltype;
		this.category = category;
		this.topics = topics;
	}

	@Override
	public String getType() {
		return "result_list";
	}
	
}

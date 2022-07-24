package mar.chatbot.executiontrace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.annotation.Nonnull;

import edu.umd.cs.findbugs.annotations.NonNull;
import io.github.arturkorb.rasa.model.Entity;
import io.github.arturkorb.rasa.model.Intent;
import mar.chatbot.actions.ActionMessage;
import mar.chatbot.actions.ActionResponse;
import mar.chatbot.actions.ActionResultList;
import mar.restservice.HBaseStats;
import mar.restservice.HBaseStats.Stats;
import mar.restservice.services.ResultItem;
import mar.restservice.services.SearchService;
import mar.restservice.services.SearchService.SearchException;

public class Conversation {
	private ExecutionGraph graph = new ExecutionGraph();
	
	public ExecutionGraph getGraph() {
		return graph;
	}

	@Nonnull
	public ActionResponse process(@Nonnull SearchService searchService, @Nonnull Intent intent, @Nonnull List<? extends Entity> entities, HashMap<Integer,List<ResultItem>> save, int key, HashMap<Integer,List<String>> modeltype, HashMap<Integer,List<String>> origins, HashMap<Integer,List<String>> categories, HashMap<Integer,List<String>> topics, HashMap<Integer,String> keywordsave) {
		switch (intent.getName()) {
		case "greet":
			return new ActionMessage("Hi there! I'm here to help :-)",key);
		case "search_a_topic":
			return searchkeyword(searchService,key, entities,save,modeltype,origins,categories,topics,keywordsave);
		case "i_want_to_search":
			return new ActionMessage(stats(entities),key);
		case "ask_param":
			return new ActionMessage(param(searchService, entities,save,key),key);
		case "add_model":
			return searchkeyword(searchService,key, entities,save,modeltype,origins,categories,topics,keywordsave);
		case "add_origin":
			return searchkeyword(searchService,key, entities,save,modeltype,origins,categories,topics,keywordsave);
		case "add_category":
			return searchkeyword(searchService,key, entities,save,modeltype,origins,categories,topics,keywordsave);
		case "add_topic":
			return searchkeyword(searchService,key, entities,save,modeltype,origins,categories,topics,keywordsave);
		case "sort":
			return new ActionMessage("You can sort by Similarity, Quality or Popularity",key);
		default:
			return new ActionMessage("I don't understand. Try something else. I will provide suggestions when I'm smarter",key);
		}
	}
	
	
	private String param(@Nonnull SearchService searchServicer, List<? extends Entity> entities, HashMap<Integer,List<ResultItem>> save, int key) {
		if(save.get(key) != null) {		
			/*try {
				String origins="";
				List<String> tabOrigin = new ArrayList<String>();
				List<ResultItem> items = searchServicer.textSearch(save.get(key));
				List<ResultItem> itemsToOrigin;
				for (ResultItem item : items) {
					if(tabOrigin.contains(item.getOrigin()) == false) {
						tabOrigin.add(item.getOrigin());
					}
				}
				for (String item : tabOrigin) {
					origins = origins + item + " ";
				}
				return "You can put different origins like "+origins;
			} catch (SearchException e) {
				return "It seems we have an internal error";
			}*/
			String origins="";
			List<String> tabOrigin = new ArrayList<String>();
			String model="";
			List<String> tabModel = new ArrayList<String>();
			String category="";
			List<String> tabCategory = new ArrayList<String>();
			String topics="";
			List<String> tabTopics = new ArrayList<String>();
			List<ResultItem> items = save.get(key);
			for (ResultItem item : items) {
				if(tabOrigin.contains(item.getOrigin()) == false) {
					tabOrigin.add(item.getOrigin());
				}
				if(tabModel.contains(item.getModelType()) == false) {
					tabModel.add(item.getModelType());
				}
				if(item.getMetadata()!=null) {
					if(tabCategory.contains(item.getMetadata().getCategory()) == false) {
						tabCategory.add(item.getMetadata().getCategory());
					}
					for (String topic : item.getMetadata().getTopics()) {
						if(tabTopics.contains(topic) == false) {
							tabTopics.add(topic);
						}
					}
				}
			}
			for (String item : tabOrigin) {
				origins = origins + item + " ";
			}
			for (String item : tabModel) {
				model = model + item + " ";
			}
			for (String item : tabCategory) {
				category = category + item + " ";
			}
			for (String item : tabTopics) {
				topics = topics + item + " ";
			}
			return "You can put different origins like "+origins +", you can put different modelType like "+model+".\nYou can also put different categories like "+category+".\n There's also different topics you can put like "+topics;
		}
		else {
			return "To get the different parameters you need to do a search before in the chatbot";
		}
	}

	private ActionResponse searchkeyword(@Nonnull SearchService searchServicer, int key, List<? extends Entity> entities, HashMap<Integer,List<ResultItem>> save, HashMap<Integer,List<String>> modeltype, HashMap<Integer,List<String>> origins, HashMap<Integer,List<String>> categories, HashMap<Integer,List<String>> topics, HashMap<Integer,String> keywordsave) {
		String keyword = keywordsave.get(key);
		/*for (Entity entity : entities) {
			if ("keyword".equals(entity.getEntity())) {
				keyword = entity.getValue();
				keyword = keyword.substring(keyword.indexOf(" ")+1);
			}
		}*/
		
		if (keyword == null) {
			return new ActionMessage("Please tell me a few keywords",key);
		}
		List<ResultItem> items = save.get(key);
		return new ActionResultList("Here are some results",key, items,modeltype.get(key),origins.get(key),categories.get(key),topics.get(key));
	}
	
	// TODO: We should pass an object with the services available
	@NonNull
	private final HBaseStats stats = new HBaseStats();
	
	private String stats(List<? extends Entity> entities) {
		String value = null;
		for (Entity entity : entities) {
			if ("model_type".equals(entity.getEntity())) {
				value = entity.getValue();
			}
		}
		
		try {
			Stats s = stats.getStats();
			StringBuilder answer = new StringBuilder();
			Integer number = s.getCounters().get(value);

			if (value == null) {
				answer.append("We have many models:\n");
				s.getCounters().forEach((k, v) -> { if (v > 0) answer.append("  - " + v + " " + k + " models.\n"); } );
			} else if (number == null) {	
				answer.append("I don't have " + value + " models. ");
				answer.append("But I have others.\n");
				s.getCounters().forEach((k, v) -> { if (v > 0) answer.append("  - " + v + " " + k + " models.\n"); } );
			} else {
				answer.append("Great! I have " + number + " " + value + " models");
			}
			
			return answer.toString();
		} catch (Exception e) {
			return "It seems we had an internal error recovering our stats! Sorry :-(";
		}
	}
}

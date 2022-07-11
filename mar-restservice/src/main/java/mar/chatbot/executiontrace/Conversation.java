package mar.chatbot.executiontrace;

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
	public ActionResponse process(@Nonnull SearchService searchService, @Nonnull Intent intent, @Nonnull List<? extends Entity> entities, String save, int key) {
		switch (intent.getName()) {
		case "greet":
			return new ActionMessage("Hi there! I'm here to help :-)",key);
		case "search_a_topic":
			return searchkeyword(searchService,key, entities);
		case "i_want_to_search":
			return new ActionMessage(stats(entities),key);
		case "add_param":
			return new ActionMessage(param(searchService, entities,save),key);
		default:
			return new ActionMessage("I don't understand. Try something else. I will provide suggestions when I'm smarter",key);
		}
	}
	private String param(@Nonnull SearchService searchServicer, List<? extends Entity> entities, String save) {
				
		try {
			String origins="";
			List<ResultItem> items = searchServicer.textSearch(save);
			List<ResultItem> itemsToOrigin;
			for (ResultItem item : items) {
				
			}
			for (ResultItem item : items) {
				origins = origins + item.getOrigin() + " ";
			}
			return "You can put different origins like "+origins;
		} catch (SearchException e) {
			return "You need to do a search before in the chatbot";
		}
	}

	private ActionResponse searchkeyword(@Nonnull SearchService searchServicer, int key, List<? extends Entity> entities) {
		String keyword = null;
		for (Entity entity : entities) {
			if ("keyword".equals(entity.getEntity())) {
				keyword = entity.getValue();
				keyword = keyword.substring(keyword.indexOf(" ")+1);
			}
		}
		
		if (keyword == null) {
			return new ActionMessage("Please tell me a few keywords",key);
		}
				
		try {
			List<ResultItem> items = searchServicer.textSearch(keyword);
			return new ActionResultList("Here are some results",key, items);
		} catch (SearchException e) {
			return new ActionMessage(e.getMessage(),key);
		}
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

package mar.chatbot.executiontrace;

import java.util.List;

import javax.annotation.Nonnull;

import org.sparkproject.jetty.proxy.AsyncMiddleManServlet;

import edu.umd.cs.findbugs.annotations.NonNull;
import io.github.arturkorb.rasa.model.Entity;
import io.github.arturkorb.rasa.model.Intent;
import mar.restservice.HBaseStats;
import mar.restservice.HBaseStats.Stats;

public class Conversation {
	private ExecutionGraph graph = new ExecutionGraph();
	
	public ExecutionGraph getGraph() {
		return graph;
	}

	@Nonnull
	public String process(@Nonnull Intent intent, @Nonnull List<? extends Entity> entities) {
		switch (intent.getName()) {
		case "greet":
			return "Hi there! I'm here to help :-)";
		case "i_want_to_search":
			return search(entities);
		default:
			return "I don't understand. Try something else. I will provide suggestions when I'm smarter";
		}
	}

	// TODO: We should pass an object with the services available
	@NonNull
	private final HBaseStats stats = new HBaseStats();
	
	private String search(List<? extends Entity> entities) {
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

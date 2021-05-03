package mar.restservice.chatbot;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import edu.umd.cs.findbugs.annotations.NonNull;
import mar.chatbot.elements.ElementsSet;
import mar.chatbot.elements.IElement;
import mar.chatbot.elements.SingleElement;


public interface IChatBotScorer {
	
	public default Map<String, Double> score(@NonNull ElementsSet set) throws IOException{
		switch (set.getType()) {
		case OR:
			return scoreOR(set);
		case AND:
			return scoreAND(set);
		}
		return null;
	}
	
	public default Map<String, Double> scoreAND(@NonNull ElementsSet set) throws IOException{
		Map<String, Double> result = new HashMap<String, Double>();
		boolean first = true;
		for (IElement element : set.getSet()) {
			Map<String, Double> mapElement = null;
			if (element instanceof SingleElement) {
				mapElement = scoreSingle((SingleElement) element);
			} else if (element instanceof ElementsSet) {
				mapElement = score((ElementsSet) element);
			}
			if (mapElement!=null) {
				if (first) {
					result = mapElement;
					first = false;
				} else {
					if (!result.isEmpty()) {
						result.keySet().retainAll(mapElement.keySet());
						for (Entry<String, Double> el : mapElement.entrySet()) {
							if (result.containsKey(el.getKey()))
								result.put(el.getKey(), result.get(el.getKey()) + el.getValue());
						}
					}
				}
			}
		}
		return result;
	}
	
	public default Map<String, Double> scoreOR(@NonNull ElementsSet set) throws IOException{
		Map<String, Double> result = new HashMap<String, Double>();
		for (IElement element : set.getSet()) {
			Map<String, Double> mapElement = null;
			if (element instanceof SingleElement) {
				mapElement = scoreSingle((SingleElement) element);
			} else if (element instanceof ElementsSet) {
				mapElement = score((ElementsSet) element);
			}
			if (mapElement!=null) {
				for (Entry<String, Double> el : mapElement.entrySet()) {
					if (result.containsKey(el.getKey()))
						result.put(el.getKey(), result.get(el.getKey()) + el.getValue());
					else
						result.put(el.getKey(), el.getValue());
				}
			}
		}
		return result;
	}
	
	public Map<String, Double> scoreSingle(@NonNull SingleElement single) throws IOException;
	
}

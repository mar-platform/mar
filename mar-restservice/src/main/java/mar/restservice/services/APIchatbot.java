package mar.restservice.services;

import static spark.Spark.post;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import mar.chatbot.elements.ChatBotResults;
import mar.chatbot.elements.EcoreElementId;
import mar.chatbot.elements.ElementId;
import mar.chatbot.elements.ElementsSet;
import mar.chatbot.elements.IElement;
import mar.chatbot.elements.SetType;
import mar.chatbot.elements.SingleElement;
import mar.chatbot.executiontrace.Cache;
import mar.restservice.HBaseGetInfo;
import mar.restservice.services.SearchOptions.SyntaxType;
import spark.Request;
import spark.Response;

public class APIchatbot extends AbstractService{
	
	private Cache cache = new Cache();

	public APIchatbot(IConfigurationProvider configuration) {
		super(configuration);	
        post("/v1/search/path", this::searchPath);
	}
	
	public Object searchPath(Request req, Response res) throws IOException, InvalidMarRequest {
		String mode = req.queryParams("mode");
		ChatBotResults scores;
		switch (mode) {
		case "and":
			scores = doSearch(req, res, SetType.AND);
			break;
		case "or":
			scores = doSearch(req, res, SetType.OR);
			break;
		case "id":
			scores = doSearchID(req, res);
			break;			
		default:
			throw new InvalidMarRequest(req, "Invalid search mode " + mode);
		}
		
		Gson gson = new Gson();
	    String jsonResp = gson.toJson(scores);
		res.type("text/json");	    
	    return jsonResp;
	}
		
	public ChatBotResults doSearch(Request req, Response res, SetType settype) throws IOException, InvalidMarRequest {
		int mykey = getKey(req);		
		SearchOptions options = SearchOptions.get(req, SyntaxType.paths);
					
		ElementsSet set = cache.getGlobalQuery(mykey);			
		ElementsSet deserializedModel = deserialize(req, options.getModel(), settype);
		set.addElement(deserializedModel);
		
		Map<String, Double> scores =  getChatBotConfiguration(options.getModelType().name()).getScorer().score(set);
		Map<String, Double> filtered = firtsElements(scores, options.getMaxResults());
		
		List<ResultItem> items = new ArrayList<ResultItem>(filtered.size());
		filtered.forEach((id, score) -> {
			items.add(new ResultItem(id, score));
		});
		
		try(HBaseGetInfo info = new HBaseGetInfo()) {
			info.updateInformation(items);
		}
		
		cache.addStep(scores, deserializedModel, mykey);
		
		return new ChatBotResults(mykey, items);
	}
	
	public ChatBotResults doSearchID(Request req, Response res) throws IOException, InvalidMarRequest { 
		int mykey = getKey(req);		
		SearchOptions options = SearchOptions.get(req, SyntaxType.paths);
	    
		String model = options.getModel();
		
		ElementsSet set = cache.getGlobalQuery(mykey);
		ElementsSet idDoc = deserializeID(req, model);
		set.addElement(idDoc);		
		
		Map<String, Double> scores =  getChatBotConfiguration(options.getModelType().name()).getScorer().score(set);
		Map<String, Double> filtered = firtsElements(scores, options.getMaxResults());
		
		List<ResultItem> items = new ArrayList<ResultItem>(filtered.size());
		filtered.forEach((id, score) -> {
			items.add(new ResultItem(id, score));
		});
		
		try(HBaseGetInfo info = new HBaseGetInfo()) {
			info.updateInformation(items);
		}
		
		cache.addStep(scores, idDoc, mykey);
		
		return new ChatBotResults(mykey, items);
	}

	private int getKey(@Nonnull Request req) {
		String key = req.queryParams("key");					
		int mykey;
		if (key == null) {
			mykey = cache.getKey();
		} else {
			mykey = Integer.parseInt(key);
		}
		return mykey;
	}

	private static ElementsSet deserialize(Request req, String json, SetType type) throws InvalidMarRequest {
		try {
			Gson gson = new Gson();
			List<SingleElement> ls = gson.fromJson(json, new TypeToken<List<SingleElement>>(){}.getType());
			Set<IElement> set = new HashSet<IElement>();
			set.addAll(ls);
			return new ElementsSet(set, type);
		} catch (JsonSyntaxException e) {
			throw new InvalidMarRequest(req, e.getMessage());
		}
	}
	
	private static ElementsSet deserializeID(Request req, String json) throws InvalidMarRequest {
		try {
			Gson gson = new Gson();
			ElementId eid = gson.fromJson(json, EcoreElementId.class);
			eid.check();
			return eid.toSet();
		} catch (JsonSyntaxException e) {
			throw new InvalidMarRequest(req, e.getMessage());			
		}
	}

}

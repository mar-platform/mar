package mar.restservice.services;

import static spark.Spark.post;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import io.github.arturkorb.rasa.RasaClient;
import io.github.arturkorb.rasa.model.Context;
import io.github.arturkorb.rasa.model.Entity;
import io.github.arturkorb.rasa.model.Intent;
import io.github.arturkorb.utils.ApiException;
import mar.chatbot.actions.ActionMessage;
import mar.chatbot.actions.ActionResultList;
import mar.chatbot.elements.ChatBotResults;
import mar.chatbot.elements.EcoreElementId;
import mar.chatbot.elements.ElementId;
import mar.chatbot.elements.ElementsSet;
import mar.chatbot.elements.IElement;
import mar.chatbot.elements.SetType;
import mar.chatbot.elements.SingleElement;
import mar.chatbot.executiontrace.Cache;
import mar.chatbot.executiontrace.Conversation;
import mar.restservice.HBaseGetInfo;
import mar.restservice.services.SearchOptions.SyntaxType;
import mar.restservice.services.SearchService.SearchException;
import spark.Request;
import spark.Response;

public class APIchatbot extends AbstractAPI {

	private final Cache cache = new Cache();
	private final RasaClient rasaClient;
	private final SearchService searchService;
	public HashMap<Integer,String> keywordsave= new HashMap<>();
	public HashMap<Integer, List<ResultItem>> save = new HashMap<>();
	public HashMap<Integer,List<String>> modeltype = new HashMap<>(); // to know all the origins decided by the user
	public HashMap<Integer,List<String>> origins = new HashMap<>();
	public HashMap<Integer,List<String>> categories = new HashMap<>();
	public HashMap<Integer,List<String>> topics = new HashMap<>();
	
	public APIchatbot(IConfigurationProvider configuration) {
		super(configuration);	
		this.rasaClient = new RasaClient().withBasePath("http://localhost:5005");
		this.searchService = new SearchService(configuration.newSearcher());
		
		post("/v1/search/path", this::searchPath);
        post("/v1/chatbot/conversation", this::conversation);
	}
	
	
	public Object conversation(Request req, Response res) throws IOException, InvalidMarRequest, ApiException {
		int key = getKey(req);
		if(key==0) {
			key=1;
		}
		String text = req.body();
		Conversation conversation = cache.getConversation(key);
		
		if (text == null || text.isEmpty())
			throw new InvalidMarRequest(req, "Body must be a not empty message");
		
		String conversationId = "conversation-" + key;
		Context context = rasaClient.sendMessageWithContextRetrieval(text, conversationId);
		
		Intent intent = context.getParseResult().getIntentRanking().get(0);
		List<Entity> entities = context.getParseResult().getEntities();
		String keyword = null;
		List<String> origin = new ArrayList<String>();
		List<String> model = new ArrayList<String>();
		List<String> category = new ArrayList<String>();
		List<String> topic = new ArrayList<String>();
		for (Entity entity : entities) {
			if ("keyword".equals(entity.getEntity())) {
				keyword = entity.getValue();
				keyword = keyword.substring(keyword.indexOf(" ")+1);
				keywordsave.put(key,keyword);
				try {
					List<ResultItem> items = this.searchService.textSearch(keyword);
					save.put(key,items);
				} catch (SearchException e) {
					
				}
				
			}
			else if ("model".equals(entity.getEntity())) { // if we detect a good keyword we put it if it was not there
				if(!modeltype.containsValue(entity.getValue())) {
					if(modeltype.get(key)!= null) {
						model=modeltype.get(key);
					}
					model.add(entity.getValue());
				}
				//else we delete it
				modeltype.put(key,model);
				
			}
			else if ("origin".equals(entity.getEntity())) { // if we detect a good keyword we put it if it was not there
				if(!origins.containsValue(entity.getValue())) {
					if(origins.get(key)!= null) {
						origin=origins.get(key);
					}
					origin.add(entity.getValue());
				}
				//else we delete it
				origins.put(key,origin);
				
			}
			else if ("category".equals(entity.getEntity())) { // if we detect a good keyword we put it if it was not there
				if(!categories.containsValue(entity.getValue())) {
					if(categories.get(key)!= null) {
						category=categories.get(key);
					}
					category.add(entity.getValue().substring(entity.getValue().indexOf(" ")+1));
				}
				//else we delete it
				categories.put(key,category);
				
			}
			else if ("topic".equals(entity.getEntity())) { // if we detect a good keyword we put it if it was not there
				if(!topics.containsValue(entity.getValue())) {
					if(topics.get(key)!= null) {
						topic=topics.get(key);
					}
					topic.add(entity.getValue().substring(entity.getValue().indexOf(" ")+1));
				}
				//else we delete it
				topics.put(key,topic);
				
			}
		
		}
		return toJson(res, conversation.process(this.searchService,intent, entities,save,key,modeltype,origins,categories,topics,keywordsave));
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
			// Make sure that they was created by the cache
			if (! cache.hasKey(mykey)) {
				mykey = cache.getKey();
			}
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
